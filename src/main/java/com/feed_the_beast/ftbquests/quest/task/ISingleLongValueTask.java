package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigLong;

/**
 * @author LatvianModder
 */
public interface ISingleLongValueTask
{
	ConfigLong getDefaultValue();

	void setValue(long value);
}