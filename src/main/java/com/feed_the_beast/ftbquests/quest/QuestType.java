package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import net.minecraft.util.IStringSerializable;

/**
 * @author LatvianModder
 */
public enum QuestType implements IStringSerializable
{
	NORMAL("normal"),
	SECRET("secret"),
	INVISIBLE("invisible");

	public static final NameMap<QuestType> NAME_MAP = NameMap.create(NORMAL, values());

	public final String name;

	QuestType(String n)
	{
		name = n;
	}

	@Override
	public String getName()
	{
		return name;
	}
}