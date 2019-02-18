package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;

/**
 * @author LatvianModder
 */
public enum EnumDependencyType implements IWithID
{
	REQUIRED("required", (data, object) -> object.isComplete(data)),
	DISABLE("disable", (data, object) -> object.isComplete(data)),
	HIDE("hide", (data, object) -> object.isComplete(data)),
	NOT_REQUIRED("not_required", (data, object) -> true),
	ENABLE("enable", (data, object) -> !object.isComplete(data)),
	SHOW("show", (data, object) -> !object.isComplete(data));

	public static final NameMap<EnumDependencyType> NAME_MAP = NameMap.create(REQUIRED, values());

	public final String name;
	public final DependencyCheck checkFunction;

	EnumDependencyType(String n, DependencyCheck f)
	{
		name = n;
		checkFunction = f;
	}

	@Override
	public String getID()
	{
		return name;
	}
}