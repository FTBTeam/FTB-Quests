package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftbquests.api.event.CustomRewardEvent;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.server.level.ServerPlayer;

public class CustomReward extends Reward {
	public CustomReward(long id, Quest quest) {
		super(id, quest);
	}

	@Override
	public RewardType getType() {
		return RewardTypes.CUSTOM;
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		NativeEventPosting.get().postEvent(new CustomRewardEvent.Data(this, player, notify));
	}
}