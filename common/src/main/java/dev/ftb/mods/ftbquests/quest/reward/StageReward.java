package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.TeamStagesHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class StageReward extends Reward {
	private String stage;
	private boolean remove = false;

	public StageReward(long id, Quest quest, String stage) {
		super(id, quest);
		this.stage = stage;
		autoclaim = RewardAutoClaim.INVISIBLE;
	}

	public StageReward(long id, Quest quest) {
		this(id, quest, "");
	}

	@Override
	public RewardType getType() {
		return RewardTypes.STAGE;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		nbt.putString("stage", stage);

		if (remove) {
			nbt.putBoolean("remove", true);
		}
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		stage = nbt.getString("stage").orElseThrow();
		remove = nbt.getBooleanOr("remove", false);
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(stage, Short.MAX_VALUE);
		buffer.writeBoolean(remove);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		stage = buffer.readUtf(Short.MAX_VALUE);
		remove = buffer.readBoolean();
	}

	@Override
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addString("stage", stage, v -> stage = v, "").setNameKey("ftbquests.reward.ftbquests.gamestage");
		config.addBool("remove", remove, v -> remove = v, false);
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		if (remove) {
			if (isTeamReward()) {
				FTBTeamsAPI.api().getManager().getTeamForPlayer(player)
						.ifPresent(team -> TeamStagesHelper.removeTeamStage(team, stage));
			} else {
				StageHelper.INSTANCE.getProvider().remove(player, stage);
			}
		} else {
			if (isTeamReward()) {
				FTBTeamsAPI.api().getManager().getTeamForPlayer(player)
						.ifPresent(team -> TeamStagesHelper.addTeamStage(team, stage));
			} else {
				StageHelper.INSTANCE.getProvider().add(player, stage);
			}
		}

		if (notify) {
			String key = (remove ? "removed" : "added") + (isTeamReward() ? "_team" : "");
			player.sendSystemMessage(Component.translatable("ftbquests.reward.ftbquests.gamestage." + key, stage), true);
		}
	}

	@Override
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.reward.ftbquests.gamestage").append(": ").append(Component.literal(stage).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public boolean ignoreRewardBlocking() {
		return true;
	}

	@Override
	protected boolean isIgnoreRewardBlockingHardcoded() {
		return true;
	}
}
