package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.events.CustomRewardEvent;
import dev.ftb.mods.ftbquests.quest.reward.CustomReward;
import dev.latvian.mods.kubejs.entity.EntityJS;
import dev.latvian.mods.kubejs.player.PlayerEventJS;

/**
 * @author LatvianModder
 */
public class CustomRewardEventJS extends PlayerEventJS {
	public final transient CustomRewardEvent event;

	public CustomRewardEventJS(CustomRewardEvent e) {
		event = e;
	}

	@Override
	public boolean canCancel() {
		return true;
	}

	@Override
	public EntityJS getEntity() {
		return entityOf(event.getPlayer());
	}

	public CustomReward getReward() {
		return event.getReward();
	}

	public boolean getNotify() {
		return event.getNotify();
	}
}