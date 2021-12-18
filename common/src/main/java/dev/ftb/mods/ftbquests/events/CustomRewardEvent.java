package dev.ftb.mods.ftbquests.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventActor;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftbquests.quest.reward.CustomReward;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class CustomRewardEvent {
	public static final Event<EventActor<CustomRewardEvent>> EVENT = EventFactory.createEventActorLoop();
	private final CustomReward reward;
	private final ServerPlayer player;
	private final boolean notify;

	public CustomRewardEvent(CustomReward r, ServerPlayer p, boolean n) {
		reward = r;
		player = p;
		notify = n;
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