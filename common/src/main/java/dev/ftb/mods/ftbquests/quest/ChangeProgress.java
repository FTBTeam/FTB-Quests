package dev.ftb.mods.ftbquests.quest;

import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import com.feed_the_beast.mods.ftbguilibrary.config.Tristate;

/**
 * @author LatvianModder
 */
public enum ChangeProgress {
	RESET("reset", true, false),
	RESET_DEPS("reset_deps", true, true),
	COMPLETE("complete", false, false),
	COMPLETE_DEPS("complete_deps", false, true);

	public static final NameMap<ChangeProgress> NAME_MAP = NameMap.of(RESET, values()).id(v -> v.id).create();
	public static boolean sendUpdates = true;
	public static Tristate sendNotifications = Tristate.DEFAULT;

	public final String id;
	public final boolean reset, complete, dependencies;

	ChangeProgress(String n, boolean r, boolean d) {
		id = n;
		reset = r;
		complete = !r;
		dependencies = d;
	}
}