package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;

/**
 * @author LatvianModder
 */
public enum RewardAutoClaim implements IWithID
{
	DEFAULT("default"),
	DISABLED("disabled"),
	ENABLED("enabled"),
	NO_TOAST("no_toast"),
	INVISIBLE("invisible");

	public static final NameMap<RewardAutoClaim> NAME_MAP = NameMap.createWithBaseTranslationKey(DEFAULT, "ftbquests.reward.autoclaim", values());
	public static final NameMap<RewardAutoClaim> NAME_MAP_NO_DEFAULT = NameMap.createWithBaseTranslationKey(DISABLED, "ftbquests.reward.autoclaim", DISABLED, ENABLED, NO_TOAST, INVISIBLE);

	private String id;

	RewardAutoClaim(String s)
	{
		id = s;
	}

	@Override
	public String getID()
	{
		return id;
	}
}