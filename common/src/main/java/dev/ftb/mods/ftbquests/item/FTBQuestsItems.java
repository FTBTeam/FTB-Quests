package dev.ftb.mods.ftbquests.item;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.block.FTBQuestsBlocks;
import dev.ftb.mods.ftbquests.item.ScreenBlockItem.ScreenSize;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class FTBQuestsItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(FTBQuests.MOD_ID, Registry.ITEM_REGISTRY);

	private static RegistrySupplier<Item> blockItem(String id, Supplier<Block> b) {
		return ITEMS.register(id, () -> new BlockItem(b.get(), new Item.Properties().tab(FTBQuests.ITEM_GROUP)));
	}

	private static RegistrySupplier<Item> blockItemFor(String id, Supplier<BlockItem> bi) {
		return ITEMS.register(id, bi);
	}

	public static final RegistrySupplier<Item> BOOK = ITEMS.register("book", QuestBookItem::new);
	public static final RegistrySupplier<Item> LOOTCRATE = ITEMS.register("lootcrate", LootCrateItem::new);
	public static final RegistrySupplier<Item> MISSING_ITEM = ITEMS.register("missing_item", MissingItem::new);
	public static final RegistrySupplier<Item> CUSTOM_ICON = ITEMS.register("custom_icon", CustomIconItem::new);
	public static final RegistrySupplier<Item> TASK_SCREEN_CONFIGURATOR = ITEMS.register("task_screen_configurator", TaskScreenConfiguratorItem::new);

	public static final RegistrySupplier<Item> BARRIER = blockItemFor("barrier", QuestBarrierBlockItem::new);
	public static final RegistrySupplier<Item> STAGE_BARRIER = blockItemFor("stage_barrier", StageBarrierBlockItem::new);
	public static final RegistrySupplier<Item> DETECTOR = blockItem("detector", FTBQuestsBlocks.DETECTOR);
	public static final RegistrySupplier<Item> LOOT_CRATE_OPENER = blockItem("loot_crate_opener", FTBQuestsBlocks.LOOT_CRATE_OPENER);

	public static final RegistrySupplier<Item> TASK_SCREEN_1 = blockItemFor("screen_1",
			() -> new ScreenBlockItem(FTBQuestsBlocks.TASK_SCREEN_1.get(), ScreenSize.ONE_X_ONE));
	public static final RegistrySupplier<Item> TASK_SCREEN_3 = blockItemFor("screen_3",
			() -> new ScreenBlockItem(FTBQuestsBlocks.TASK_SCREEN_3.get(), ScreenSize.THREE_X_THREE));
	public static final RegistrySupplier<Item> TASK_SCREEN_5 = blockItemFor("screen_5",
			() -> new ScreenBlockItem(FTBQuestsBlocks.TASK_SCREEN_5.get(), ScreenSize.FIVE_X_FIVE));
	public static final RegistrySupplier<Item> TASK_SCREEN_7 = blockItemFor("screen_7",
			() -> new ScreenBlockItem(FTBQuestsBlocks.TASK_SCREEN_7.get(), ScreenSize.SEVEN_X_SEVEN));

	public static void register() {
		ITEMS.register();
	}
}