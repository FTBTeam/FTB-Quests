package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author LatvianModder
 */
public enum EnumVisibility implements IWithID
{
	VISIBLE("visible", 2),
	SECRET("secret", 1),
	INVISIBLE("invisible", 0),
	INTERNAL("internal", -1);

	public static final NameMap<EnumVisibility> NAME_MAP = NameMap.create(VISIBLE, NameMap.ObjectProperties.withName((sender, o) -> new TextComponentTranslation(o.langKey)), values());

	private final String id;
	private final String langKey;
	public final int visibility;

	EnumVisibility(String s, int v)
	{
		id = s;
		langKey = "ftbquests.quest.visibility." + id;
		visibility = v;
	}

	@Override
	public String getID()
	{
		return id;
	}

	public boolean isVisible()
	{
		return visibility >= VISIBLE.visibility;
	}

	public boolean isInvisible()
	{
		return visibility <= INVISIBLE.visibility;
	}

	public EnumVisibility strongest(EnumVisibility other)
	{
		return visibility <= other.visibility ? this : other;
	}
}