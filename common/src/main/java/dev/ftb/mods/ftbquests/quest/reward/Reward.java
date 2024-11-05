package dev.ftb.mods.ftbquests.quest.reward;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.net.ClaimRewardMessage;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author LatvianModder
 */
public abstract class Reward extends QuestObjectBase {
	protected final Quest quest;

	private Tristate team;
	protected RewardAutoClaim autoclaim;
	private boolean excludeFromClaimAll;
	private boolean ignoreRewardBlocking;
	protected boolean disableRewardScreenBlur;

	public Reward(long id, Quest q) {
		super(id);

		quest = q;
		team = Tristate.DEFAULT;
		autoclaim = RewardAutoClaim.DEFAULT;
		excludeFromClaimAll = getType().getExcludeFromListRewards();
		ignoreRewardBlocking = false;
		disableRewardScreenBlur = false;
	}

	public Quest getQuest() {
		return quest;
	}

	@Override
	public final QuestObjectType getObjectType() {
		return QuestObjectType.REWARD;
	}

	@Override
	public final BaseQuestFile getQuestFile() {
		return quest.getChapter().file;
	}

	@Override
	@Nullable
	public final Chapter getQuestChapter() {
		return quest.getChapter();
	}

	@Override
	public final long getParentID() {
		return quest.id;
	}

	public abstract RewardType getType();

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);

		if (team != Tristate.DEFAULT) {
			team.write(nbt, "team_reward");
		}

		if (autoclaim != RewardAutoClaim.DEFAULT) {
			nbt.putString("auto", autoclaim.id);
		}

		if (excludeFromClaimAll) nbt.putBoolean("exclude_from_claim_all", true);
		if (ignoreRewardBlocking) nbt.putBoolean("ignore_reward_blocking", true);
		if (disableRewardScreenBlur) nbt.putBoolean("disable_reward_screen_blur", true);
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		team = Tristate.read(nbt, "team_reward");
		autoclaim = RewardAutoClaim.NAME_MAP.get(nbt.getString("auto"));
		excludeFromClaimAll = nbt.getBoolean("exclude_from_claim_all");
		ignoreRewardBlocking = nbt.getBoolean("ignore_reward_blocking");
		disableRewardScreenBlur	= nbt.getBoolean("disable_reward_screen_blur");
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		Tristate.NAME_MAP.write(buffer, team);
		RewardAutoClaim.NAME_MAP.write(buffer, autoclaim);
		buffer.writeBoolean(excludeFromClaimAll);
		buffer.writeBoolean(ignoreRewardBlocking);
		buffer.writeBoolean(disableRewardScreenBlur);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		team = Tristate.NAME_MAP.read(buffer);
		autoclaim = RewardAutoClaim.NAME_MAP.read(buffer);
		excludeFromClaimAll = buffer.readBoolean();
		ignoreRewardBlocking = buffer.readBoolean();
		disableRewardScreenBlur = buffer.readBoolean();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addEnum("team", team, v -> team = v, Tristate.NAME_MAP)
				.setNameKey("ftbquests.reward.team_reward");
		config.addEnum("autoclaim", autoclaim, v -> autoclaim = v, RewardAutoClaim.NAME_MAP)
				.setNameKey("ftbquests.reward.autoclaim");
		config.addBool("exclude_from_claim_all", getExcludeFromClaimAll(), v -> excludeFromClaimAll = v, excludeFromClaimAll)
				.setNameKey("ftbquests.reward.exclude_from_claim_all")
				.setCanEdit(!isClaimAllHardcoded());
		config.addBool("ignore_reward_blocking", ignoreRewardBlocking(), v -> ignoreRewardBlocking = v, ignoreRewardBlocking)
				.setNameKey("ftbquests.quest.misc.ignore_reward_blocking")
				.setCanEdit(!isIgnoreRewardBlockingHardcoded());
		config.addBool("disable_reward_screen_blur", disableRewardScreenBlur, v -> disableRewardScreenBlur = v, false)
				.setNameKey("ftbquests.reward.disable_reward_screen_blur");
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
		quest.removeReward(this);

		for (TeamData data : getQuestFile().getAllTeamData()) {
			data.deleteReward(this);
		}

		super.deleteSelf();
	}

	@Override
	public final void deleteChildren() {
		for (TeamData data : getQuestFile().getAllTeamData()) {
			data.deleteReward(this);
		}

		super.deleteChildren();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void editedFromGUI() {
		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
		if (gui != null) {
			gui.refreshQuestPanel();
			if (gui.isViewingQuest()) gui.refreshViewQuestPanel();
		}
	}

	@Override
	public void onCreated() {
		quest.addReward(this);
	}

	public final boolean isTeamReward() {
		return team.get(quest.getQuestFile().isDefaultPerTeamReward());
	}

	public final RewardAutoClaim getAutoClaimType() {
		if (quest.getChapter().isAlwaysInvisible() && (autoclaim == RewardAutoClaim.DEFAULT || autoclaim == RewardAutoClaim.DISABLED)) {
			return RewardAutoClaim.ENABLED;
		}

		if (autoclaim == RewardAutoClaim.DEFAULT) {
			return quest.getQuestFile().getDefaultRewardAutoClaim();
		}

		return autoclaim;
	}

	@Override
	public final void forceProgress(TeamData teamData, ProgressChange progressChange) {
		if (progressChange.shouldReset()) {
			teamData.resetReward(progressChange.getPlayerId(), this);
		} else {
			teamData.claimReward(progressChange.getPlayerId(), this, progressChange.getDate().getTime());
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		return getType().getIconSupplier();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Component getAltTitle() {
		return getType().getDisplayName();
	}

	@Override
	public final ConfigGroup createSubGroup(ConfigGroup group) {
		RewardType type = getType();
		return group.getOrCreateSubgroup(getObjectType().getId())
				.getOrCreateSubgroup(type.getTypeId().getNamespace())
				.getOrCreateSubgroup(type.getTypeId().getPath());
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
			NetworkManager.sendToServer(new ClaimRewardMessage(id, true));
		}
	}

	public boolean getExcludeFromClaimAll() {
		return excludeFromClaimAll;
	}

	public boolean isClaimAllHardcoded() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public Optional<PositionedIngredient> getIngredient(Widget widget) {
		return PositionedIngredient.of(getIcon().getIngredient(), widget);
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