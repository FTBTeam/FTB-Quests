package dev.ftb.mods.ftbquests.quest.task;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import dev.architectury.hooks.level.entity.PlayerHooks;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.TeamStagesHelper;

public class StageTask extends AbstractBooleanTask {
	private String stage = "";
	private boolean teamStage = false;

	public StageTask(long id, Quest quest) {
		super(id, quest);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.STAGE;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		nbt.putString("stage", stage);
		if (teamStage) {
			nbt.putBoolean("team_stage", true);
		}
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		stage = nbt.getString("stage").orElseThrow();
		teamStage = nbt.getBooleanOr("team_stage", false);
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(stage, Short.MAX_VALUE);
		buffer.writeBoolean(teamStage);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		stage = buffer.readUtf(Short.MAX_VALUE);
		teamStage = buffer.readBoolean();
	}

	@Override
	public void fillConfigGroup(EditableConfigGroup config) {
		super.fillConfigGroup(config);
		config.addString("stage", stage, v -> stage = v, "").setNameKey("ftbquests.task.ftbquests.gamestage");
		config.addBool("team_stage", teamStage, v -> teamStage = v, false);
	}

	@Override
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.task.ftbquests.gamestage").append(": ").append(Component.literal(stage).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 20;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
        if (teamStage) {
			return FTBTeamsAPI.api().getManager().getTeamByID(teamData.getTeamId())
					.map(team -> TeamStagesHelper.hasTeamStage(team, stage))
					.orElse(false);
		} else {
			return StageHelper.INSTANCE.getProvider().has(player, stage);
		}
    }

	@SuppressWarnings("unused")
	public static void checkStages(ServerPlayer player) {
		// hook for FTB XMod Compat to call into
		if (PlayerHooks.isFake(player)) {
			return;
		}

		ServerQuestFile.ifExists(instance -> instance.getTeamData(player).ifPresent(data -> {
            if (!data.isLocked()) {
                instance.withPlayerContext(player, () -> {
                    for (Task task : ServerQuestFile.getInstance().getAllTasks()) {
                        if (task instanceof StageTask && data.canStartTasks(task.getQuest())) {
                            task.submitTask(data, player);
                        }
                    }
                });
            }
        }));
	}
}
