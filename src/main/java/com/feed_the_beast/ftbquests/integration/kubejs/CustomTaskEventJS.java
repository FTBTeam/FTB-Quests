package com.feed_the_beast.ftbquests.integration.kubejs;

import dev.latvian.kubejs.events.EventJS;

/**
 * @author LatvianModder
 */
public class CustomTaskEventJS extends EventJS
{
	public CustomTaskCheckerJS check;

	@Override
	public boolean canCancel()
	{
		return true;
	}
}