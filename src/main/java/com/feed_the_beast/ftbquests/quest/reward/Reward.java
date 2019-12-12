package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.Tristate;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class Reward extends QuestObjectBase
{
	public Quest quest;

	public Tristate team;
	public RewardAutoClaim autoclaim;

	public Reward(Quest q)
	{
		quest = q;
		team = Tristate.DEFAULT;
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
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		team.write(nbt, "team_reward");

		if (autoclaim != RewardAutoClaim.DEFAULT)
		{
			nbt.putString("auto", autoclaim.id);
		}
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		team = Tristate.read(nbt, "team_reward");
		autoclaim = RewardAutoClaim.NAME_MAP.get(nbt.getString("auto"));
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		Tristate.NAME_MAP.write(buffer, team);
		RewardAutoClaim.NAME_MAP.write(buffer, autoclaim);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		team = Tristate.NAME_MAP.read(buffer);
		autoclaim = RewardAutoClaim.NAME_MAP.read(buffer);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addEnum("team", team, v -> team = v, Tristate.NAME_MAP).setNameKey("ftbquests.reward.team_reward").setCanEdit(!quest.canRepeat);
		config.addEnum("autoclaim", autoclaim, v -> autoclaim = v, RewardAutoClaim.NAME_MAP).setNameKey("ftbquests.reward.autoclaim");
	}

	public abstract void claim(ServerPlayerEntity player, boolean notify);

	public ItemStack claimAutomated(TileEntity tileEntity, @Nullable ServerPlayerEntity player)
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

		for (PlayerData data : getQuestFile().getAllData())
		{
			data.setRewardClaimed(id, false);
		}

		super.deleteSelf();
	}

	@Override
	public final void deleteChildren()
	{
		for (PlayerData data : getQuestFile().getAllData())
		{
			data.setRewardClaimed(id, false);
		}

		super.deleteChildren();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
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
	public final void changeProgress(PlayerData data, ChangeProgress type)
	{
		if (type == ChangeProgress.RESET || type == ChangeProgress.RESET_DEPS)
		{
			data.setRewardClaimed(id, false);
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
		return group.getGroup(getObjectType().id).getGroup(type.getRegistryName().getNamespace()).getGroup(type.getRegistryName().getPath());
	}

	public void addMouseOverText(List<String> list)
	{
	}

	public boolean addTitleInMouseOverText()
	{
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public void onButtonClicked(Button button, boolean canClick)
	{
		if (canClick)
		{
			button.playClickSound();
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