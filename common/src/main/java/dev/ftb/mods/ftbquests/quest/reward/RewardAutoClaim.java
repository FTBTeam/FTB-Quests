package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftbguilibrary.config.NameMap;

import java.util.Arrays;

/**
 * @author LatvianModder
 */
public enum RewardAutoClaim {
	DEFAULT("default"),
	DISABLED("disabled"),
	ENABLED("enabled"),
	NO_TOAST("no_toast"),
	INVISIBLE("invisible");

	public static final NameMap<RewardAutoClaim> NAME_MAP = NameMap.of(DEFAULT, values()).baseNameKey("ftbquests.reward.autoclaim").create();
	public static final NameMap<RewardAutoClaim> NAME_MAP_NO_DEFAULT = NameMap.of(DISABLED, Arrays.asList(DISABLED, ENABLED, NO_TOAST, INVISIBLE)).baseNameKey("ftbquests.reward.autoclaim").create();

	public String id;

	RewardAutoClaim(String s) {
		id = s;
	}
}