package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author LatvianModder
 */
public enum RedstoneOutputMode implements IStringSerializable
{
	DISABLED("disabled"),
	ENABLED("enabled"),
	LEVEL("level");

	public static final NameMap<RedstoneOutputMode> NAME_MAP = NameMap.create(DISABLED, NameMap.ObjectProperties.withName((sender, value) -> new TextComponentTranslation(value.langKey)), values());

	private final String name;
	private final String langKey;

	RedstoneOutputMode(String n)
	{
		name = n;
		langKey = "tile.ftbquests.screen.redstone_output_mode." + name;
	}

	@Override
	public String getName()
	{
		return name;
	}
}