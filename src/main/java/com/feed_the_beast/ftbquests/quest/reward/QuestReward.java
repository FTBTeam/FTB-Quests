package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestReward extends QuestObjectBase
{
	public final Quest quest;

	public boolean team = false;
	private boolean emergency = false;

	public QuestReward(Quest q)
	{
		quest = q;
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
	public abstract void writeData(NBTTagCompound nbt);

	@Override
	public abstract void readData(NBTTagCompound nbt);

	@Override
	public abstract void getConfig(ConfigGroup config);

	public abstract void claim(EntityPlayerMP player);

	@Override
	public final void getExtraConfig(ConfigGroup config)
	{
		super.getExtraConfig(config);
		config.addBool("team", () -> team, v -> team = v, false).setDisplayName(new TextComponentTranslation("ftbquests.reward.team_reward")).setCanEdit(!quest.canRepeat);
		//config.addBool("emergency", () -> emergency, v -> emergency = v, false).setDisplayName(new TextComponentTranslation("ftbquests.reward.emergency"));
	}

	@Override
	public void resetProgress(ITeamData data, boolean dependencies)
	{
		data.unclaimRewards(Collections.singleton(this));
	}

	@Override
	public final void writeCommonData(NBTTagCompound nbt)
	{
		super.writeCommonData(nbt);

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
	public final void readCommonData(NBTTagCompound nbt)
	{
		super.readCommonData(nbt);

		team = nbt.getBoolean("team_reward");
		emergency = nbt.getBoolean("emergency");
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
		return GuiIcons.MONEY_BAG;
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return QuestRewardType.getType(getClass()).getDisplayName();
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