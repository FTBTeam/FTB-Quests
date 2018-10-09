package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public enum LootRarity implements IStringSerializable
{
	COMMON("common", TextFormatting.WHITE),
	UNCOMMON("uncommon", TextFormatting.DARK_GREEN),
	RARE("rare", TextFormatting.BLUE),
	EPIC("epic", TextFormatting.DARK_PURPLE),
	LEGENDARY("legendary", TextFormatting.GOLD);

	public static final LootRarity[] VALUES = values();

	private final String name;
	private final TextFormatting color;
	private final String translationKey;
	private final ResourceLocation lootTable;

	LootRarity(String n, TextFormatting c)
	{
		name = n;
		color = c;
		translationKey = "ftbquests.rarity." + name;
		lootTable = new ResourceLocation(FTBQuests.MOD_ID, "lootcrates/" + name);
	}

	@Override
	public String getName()
	{
		return name;
	}

	public TextFormatting getColor()
	{
		return color;
	}

	public String getTranslationKey()
	{
		return translationKey;
	}

	public ResourceLocation getLootTable()
	{
		return lootTable;
	}
}