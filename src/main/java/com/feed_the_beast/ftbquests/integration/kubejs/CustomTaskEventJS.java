package com.feed_the_beast.ftbquests.integration.kubejs;

import dev.latvian.kubejs.documentation.DocClass;
import dev.latvian.kubejs.documentation.DocField;
import dev.latvian.kubejs.event.EventJS;

/**
 * @author LatvianModder
 */
@DocClass("Custom task check override event. You can use this to have custom condition combinations for quests")
public class CustomTaskEventJS extends EventJS
{
	@DocField("Check callback - function (player), returns boolean")
	public CustomTaskCheckerJS check;

	@DocField("How often in ticks the callback function should be checked")
	public int checkTimer = 1;

	@Override
	public boolean canCancel()
	{
		return true;
	}
}