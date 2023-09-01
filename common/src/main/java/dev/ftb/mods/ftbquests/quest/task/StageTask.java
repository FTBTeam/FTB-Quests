package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class StageTask extends AbstractBooleanTask {
	private String stage = "";

	public StageTask(long id, Quest quest) {
		super(id, quest);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.STAGE;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("stage", stage);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		stage = nbt.getString("stage");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(stage, Short.MAX_VALUE);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		stage = buffer.readUtf(Short.MAX_VALUE);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addString("stage", stage, v -> stage = v, "").setNameKey("ftbquests.task.ftbquests.gamestage");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.task.ftbquests.gamestage").append(": ").append(Component.literal(stage).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 20;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		return StageHelper.INSTANCE.getProvider().has(player, stage);
	}

	@SuppressWarnings("unused")
	public static void checkStages(ServerPlayer player) {
		// hook for FTB XMod Compat to call into

		TeamData data = ServerQuestFile.INSTANCE == null || PlayerHooks.isFake(player) ? null : ServerQuestFile.INSTANCE.getOrCreateTeamData(player);

		if (data == null || data.isLocked()) {
			return;
		}

		ServerQuestFile.INSTANCE.withPlayerContext(player, () -> {
			for (Task task : ServerQuestFile.INSTANCE.getAllTasks()) {
				if (task instanceof StageTask && data.canStartTasks(task.getQuest())) {
					task.submitTask(data, player);
				}
			}
		});
	}
}
