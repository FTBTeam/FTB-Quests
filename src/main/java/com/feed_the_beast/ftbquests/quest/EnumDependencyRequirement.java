package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author LatvianModder
 */
public enum EnumDependencyRequirement implements IWithID
{
	ALL_COMPLETED("all_completed", false, true),
	ONE_COMPLETED("one_completed", true, true),
	ALL_STARTED("all_started", false, false),
	ONE_STARTED("one_started", true, false);

	public static final NameMap<EnumDependencyRequirement> NAME_MAP = NameMap.create(ALL_COMPLETED, NameMap.ObjectProperties.withName((sender, o) -> new TextComponentTranslation(o.langKey)), values());

	private final String id;
	private final String langKey;
	public final boolean one;
	public final boolean completed;

	EnumDependencyRequirement(String s, boolean o, boolean c)
	{
		id = s;
		langKey = "ftbquests.quest.dependency_requirement." + id;
		one = o;
		completed = c;
	}

	@Override
	public String getID()
	{
		return id;
	}
}