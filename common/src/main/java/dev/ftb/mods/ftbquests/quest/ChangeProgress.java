package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftbguilibrary.config.NameMap;

/**
 * @author LatvianModder
 */
public enum ChangeProgress {
	RESET("reset", true),
	COMPLETE("complete", false);

	public static final NameMap<ChangeProgress> NAME_MAP = NameMap.of(RESET, values()).id(v -> v.id).create();

	public final String id;
	public final boolean reset, complete;

	ChangeProgress(String n, boolean r) {
		id = n;
		reset = r;
		complete = !r;
	}
}