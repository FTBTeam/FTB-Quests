package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.EnumTristate;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class Reward extends QuestObjectBase
{
	public Quest quest;

	public EnumTristate team;
	public RewardAutoClaim autoclaim;

	public Reward(Quest q)
	{
		quest = q;
		team = EnumTristate.DEFAULT;
		autoclaim = RewardAutoClaim.DEFAULT;
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
	public final Chapter getQuestChapter()
	{
		return quest.chapter;
	}

	@Override
	public final int getParentID()
	{
		return quest.id;
	}

	public abstract RewardType getType();

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		team.write(nbt, "team_reward");

		if (autoclaim != RewardAutoClaim.DEFAULT)
		{
			nbt.setString("auto", autoclaim.getId());
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		team = EnumTristate.read(nbt, "team_reward");
		autoclaim = RewardAutoClaim.NAME_MAP.get(nbt.getString("auto"));

		if (nbt.hasKey("autoclaim") || nbt.hasKey("invisible"))
		{
			if (nbt.getBoolean("invisible"))
			{
				autoclaim = RewardAutoClaim.INVISIBLE;
			}
			else if (nbt.hasKey("autoclaim"))
			{
				autoclaim = nbt.getBoolean("autoclaim") ? RewardAutoClaim.ENABLED : RewardAutoClaim.DISABLED;
			}
		}
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		EnumTristate.NAME_MAP.write(data, team);
		RewardAutoClaim.NAME_MAP.write(data, autoclaim);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		team = EnumTristate.NAME_MAP.read(data);
		autoclaim = RewardAutoClaim.NAME_MAP.read(data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addEnum("team", () -> team, v -> team = v, EnumTristate.NAME_MAP).setDisplayName(new TextComponentTranslation("ftbquests.reward.team_reward")).setCanEdit(!quest.canRepeat);
		config.addEnum("autoclaim", () -> autoclaim, v -> autoclaim = v, RewardAutoClaim.NAME_MAP).setDisplayName(new TextComponentTranslation("ftbquests.reward.autoclaim"));
	}

	public abstract void claim(EntityPlayerMP player, boolean notify);

	public ItemStack claimAutomated(TileEntity tileEntity, @Nullable EntityPlayerMP player)
	{
		if (player != null)
		{
			claim(player, false);
		}

		return ItemStack.EMPTY;
	}

	@Override
	public final void deleteSelf()
	{
		quest.rewards.remove(this);

		Collection<Reward> c = Collections.singleton(this);

		for (QuestData data : getQuestFile().getAllData())
		{
			data.unclaimRewards(c);
		}

		super.deleteSelf();
	}

	@Override
	public final void deleteChildren()
	{
		Collection<Reward> c = Collections.singleton(this);

		for (QuestData data : getQuestFile().getAllData())
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

	public final RewardAutoClaim getAutoClaimType()
	{
		if (quest.chapter.alwaysInvisible && (autoclaim == RewardAutoClaim.DEFAULT || autoclaim == RewardAutoClaim.DISABLED))
		{
			return RewardAutoClaim.ENABLED;
		}

		if (autoclaim == RewardAutoClaim.DEFAULT)
		{
			return quest.chapter.file.defaultRewardAutoClaim;
		}

		return autoclaim;
	}

	@Override
	public final void changeProgress(QuestData data, ChangeProgress type)
	{
		if (type == ChangeProgress.RESET || type == ChangeProgress.RESET_DEPS)
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
		RewardType type = getType();
		return group.getGroup(getObjectType().getId()).getGroup(type.getRegistryName().getNamespace()).getGroup(type.getRegistryName().getPath());
	}

	public void addMouseOverText(List<String> list)
	{
	}

	public boolean addTitleInMouseOverText()
	{
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void onButtonClicked(boolean canClick)
	{
		if (canClick)
		{
			GuiHelper.playClickSound();
			new MessageClaimReward(id, true).sendToServer();
		}
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