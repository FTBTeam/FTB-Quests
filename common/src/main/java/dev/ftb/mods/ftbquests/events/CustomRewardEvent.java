package dev.ftb.mods.ftbquests.events;

import dev.ftb.mods.ftbquests.quest.reward.CustomReward;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public interface CustomRewardEvent extends Consumer<CustomRewardEvent.Data> {
//	public static final Event<EventActor<CustomRewardEvent>> EVENT = EventFactory.createEventActorLoop();

	record Data(CustomReward reward, ServerPlayer player, boolean shouldNotify) {
	}

//	private final CustomReward reward;
//	private final ServerPlayer player;
//	private final boolean notify;
//
//	public CustomRewardEvent(CustomReward reward, ServerPlayer player, boolean notify) {
//		this.reward = reward;
//		this.player = player;
//		this.notify = notify;
//	}
//
//	public CustomReward getReward() {
//		return reward;
//	}
//
//	public ServerPlayer getPlayer() {
//		return player;
//	}
//
//	public boolean getNotify() {
//		return notify;
//	}
}