package com.feed_the_beast.ftbquests.integration.kubejs;

import dev.latvian.kubejs.event.EventJS;

/**
 * @author LatvianModder
 */
public class CustomTaskEventJS extends EventJS
{
	public CustomTaskCheckerJS check;
	public int checkTimer = 1;

	@Override
	public boolean canCancel()
	{
		return true;
	}
}