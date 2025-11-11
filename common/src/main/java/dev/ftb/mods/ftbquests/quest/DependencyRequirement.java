package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.config.NameMap;

public enum DependencyRequirement {
	ALL_COMPLETED("all_completed", false, true),
	ONE_COMPLETED("one_completed", true, true),
	ALL_STARTED("all_started", false, false),
	ONE_STARTED("one_started", true, false);

	public static final NameMap<DependencyRequirement> NAME_MAP = NameMap.of(ALL_COMPLETED, values()).id(v -> v.id).baseNameKey("ftbquests.quest.dependency_requirement").create();

	private final String id;
	private final boolean needOne;
	private final boolean completed;

	DependencyRequirement(String id, boolean needOne, boolean completed) {
		this.id = id;
		this.needOne = needOne;
		this.completed = completed;
	}

	public String getId() {
		return id;
	}

	public boolean needOnlyOne() {
		return needOne;
	}

	public boolean needCompletion() {
		return completed;
	}
}