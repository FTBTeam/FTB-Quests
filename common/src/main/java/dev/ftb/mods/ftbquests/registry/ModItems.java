package dev.ftb.mods.ftbquests.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.item.*;
import dev.ftb.mods.ftbquests.item.ScreenBlockItem.ScreenSize;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(FTBQuestsAPI.MOD_ID, Registries.ITEM);

	private static RegistrySupplier<Item> blockItem(String id, Supplier<Block> b) {
		return register(id, (resourceKey) -> new BlockItem(b.get(), defaultProps().setId(resourceKey)));
	}

	private static RegistrySupplier<Item> blockItemFor(String id, Function<ResourceKey<Item>, BlockItem> bi) {
		return register(id, bi);
	}

	public static final RegistrySupplier<Item> BOOK = register("book", QuestBookItem::new);
	public static final RegistrySupplier<Item> LOOTCRATE = register("lootcrate", LootCrateItem::new);
	public static final RegistrySupplier<Item> TASK_SCREEN_CONFIGURATOR = register("task_screen_configurator", TaskScreenConfiguratorItem::new);

	public static final RegistrySupplier<Item> MISSING_ITEM = register("missing_item", MissingItem::new);
	public static final RegistrySupplier<Item> CUSTOM_ICON = register("custom_icon", CustomIconItem::new);

	public static final RegistrySupplier<Item> BARRIER = blockItemFor("barrier", QuestBarrierBlockItem::new);
	public static final RegistrySupplier<Item> STAGE_BARRIER = blockItemFor("stage_barrier", StageBarrierBlockItem::new);
	public static final RegistrySupplier<Item> DETECTOR = blockItem("detector", ModBlocks.DETECTOR);
	public static final RegistrySupplier<Item> LOOT_CRATE_OPENER = blockItem("loot_crate_opener", ModBlocks.LOOT_CRATE_OPENER);

	public static final RegistrySupplier<Item> TASK_SCREEN_1 = blockItemFor("screen_1",
			(id) -> new ScreenBlockItem(id, ModBlocks.TASK_SCREEN_1.get(), ScreenSize.ONE_X_ONE));
	public static final RegistrySupplier<Item> TASK_SCREEN_3 = blockItemFor("screen_3",
			(id) -> new ScreenBlockItem(id, ModBlocks.TASK_SCREEN_3.get(), ScreenSize.THREE_X_THREE));
	public static final RegistrySupplier<Item> TASK_SCREEN_5 = blockItemFor("screen_5",
			(id) -> new ScreenBlockItem(id, ModBlocks.TASK_SCREEN_5.get(), ScreenSize.FIVE_X_FIVE));
	public static final RegistrySupplier<Item> TASK_SCREEN_7 = blockItemFor("screen_7",
			(id) -> new ScreenBlockItem(id, ModBlocks.TASK_SCREEN_7.get(), ScreenSize.SEVEN_X_SEVEN));

	public static final List<RegistrySupplier<Item>> BASE_ITEMS = List.of(
			BOOK,
			BARRIER,
			STAGE_BARRIER,
			DETECTOR,
			LOOT_CRATE_OPENER,
			TASK_SCREEN_1,
			TASK_SCREEN_3,
			TASK_SCREEN_5,
			TASK_SCREEN_7,
			TASK_SCREEN_CONFIGURATOR
	);

	public static void register() {
		ITEMS.register();

		CreativeTabRegistry.appendStack(FTBLibrary.getCreativeModeTab(), BASE_ITEMS.stream().map(item -> () -> new ItemStack(item.get())));

		CreativeTabRegistry.modify(FTBLibrary.getCreativeModeTab(), (flags, output, canUseGameMasterBlocks) -> {
            var stacks = FTBQuests.PROXY.getKnownLootCrates()
                    .stream()
                    .map(LootCrate::createStack)
					.filter(stack -> !stack.isEmpty())
                    .toList();
            output.acceptAllAfter(BASE_ITEMS.getLast().get(), stacks);
        });
	}

	public static Item.Properties defaultProps() {
		return new Item.Properties();
	}

	private static RegistrySupplier<Item> register(String id, Function<ResourceKey<Item>, ? extends Item> func) {
		var tmpId = ResourceKey.create(Registries.ITEM, FTBQuestsAPI.id(id));
		return ITEMS.register(id, () -> func.apply(tmpId));
	}
}
