package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;

/**
 * @author MasterOfBob777
 */
public class RepeatReward extends Reward {
	public int maxRepeats;


	public RepeatReward(Quest quest) {
		super(quest);
		autoclaim = RewardAutoClaim.INVISIBLE;
	}

	@Override
	public RewardType getType() {
		return RewardTypes.REPEAT;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putInt("maxRepeats", maxRepeats);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		maxRepeats = nbt.getInt("maxRepeats");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeInt(maxRepeats);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		maxRepeats = buffer.readInt();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addInt("maxRepeats", maxRepeats, v -> maxRepeats = v, 0, 0, Integer.MAX_VALUE);
	}

	@Override
	public void forceProgress(TeamData teamData, ProgressChange progressChange) {
		super.forceProgress(teamData, progressChange);
		if (progressChange.reset && progressChange.origin != this) {
			teamData.getOnlineMembers().forEach(player -> {
				CompoundTag extra = FTBTeamsAPI.getPlayerTeam(player).getExtraData();
				String timesCompletedKey = "ftbquests:repeat/times_completed?" + quest.id;
				extra.putInt(timesCompletedKey, 0);
			});
		}
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		ProgressChange progressChange = new ProgressChange(ServerQuestFile.INSTANCE);
		progressChange.origin = this;
		progressChange.reset = true;
		progressChange.player = player.getUUID();

		CompoundTag extra = FTBTeamsAPI.getPlayerTeam(player).getExtraData();
		String timesCompletedKey = "ftbquests:repeat/times_completed?" + quest.id;
		int timesCompleted = extra.getInt(timesCompletedKey);

		// String lastCompletedKey = "ftbquests:repeat/last_completed?" + quest.id;
		// long lastCompleted = extra.getLong(lastCompletedKey);

		if (maxRepeats != 0 && timesCompleted < maxRepeats - 1) {
			extra.putInt(timesCompletedKey, timesCompleted + 1);

			TeamData teamData = ServerQuestFile.INSTANCE.getData(player);
			
			for (Reward reward : quest.rewards) {
				reward.forceProgress(teamData, progressChange);
			}
	
			for (Task task : quest.tasks) {
				task.forceProgress(teamData, progressChange);
			}
			
		}
		
		if (notify) {
			player.sendMessage(new TranslatableComponent("ftbquests.reward.ftbquests.repeat"), Util.NIL_UUID);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return new TranslatableComponent("ftbquests.reward.ftbquests.repeat");
	}
}
