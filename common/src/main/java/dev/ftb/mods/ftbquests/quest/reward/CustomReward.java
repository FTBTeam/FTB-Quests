package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftbquests.events.CustomRewardEvent;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class CustomReward extends Reward {
	public CustomReward(Quest quest) {
		super(quest);
	}

	@Override
	public RewardType getType() {
		return RewardTypes.CUSTOM;
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		CustomRewardEvent.EVENT.invoker().act(new CustomRewardEvent(this, player, notify));
	}
}