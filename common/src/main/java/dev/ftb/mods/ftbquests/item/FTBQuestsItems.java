package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.block.FTBQuestsBlocks;
import me.shedaniel.architectury.registry.DeferredRegister;
import me.shedaniel.architectury.registry.RegistrySupplier;
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

	public static final RegistrySupplier<Item> BOOK = ITEMS.register("book", QuestBookItem::new);
	public static final RegistrySupplier<Item> LOOTCRATE = ITEMS.register("lootcrate", LootCrateItem::new);
	public static final RegistrySupplier<Item> MISSING_ITEM = ITEMS.register("missing_item", MissingItem::new);
	public static final RegistrySupplier<Item> CUSTOM_ICON = ITEMS.register("custom_icon", CustomIconItem::new);

	private static RegistrySupplier<Item> blockItem(String id, Supplier<Block> b) {
		return ITEMS.register(id, () -> new BlockItem(b.get(), new Item.Properties().tab(FTBQuests.ITEM_GROUP)));
	}

	public static final RegistrySupplier<Item> BANNER = blockItem("banner", FTBQuestsBlocks.BANNER);
	public static final RegistrySupplier<Item> BARRIER = ITEMS.register("barrier", QuestBarrierBlockItem::new);
	public static final RegistrySupplier<Item> STAGE_BARRIER = ITEMS.register("stage_barrier", StageBarrierBlockItem::new);
	public static final RegistrySupplier<Item> DETECTOR = blockItem("detector", FTBQuestsBlocks.DETECTOR);

	public static void register() {
		ITEMS.register();
	}
}