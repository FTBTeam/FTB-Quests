package dev.ftb.mods.ftbquests.integration.gamestages;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.BooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public class GameStageTask extends BooleanTask {
	public String stage = "";

	public GameStageTask(Quest quest) {
		super(quest);
	}

	@Override
	public TaskType getType() {
		return GameStagesIntegration.GAMESTAGE_TASK;
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
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addString("stage", stage, v -> stage = v, "").setNameKey("ftbquests.task.ftbquests.gamestage");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public MutableComponent getAltTitle() {
		return new TranslatableComponent("ftbquests.task.ftbquests.gamestage").append(": ").append(new TextComponent(stage).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 20;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		return GameStagesIntegration.hasStage(player, stage);
	}
}