package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.reward.CustomReward;
import me.shedaniel.architectury.ForgeEvent;
import me.shedaniel.architectury.event.Actor;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
@ForgeEvent
public class CustomRewardEvent {
	public static final Event<Actor<CustomRewardEvent>> EVENT = EventFactory.createActorLoop();
	private final CustomReward reward;
	private final ServerPlayer player;
	private final boolean notify;

	public CustomRewardEvent(CustomReward r, ServerPlayer p, boolean n) {
		reward = r;
		player = p;
		notify = n;
	}

	public boolean isCancelable() {
		return true;
	}

	public CustomReward getReward() {
		return reward;
	}

	public ServerPlayer getPlayer() {
		return player;
	}

	public boolean getNotify() {
		return notify;
	}
}