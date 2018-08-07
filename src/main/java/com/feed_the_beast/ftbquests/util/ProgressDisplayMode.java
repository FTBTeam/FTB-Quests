package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author LatvianModder
 */
public enum ProgressDisplayMode implements IStringSerializable
{
	PROGRESS("progress"),
	PERCENT("percent"),
	BAR("bar");

	public static final NameMap<ProgressDisplayMode> NAME_MAP = NameMap.create(PROGRESS, NameMap.ObjectProperties.withName((sender, value) -> new TextComponentTranslation(value.langKey)), values());

	private final String name;
	private final String langKey;

	ProgressDisplayMode(String n)
	{
		name = n;
		langKey = "tile.ftbquests.screen.progress_display_mode." + name;
	}

	@Override
	public String getName()
	{
		return name;
	}
}