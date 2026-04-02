package dev.ftb.mods.ftbquests.neoforge;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.neoforge.NeoEventHelper;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.api.event.*;
import dev.ftb.mods.ftbquests.api.event.progress.ChapterProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.FileProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.QuestProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.TaskProgressEvent;
import dev.ftb.mods.ftbquests.api.neoforge.FTBQuestsClientEvent;
import dev.ftb.mods.ftbquests.api.neoforge.FTBQuestsEvent;
import dev.ftb.mods.ftbquests.block.neoforge.NeoLootCrateOpenerBlockEntity;
import dev.ftb.mods.ftbquests.block.neoforge.NeoTaskScreenAuxBlockEntity;
import dev.ftb.mods.ftbquests.block.neoforge.NeoTaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.quest.task.neoforge.ForgeEnergyTask;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(FTBQuestsAPI.MOD_ID)
public class FTBQuestsNeoForge {
	public FTBQuestsNeoForge(IEventBus modEventBus) {
		FTBQuests quests = new FTBQuests();

		ArgumentTypes.COMMAND_ARGUMENT_TYPES.register(modEventBus);

		ForgeEnergyTask.TYPE = TaskTypes.register(FTBQuestsAPI.id("forge_energy"), ForgeEnergyTask::new,
				() -> Icon.getIcon(ForgeEnergyTask.EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(ForgeEnergyTask.FULL_TEXTURE.toString())));

		NeoEventHandler.init(quests.eventHandler);

		modEventBus.addListener(this::addCreativeTabContents);
		modEventBus.addListener(FTBQuestsNeoForge::registerCapabilities);

		registerNeoEventPosters();
	}

	private static void registerNeoEventPosters() {
		IEventBus bus = NeoForge.EVENT_BUS;

		NeoEventHelper.registerNeoEventPoster(bus, CustomFilterDisplayItemsEvent.Data.class, FTBQuestsClientEvent.CustomFilterDisplayItems::new);
		NeoEventHelper.registerNeoEventPoster(bus, ClearFileCacheEvent.Data.class, FTBQuestsEvent.ClearFileCache::new);
		NeoEventHelper.registerNeoEventPoster(bus, CustomTaskEvent.Data.class, FTBQuestsEvent.CustomTask::new);
		NeoEventHelper.registerNeoEventPoster(bus, CustomRewardEvent.Data.class, FTBQuestsEvent.CustomReward::new);
		NeoEventHelper.registerNeoEventPoster(bus, FileProgressEvent.Data.class, FTBQuestsEvent.FileProgress::new);
		NeoEventHelper.registerNeoEventPoster(bus, ChapterProgressEvent.Data.class, FTBQuestsEvent.ChapterProgress::new);
		NeoEventHelper.registerNeoEventPoster(bus, QuestProgressEvent.Data.class, FTBQuestsEvent.QuestProgress::new);
		NeoEventHelper.registerNeoEventPoster(bus, TaskProgressEvent.Data.class, FTBQuestsEvent.TaskProgress::new);
	}

	private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTab() == FTBLibrary.getCreativeModeTab().get()) {
			event.acceptAll(ModItems.BASE_ITEMS.stream().map(item -> new ItemStack(item.get())).toList());
			var crates = FTBQuests.getKnownLootCrates()
					.stream()
					.map(LootCrate::createStack)
					.filter(stack -> !stack.isEmpty())
					.toList();
			event.acceptAll(crates);
		}
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntityTypes.CORE_TASK_SCREEN.get(),
				(be, _) -> ((NeoTaskScreenBlockEntity) be).getItemHandler());
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntityTypes.CORE_TASK_SCREEN.get(),
				(be, _) -> ((NeoTaskScreenBlockEntity) be).getFluidHandler());
		event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntityTypes.CORE_TASK_SCREEN.get(),
				(be, _) -> ((NeoTaskScreenBlockEntity) be).getEnergyHandler());

		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntityTypes.AUX_TASK_SCREEN.get(),
				(be, _) -> ((NeoTaskScreenAuxBlockEntity) be).getItemHandler());
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntityTypes.AUX_TASK_SCREEN.get(),
				(be, _) -> ((NeoTaskScreenAuxBlockEntity) be).getFluidHandler());
		event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntityTypes.AUX_TASK_SCREEN.get(),
				(be, _) -> ((NeoTaskScreenAuxBlockEntity) be).getEnergyHandler());

		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntityTypes.LOOT_CRATE_OPENER.get(),
				(be, _) -> ((NeoLootCrateOpenerBlockEntity) be).getLootCrateHandler());
	}
}
