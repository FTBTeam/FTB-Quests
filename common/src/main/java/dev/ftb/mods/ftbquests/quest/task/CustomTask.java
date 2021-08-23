package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.net.SubmitTaskPacket;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class CustomTask extends Task {
	public static final Predicate<QuestObjectBase> PREDICATE = object -> object instanceof CustomTask;

	@FunctionalInterface
	public interface Check {
		void check(CustomTask.Data taskData, ServerPlayer player);
	}

	public Check check;
	public int checkTimer;
	public long maxProgress;
	public boolean enableButton;

	public CustomTask(Quest quest) {
		super(quest);
		check = null;
		checkTimer = 1;
		maxProgress = 1L;
		enableButton = false;
	}

	@Override
	public TaskType getType() {
		return TaskTypes.CUSTOM;
	}

	@Override
	public long getMaxProgress() {
		return maxProgress;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
		if (enableButton && canClick) {
			button.playClickSound();
			new SubmitTaskPacket(id).sendToServer();
		}
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return check == null ? 0 : checkTimer;
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeVarInt(checkTimer);
		buffer.writeVarLong(maxProgress);
		buffer.writeBoolean(enableButton);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		checkTimer = buffer.readVarInt();
		maxProgress = buffer.readVarLong();
		enableButton = buffer.readBoolean();
	}

	@Override
	public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem) {
		if (check != null && !teamData.isCompleted(this)) {
			check.check(new Data(this, teamData), player);
		}
	}

	@Override
	public boolean checkOnLogin() {
		return false;
	}

	public static class Data {
		public final CustomTask task;
		public final TeamData teamData;

		public Data(CustomTask t, TeamData d) {
			task = t;
			teamData = d;
		}

		public long getProgress() {
			return teamData.getProgress(task);
		}

		public void setProgress(long l) {
			teamData.setProgress(task, l);
		}

		public void addProgress(long l) {
			teamData.addProgress(task, l);
		}
	}
}