package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.io.Bits;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestReward extends QuestObjectBase
{
	public final Quest quest;

	public boolean team;
	private boolean emergency;

	public QuestReward(Quest q)
	{
		quest = q;
		team = quest.chapter.file.defaultRewardTeam;
		emergency = false;
	}

	@Override
	public final QuestObjectType getObjectType()
	{
		return QuestObjectType.REWARD;
	}

	@Override
	public final QuestFile getQuestFile()
	{
		return quest.chapter.file;
	}

	@Override
	public final QuestChapter getQuestChapter()
	{
		return quest.chapter;
	}

	@Override
	public final int getParentID()
	{
		return quest.uid;
	}

	public abstract QuestRewardType getType();

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);

		if (team != quest.chapter.file.defaultRewardTeam)
		{
			nbt.setBoolean("team_reward", team);
		}

		if (emergency)
		{
			nbt.setBoolean("emergency", true);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		team = nbt.hasKey("team_reward") ? nbt.getBoolean("team_reward") : quest.chapter.file.defaultRewardTeam;
		emergency = nbt.getBoolean("emergency");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, team);
		flags = Bits.setFlag(flags, 2, emergency);
		data.writeVarInt(flags);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		int flags = data.readVarInt();
		team = Bits.getFlag(flags, 1);
		emergency = Bits.getFlag(flags, 2);
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addBool("team", () -> team, v -> team = v, false).setDisplayName(new TextComponentTranslation("ftbquests.reward.team_reward")).setCanEdit(!quest.canRepeat);
		//config.addBool("emergency", () -> emergency, v -> emergency = v, false).setDisplayName(new TextComponentTranslation("ftbquests.reward.emergency"));
	}

	public abstract void claim(EntityPlayerMP player);

	@Override
	public void resetProgress(ITeamData data, boolean dependencies)
	{
		data.unclaimRewards(Collections.singleton(this));
	}

	@Override
	public final void deleteSelf()
	{
		quest.rewards.remove(this);

		Collection<QuestReward> c = Collections.singleton(this);

		for (ITeamData data : quest.chapter.file.getAllData())
		{
			data.unclaimRewards(c);
		}

		super.deleteSelf();
	}

	@Override
	public final void deleteChildren()
	{
		Collection<QuestReward> c = Collections.singleton(this);

		for (ITeamData data : quest.chapter.file.getAllData())
		{
			data.unclaimRewards(c);
		}

		super.deleteChildren();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void editedFromGUI()
	{
		GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

		if (gui != null && gui.selectedQuest != null)
		{
			gui.questRight.refreshWidgets();
		}
	}

	@Override
	public void onCreated()
	{
		quest.rewards.add(this);
	}

	public final boolean isTeamReward()
	{
		return team || quest.canRepeat;
	}

	public final boolean addToEmergencyItems()
	{
		return emergency;
	}

	@Override
	public Icon getAltIcon()
	{
		return getType().getIcon();
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return getType().getDisplayName();
	}

	@Override
	public final ConfigGroup createSubGroup(ConfigGroup group)
	{
		QuestRewardType type = getType();
		return group.getGroup(getObjectType().getName()).getGroup(type.getRegistryName().getNamespace()).getGroup(type.getRegistryName().getPath());
	}

	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list)
	{
	}

	@SideOnly(Side.CLIENT)
	public void onButtonClicked()
	{
		new MessageClaimReward(uid).sendToServer();
	}
}