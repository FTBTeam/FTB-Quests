package dev.ftb.mods.ftbquests.events;

import dev.ftb.mods.ftbquests.quest.reward.CustomReward;
import me.shedaniel.architectury.ForgeEvent;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventActor;
import me.shedaniel.architectury.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
@ForgeEvent
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