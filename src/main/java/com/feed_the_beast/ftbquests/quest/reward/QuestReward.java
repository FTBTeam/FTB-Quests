package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.EnumTristate;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.net.MessageDisplayRewardToast;
import com.feed_the_beast.ftbquests.quest.EnumChangeProgress;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestReward extends QuestObjectBase
{
	public Quest quest;

	public EnumTristate team;
	public EnumTristate autoclaim;
	public boolean invisible;

	public QuestReward(Quest q)
	{
		quest = q;
		team = EnumTristate.DEFAULT;
		autoclaim = EnumTristate.DEFAULT;
		invisible = false;
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
	@Nullable
	public final QuestChapter getQuestChapter()
	{
		return quest.chapter;
	}

	@Override
	public final int getParentID()
	{
		return quest.id;
	}

	public abstract QuestRewardType getType();

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		team.write(nbt, "team_reward");
		autoclaim.write(nbt, "autoclaim");

		if (invisible)
		{
			nbt.setBoolean("invisible", true);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		team = EnumTristate.read(nbt, "team_reward");
		autoclaim = EnumTristate.read(nbt, "autoclaim");
		invisible = nbt.getBoolean("invisible");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		EnumTristate.NAME_MAP.write(data, team);
		EnumTristate.NAME_MAP.write(data, autoclaim);
		data.writeBoolean(invisible);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		team = EnumTristate.NAME_MAP.read(data);
		autoclaim = EnumTristate.NAME_MAP.read(data);
		invisible = data.readBoolean();
	}

	@Override
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.addEnum("team", () -> team, v -> team = v, EnumTristate.NAME_MAP).setDisplayName(new TextComponentTranslation("ftbquests.reward.team_reward")).setCanEdit(!(quest instanceof Quest) || !quest.canRepeat);
		config.addEnum("autoclaim", () -> autoclaim, v -> autoclaim = v, EnumTristate.NAME_MAP).setDisplayName(new TextComponentTranslation("ftbquests.reward.autoclaim")).setCanEdit(!invisible);
		config.addBool("invisible", () -> invisible, v -> invisible = v, false).setDisplayName(new TextComponentTranslation("ftbquests.reward.invisible"));
	}

	public abstract void claim(EntityPlayerMP player);

	public ItemStack claimAutomated(TileEntity tileEntity, @Nullable EntityPlayerMP player)
	{
		if (player != null)
		{
			MessageDisplayRewardToast.ENABLED = false;
			claim(player);
			MessageDisplayRewardToast.ENABLED = true;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public final void deleteSelf()
	{
		quest.rewards.remove(this);

		Collection<QuestReward> c = Collections.singleton(this);

		for (QuestData data : getQuestFile().getAllData())
		{
			data.unclaimRewards(c);
		}

		super.deleteSelf();
	}

	@Override
	public final void deleteChildren()
	{
		Collection<QuestReward> c = Collections.singleton(this);

		for (QuestData data : getQuestFile().getAllData())
		{
			data.unclaimRewards(c);
		}

		super.deleteChildren();
	}

	@Override
	public void editedFromGUI()
	{
		GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

		if (gui != null && gui.getViewedQuest() != null)
		{
			gui.viewQuestPanel.refreshWidgets();
		}

		if (gui != null)
		{
			gui.questPanel.refreshWidgets();
		}
	}

	@Override
	public void onCreated()
	{
		quest.rewards.add(this);
	}

	public final boolean isTeamReward()
	{
		return quest.canRepeat || team.get(quest.chapter.file.defaultRewardTeam);
	}

	public final boolean shouldAutoClaimReward()
	{
		return invisible || autoclaim.get(quest.chapter.alwaysInvisible || quest.chapter.file.defaultRewardAutoclaim);
	}

	@Override
	public final void changeProgress(QuestData data, EnumChangeProgress type)
	{
		if (type == EnumChangeProgress.RESET || type == EnumChangeProgress.RESET_DEPS)
		{
			data.unclaimRewards(Collections.singleton(this));
		}
	}

	@Override
	public Icon getAltIcon()
	{
		return getType().getIcon();
	}

	@Override
	public String getAltTitle()
	{
		return getType().getDisplayName();
	}

	@Override
	public final ConfigGroup createSubGroup(ConfigGroup group)
	{
		QuestRewardType type = getType();
		return group.getGroup(getObjectType().getID()).getGroup(type.getRegistryName().getNamespace()).getGroup(type.getRegistryName().getPath());
	}

	public void addMouseOverText(List<String> list)
	{
	}

	public boolean addTitleInMouseOverText()
	{
		return true;
	}

	public void onButtonClicked()
	{
		new MessageClaimReward(id).sendToServer();
	}

	public boolean getExcludeFromClaimAll()
	{
		return getType().getExcludeFromListRewards();
	}

	@Nullable
	public Object getIngredient()
	{
		return getIcon().getIngredient();
	}

	@Override
	public final int refreshJEI()
	{
		return FTBQuestsJEIHelper.QUESTS;
	}

	public String getButtonText()
	{
		return "";
	}
}