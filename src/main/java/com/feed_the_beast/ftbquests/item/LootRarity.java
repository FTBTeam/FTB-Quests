package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public enum LootRarity implements IWithID
{
	COMMON("common", TextFormatting.WHITE),
	UNCOMMON("uncommon", TextFormatting.DARK_GREEN),
	RARE("rare", TextFormatting.BLUE),
	EPIC("epic", TextFormatting.DARK_PURPLE),
	LEGENDARY("legendary", TextFormatting.GOLD);

	public static final LootRarity[] VALUES = values();

	private final String id;
	private final TextFormatting color;
	private final String translationKey;
	private final ResourceLocation lootTable;

	LootRarity(String n, TextFormatting c)
	{
		id = n;
		color = c;
		translationKey = "ftbquests.rarity." + id;
		lootTable = new ResourceLocation(FTBQuests.MOD_ID, "lootcrates/" + id);
	}

	@Override
	public String getID()
	{
		return id;
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

	public Item getItem()
	{
		switch (this)
		{
			case UNCOMMON:
				return FTBQuestsItems.UNCOMMON_LOOTCRATE;
			case RARE:
				return FTBQuestsItems.RARE_LOOTCRATE;
			case EPIC:
				return FTBQuestsItems.EPIC_LOOTCRATE;
			case LEGENDARY:
				return FTBQuestsItems.LEGENDARY_LOOTCRATE;
		}

		return FTBQuestsItems.COMMON_LOOTCRATE;
	}
}