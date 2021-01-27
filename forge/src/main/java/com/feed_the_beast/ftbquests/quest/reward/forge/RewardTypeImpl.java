package com.feed_the_beast.ftbquests.quest.reward.forge;

import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import me.shedaniel.architectury.registry.Registry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.RegistryManager;

public class RewardTypeImpl
{
	public static void postRegistryEvent(Registry<RewardType> REGISTRY)
	{
		MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register(REGISTRY.key().location(), RegistryManager.ACTIVE.getRegistry(REGISTRY.key().location())));
	}
}
