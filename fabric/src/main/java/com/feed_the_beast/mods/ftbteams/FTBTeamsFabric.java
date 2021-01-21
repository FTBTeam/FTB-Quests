package com.feed_the_beast.mods.ftbteams;

import net.fabricmc.api.ModInitializer;

public class FTBTeamsFabric implements ModInitializer
{
	@Override
	public void onInitialize()
	{
		new FTBTeams().setup();
	}
}
