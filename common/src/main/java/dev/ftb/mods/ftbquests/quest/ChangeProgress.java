package dev.ftb.mods.ftbquests.quest;

import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import com.feed_the_beast.mods.ftbguilibrary.config.Tristate;

/**
 * @author LatvianModder
 */
public enum ChangeProgress {
	RESET("reset", true),
	COMPLETE("complete", false);

	public static final NameMap<ChangeProgress> NAME_MAP = NameMap.of(RESET, values()).id(v -> v.id).create();
	public static boolean sendUpdates = true;
	public static Tristate sendNotifications = Tristate.DEFAULT;

	public final String id;
	public final boolean reset, complete;

	ChangeProgress(String n, boolean r) {
		id = n;
		reset = r;
		complete = !r;
	}
}