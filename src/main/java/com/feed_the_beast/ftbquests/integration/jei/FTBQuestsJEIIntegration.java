package com.feed_the_beast.ftbquests.integration.jei;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;

/**
 * @author LatvianModder
 */
@JEIPlugin
public class FTBQuestsJEIIntegration implements IModPlugin
{
	public static IJeiRuntime RUNTIME;

	@Override
	public void onRuntimeAvailable(IJeiRuntime r)
	{
		RUNTIME = r;
	}
}