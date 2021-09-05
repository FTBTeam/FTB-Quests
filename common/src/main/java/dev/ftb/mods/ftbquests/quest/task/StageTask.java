package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.integration.StageHelper;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import me.shedaniel.architectury.hooks.PlayerHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class StageTask extends BooleanTask {
	public String stage = "";

	public StageTask(Quest quest) {
		super(quest);
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
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addString("stage", stage, v -> stage = v, "").setNameKey("ftbquests.task.ftbquests.gamestage");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return new TranslatableComponent("ftbquests.task.ftbquests.gamestage").append(": ").append(new TextComponent(stage).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 20;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		return StageHelper.instance.get().has(player, stage);
	}

	public static void checkStages(ServerPlayer player) {
		TeamData data = ServerQuestFile.INSTANCE == null || PlayerHooks.isFake(player) ? null : ServerQuestFile.INSTANCE.getData(player);

		if (data == null || data.isLocked()) {
			return;
		}

		ServerQuestFile.INSTANCE.currentPlayer = player;

		for (Task task : ServerQuestFile.INSTANCE.getAllTasks()) {
			if (task instanceof StageTask && data.canStartTasks(task.quest)) {
				task.submitTask(data, player);
			}
		}

		ServerQuestFile.INSTANCE.currentPlayer = null;
	}
}