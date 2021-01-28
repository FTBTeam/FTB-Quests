package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftbquests.FTBQuests;
import me.shedaniel.architectury.registry.DeferredRegister;
import me.shedaniel.architectury.registry.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

/**
 * @author LatvianModder
 */
public class FTBQuestsItems
{
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(FTBQuests.MOD_ID, Registry.ITEM_REGISTRY);
	public static final RegistrySupplier<Item> BOOK = ITEMS.register("book", ItemQuestBook::new);
	public static final RegistrySupplier<Item> LOOTCRATE = ITEMS.register("lootcrate", ItemLootCrate::new);
	public static final RegistrySupplier<Item> MISSING_ITEM = ITEMS.register("missing_item", MissingItem::new);

	public static void register()
	{
		ITEMS.register();
	}
}