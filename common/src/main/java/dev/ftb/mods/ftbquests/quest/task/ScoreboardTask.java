package dev.ftb.mods.ftbquests.quest.task;

import java.util.Collection;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;

/**
 * @author MasterOfBob777
 */
public class ScoreboardTask extends Task {
	public String name;
	public int value;
	private String objective;

	public ScoreboardTask(Quest quest) {
		super(quest);
	} 

	@Override
	public long getMaxProgress() {
		return 1L;
	}

	@Override
	public TaskType getType() {
		return TaskTypes.SCOREBOARD;
	}

	@Override
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addString("objective", objective, v -> objective = v, "");
		config.addInt("value", value, v -> value = v, 0, 0, Integer.MAX_VALUE);
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("objective", objective);
		nbt.putInt("value", value);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		objective = nbt.getString("objective");
		value = nbt.getInt("value");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(objective);
		buffer.writeInt(value);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		objective = buffer.readUtf();
		value = buffer.readInt();
	}

	@Override
	public boolean canExclusive() {
		return true;
	}

	private int lastVal = -1;

	public void test(ServerQuestFile file, ServerScoreboard scoreboard) {
		Objective obj = scoreboard.getObjective(objective);
		Collection<Score> scores = scoreboard.getPlayerScores(obj);

		for (Score score : scores) {
			ServerPlayer player = file.server.getPlayerList().getPlayerByName(score.getOwner());
			if (player == null) {
				continue;
			}
			TeamData data = file.getData(player);
			if (data.isLocked()) {
				return;
			}

			if (exclusive ? false : data.isCompleted(this)) { 
				continue;
			}
			
			int val = score.getScore();

			if (val >= value) {
				data.addProgress(this, 1L);
				lastVal = val;
			} else if (exclusive && val != lastVal){
				data.setProgress(this, 0L);
			}
		}
	}
}
