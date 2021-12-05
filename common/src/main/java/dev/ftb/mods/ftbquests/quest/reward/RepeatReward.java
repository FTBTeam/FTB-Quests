package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.ProgressChange;

/**
 * @author MasterOfBob777
 */
public class RepeatReward extends Reward {
	public RepeatReward(Quest quest) {
		super(quest);
		autoclaim = RewardAutoClaim.INVISIBLE;
	}

	@Override
	public RewardType getType() {
		return RewardTypes.REPEAT;
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		ProgressChange progressChange = new ProgressChange(ServerQuestFile.INSTANCE);
		progressChange.origin = quest;
		progressChange.reset = true;
		progressChange.player = player.getUUID();

		TeamData teamData = ServerQuestFile.INSTANCE.getData(player);

		for (Reward reward : quest.rewards) {
			reward.forceProgress(teamData, progressChange);
		}

		for (Task task : quest.tasks) {
			task.forceProgress(teamData, progressChange);
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
