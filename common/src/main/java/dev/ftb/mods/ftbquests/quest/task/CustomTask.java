package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.EditObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class CustomTask extends Task {
	public static final Predicate<QuestObjectBase> PREDICATE = object -> object instanceof CustomTask;

	private Check check;
	private int checkTimer;
	private long maxProgress;
	private boolean enableButton;

	public CustomTask(long id, Quest quest) {
		super(id, quest);

		check = null;
		checkTimer = 1;
		maxProgress = 1L;
		enableButton = false;
	}

	public void setCheck(Check check) {
		this.check = check;
	}

	@Override
	public TaskType getType() {
		return TaskTypes.CUSTOM;
	}

	@Override
	public long getMaxProgress() {
		return maxProgress;
	}

	public void setCheckTimer(int checkTimer) {
		if (this.checkTimer != checkTimer) {
			this.checkTimer = checkTimer;
			TaskSync.toSync.add(id);
		}
	}

	public void setMaxProgress(long maxProgress) {
		if (this.maxProgress != maxProgress) {
			this.maxProgress = maxProgress;
			TaskSync.toSync.add(id);
		}
	}

	public void setEnableButton(boolean enableButton) {
		if (this.enableButton != enableButton) {
			this.enableButton = enableButton;
			TaskSync.toSync.add(id);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
		if (enableButton && canClick) {
			button.playClickSound();
			new SubmitTaskMessage(id).sendToServer();
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
		if (check != null && checkTaskSequence(teamData) && !teamData.isCompleted(this)) {
			check.check(new Data(this, teamData), player);
		}
	}

	@Override
	public boolean checkOnLogin() {
		return false;
	}

	public record Data(CustomTask task, TeamData teamData) {
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

	@FunctionalInterface
	public interface Check {
		void check(CustomTask.Data taskData, ServerPlayer player);
	}

	public static class TaskSync {
		private static final LongSet toSync = new LongOpenHashSet();

		public static void tick(MinecraftServer server) {
			if (!toSync.isEmpty() && ServerQuestFile.INSTANCE != null) {
				toSync.forEach(id -> {
					if (ServerQuestFile.INSTANCE.get(id) instanceof CustomTask c) {
						new EditObjectResponseMessage(c).sendToAll(server);
					}
                });
				toSync.clear();
			}
		}
	}
}