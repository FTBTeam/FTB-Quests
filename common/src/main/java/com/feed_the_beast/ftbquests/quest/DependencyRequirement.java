package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;

/**
 * @author LatvianModder
 */
public enum DependencyRequirement {
	ALL_COMPLETED("all_completed", false, true),
	ONE_COMPLETED("one_completed", true, true),
	ALL_STARTED("all_started", false, false),
	ONE_STARTED("one_started", true, false);

	public static final NameMap<DependencyRequirement> NAME_MAP = NameMap.of(ALL_COMPLETED, values()).id(v -> v.id).baseNameKey("ftbquests.quest.dependency_requirement").create();

	public final String id;
	public final boolean one;
	public final boolean completed;

	DependencyRequirement(String s, boolean o, boolean c) {
		id = s;
		one = o;
		completed = c;
	}
}