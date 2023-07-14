package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.util.ClientUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.net.ClaimRewardMessage;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public abstract class Reward extends QuestObjectBase {
	public final Quest quest;

	private Tristate team;
	protected RewardAutoClaim autoclaim;
	private boolean excludeFromClaimAll;
	private boolean ignoreRewardBlocking;

	public Reward(Quest q) {
		quest = q;
		team = Tristate.DEFAULT;
		autoclaim = RewardAutoClaim.DEFAULT;
		excludeFromClaimAll = getType().getExcludeFromListRewards();
		ignoreRewardBlocking = false;
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

		if (excludeFromClaimAll) nbt.putBoolean("exclude_from_claim_all", true);
		if (ignoreRewardBlocking) nbt.putBoolean("ignore_reward_blocking", true);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		team = Tristate.read(nbt, "team_reward");
		autoclaim = RewardAutoClaim.NAME_MAP.get(nbt.getString("auto"));
		excludeFromClaimAll = nbt.getBoolean("exclude_from_claim_all");
		ignoreRewardBlocking = nbt.getBoolean("ignore_reward_blocking");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		Tristate.NAME_MAP.write(buffer, team);
		RewardAutoClaim.NAME_MAP.write(buffer, autoclaim);
		buffer.writeBoolean(excludeFromClaimAll);
		buffer.writeBoolean(ignoreRewardBlocking);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		team = Tristate.NAME_MAP.read(buffer);
		autoclaim = RewardAutoClaim.NAME_MAP.read(buffer);
		excludeFromClaimAll = buffer.readBoolean();
		ignoreRewardBlocking = buffer.readBoolean();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addEnum("team", team, v -> team = v, Tristate.NAME_MAP).setNameKey("ftbquests.reward.team_reward");
		config.addEnum("autoclaim", autoclaim, v -> autoclaim = v, RewardAutoClaim.NAME_MAP).setNameKey("ftbquests.reward.autoclaim");
		config.addBool("exclude_from_claim_all", getExcludeFromClaimAll(), v -> excludeFromClaimAll = v, excludeFromClaimAll)
				.setNameKey("ftbquests.reward.exclude_from_claim_all").setCanEdit(!isClaimAllHardcoded());
		config.addBool("ignore_reward_blocking", ignoreRewardBlocking(), v -> ignoreRewardBlocking = v, ignoreRewardBlocking)
				.setNameKey("ftbquests.quest.ignore_reward_blocking").setCanEdit(!isIgnoreRewardBlockingHardcoded());
	}

	public abstract void claim(ServerPlayer player, boolean notify);

	/**
	 * Called by the Loot Crate Opener when it's about to open a crate. Can be overridden to add any itemstacks the
	 * crate would produce to the {@code items} list; items in this list will be stored in the loot crate opener.
	 *
	 * @param blockEntity the loot crate opener
	 * @param items list of items to add to
	 * @param random random value
	 * @param playerId UUID of the player who placed the loot crate opener
	 * @param player the player, may be null if not online
	 * @return true if the crate opening should proceed, false if not
	 */
	public boolean automatedClaimPre(BlockEntity blockEntity, List<ItemStack> items, RandomSource random, UUID playerId, @Nullable ServerPlayer player) {
		return player != null;
	}

	/**
	 * Called after a crate has been opened by the Loot Crate Opener. Default behaviour is to follow the reward's normal
	 * claim behaviour, but any items added to the {@code items} list in
	 * {@link #automatedClaimPre(BlockEntity, List, RandomSource, UUID, ServerPlayer)} must <strong>not</strong> be given to
	 * the player now!
	 *
	 * @param blockEntity the loot crate opener
	 * @param playerId UUID of the player who placed the loot crate opener
	 * @param player the player, may be null if not online
	 */
	public void automatedClaimPost(BlockEntity blockEntity, UUID playerId, @Nullable ServerPlayer player) {
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
	public final void forceProgress(TeamData teamData, ProgressChange progressChange) {
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
		return excludeFromClaimAll;
	}

	public boolean isClaimAllHardcoded() {
		return false;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public Object getIngredient() {
		return getIcon().getIngredient();
	}

    @Override
	public Set<RecipeModHelper.Components> componentsToRefresh() {
		return EnumSet.of(RecipeModHelper.Components.QUESTS);
	}

	@Environment(EnvType.CLIENT)
	public String getButtonText() {
		return "";
	}

	public boolean ignoreRewardBlocking() {
		return ignoreRewardBlocking;
	}

	protected boolean isIgnoreRewardBlockingHardcoded() {
		return false;
	}
}