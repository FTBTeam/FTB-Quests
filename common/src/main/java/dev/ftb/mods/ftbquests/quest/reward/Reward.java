package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.util.ClientUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.jei.FTBQuestsJEIHelper;
import dev.ftb.mods.ftbquests.net.ClaimRewardMessage;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public abstract class Reward extends QuestObjectBase {
	public Quest quest;

	public Tristate team;
	public RewardAutoClaim autoclaim;
	public boolean optional;

	public Reward(Quest q) {
		quest = q;
		team = Tristate.DEFAULT;
		autoclaim = RewardAutoClaim.DEFAULT;
		optional = getType().getExcludeFromListRewards();
	}

	@Override
	public final QuestObjectType getObjectType() {
		return QuestObjectType.REWARD;
	}

	@Override
	public final QuestFile getQuestFile() {
		return quest.chapter.file;
	}

	@Override
	@Nullable
	public final Chapter getQuestChapter() {
		return quest.chapter;
	}

	@Override
	public final long getParentID() {
		return quest.id;
	}

	public abstract RewardType getType();

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);

		if (team != Tristate.DEFAULT) {
			team.write(nbt, "team_reward");
		}

		if (autoclaim != RewardAutoClaim.DEFAULT) {
			nbt.putString("auto", autoclaim.id);
		}

		if (optional) nbt.putBoolean("optional", optional);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		team = Tristate.read(nbt, "team_reward");
		autoclaim = RewardAutoClaim.NAME_MAP.get(nbt.getString("auto"));
		optional = nbt.getBoolean("optional");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		Tristate.NAME_MAP.write(buffer, team);
		RewardAutoClaim.NAME_MAP.write(buffer, autoclaim);
		buffer.writeBoolean(optional);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		team = Tristate.NAME_MAP.read(buffer);
		autoclaim = RewardAutoClaim.NAME_MAP.read(buffer);
		optional = buffer.readBoolean();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addEnum("team", team, v -> team = v, Tristate.NAME_MAP).setNameKey("ftbquests.reward.team_reward");
		config.addEnum("autoclaim", autoclaim, v -> autoclaim = v, RewardAutoClaim.NAME_MAP).setNameKey("ftbquests.reward.autoclaim");
		config.addBool("optional", optional, v -> optional = v, optional).setNameKey("ftbquests.reward.optional");
	}

	public abstract void claim(ServerPlayer player, boolean notify);

	/**
	 * @return Optional.empty() if this reward doesn't support auto-claiming or item can't be returned as single stack, Optional.of(ItemStack.EMPTY) if it did something, but doesn't return item
	 */
	public Optional<ItemStack> claimAutomated(BlockEntity tileEntity, UUID playerId, @Nullable ServerPlayer player, boolean simulate) {
		if (player != null) {
			if (!simulate) {
				claim(player, false);
			}

			return Optional.of(ItemStack.EMPTY);
		}

		return Optional.empty();
	}

	public boolean automatedClaimPre(BlockEntity tileEntity, List<ItemStack> items, Random random, UUID playerId, @Nullable ServerPlayer player) {
		return player != null;
	}

	public void automatedClaimPost(BlockEntity tileEntity, UUID playerId, @Nullable ServerPlayer player) {
		if (player != null) {
			claim(player, false);
		}
	}

	@Override
	public final void deleteSelf() {
		quest.rewards.remove(this);

		for (TeamData data : getQuestFile().getAllData()) {
			data.deleteReward(this);
		}

		super.deleteSelf();
	}

	@Override
	public final void deleteChildren() {
		for (TeamData data : getQuestFile().getAllData()) {
			data.deleteReward(this);
		}

		super.deleteChildren();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void editedFromGUI() {
		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

		if (gui != null && gui.isViewingQuest()) {
			gui.viewQuestPanel.refreshWidgets();
		}

		if (gui != null) {
			gui.questPanel.refreshWidgets();
		}
	}

	@Override
	public void onCreated() {
		quest.rewards.add(this);
	}

	public final boolean isTeamReward() {
		return team.get(quest.chapter.file.defaultRewardTeam);
	}

	public final RewardAutoClaim getAutoClaimType() {
		if (quest.chapter.alwaysInvisible && (autoclaim == RewardAutoClaim.DEFAULT || autoclaim == RewardAutoClaim.DISABLED)) {
			return RewardAutoClaim.ENABLED;
		}

		if (autoclaim == RewardAutoClaim.DEFAULT) {
			return quest.chapter.file.defaultRewardAutoClaim;
		}

		return autoclaim;
	}

	@Override
	public void forceProgress(TeamData teamData, ProgressChange progressChange) {
		if (progressChange.reset) {
			teamData.resetReward(progressChange.player, this);
		} else {
			teamData.claimReward(progressChange.player, this, progressChange.time.getTime());
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		return getType().getIcon();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Component getAltTitle() {
		return getType().getDisplayName();
	}

	@Override
	public final ConfigGroup createSubGroup(ConfigGroup group) {
		RewardType type = getType();
		return group.getGroup(getObjectType().id).getGroup(type.id.getNamespace()).getGroup(type.id.getPath());
	}

	@Environment(EnvType.CLIENT)
	public void addMouseOverText(TooltipList list) {
	}

	@Environment(EnvType.CLIENT)
	public boolean addTitleInMouseOverText() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
		if (canClick) {
			button.playClickSound();
			new ClaimRewardMessage(id, true).sendToServer();
		}
	}

	public boolean getExcludeFromClaimAll() {
		return optional;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public Object getIngredient() {
		return getIcon().getIngredient();
	}

	@Override
	public final int refreshJEI() {
		return FTBQuestsJEIHelper.QUESTS;
	}

	@Environment(EnvType.CLIENT)
	public String getButtonText() {
		return "";
	}
}