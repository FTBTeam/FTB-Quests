package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import net.minecraft.util.IStringSerializable;

/**
 * @author LatvianModder
 */
public enum EnumQuestVisibilityType implements IStringSerializable
{
	NORMAL("normal"),
	SECRET_ONE("secret_one"),
	SECRET_ALL("secret_all"),
	INVISIBLE_ONE("invisible_one"),
	INVISIBLE_ALL("invisible_all");

	public static final NameMap<EnumQuestVisibilityType> NAME_MAP = NameMap.create(NORMAL, values());

	public final String name;

	EnumQuestVisibilityType(String n)
	{
		name = n;
	}

	@Override
	public String getName()
	{
		return name;
	}
}