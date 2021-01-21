package com.feed_the_beast.mods.ftbteams.impl.fabric;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class TeamImplImpl // we love this name
{
	public static Component newChatWithLinks(String message) {
		return new TextComponent(message);
	}
}
