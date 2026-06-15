package dev.ftb.mods.ftbquests.fabric;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.fabric.FabricEventHelper;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.FTBQuestsEventHandler;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.api.event.CustomFilterDisplayItemsEvent;
import dev.ftb.mods.ftbquests.api.fabric.FTBQuestsClientEvents;
import dev.ftb.mods.ftbquests.api.fabric.FTBQuestsEvents;
import dev.ftb.mods.ftbquests.block.fabric.FabricLootCrateOpenerBlockEntity;
import dev.ftb.mods.ftbquests.block.fabric.FabricTaskScreenAuxBlockEntity;
import dev.ftb.mods.ftbquests.block.fabric.FabricTaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.command.ChangeProgressArgument;
import dev.ftb.mods.ftbquests.api.event.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.api.event.CustomRewardEvent;
import dev.ftb.mods.ftbquests.api.event.CustomTaskEvent;
import dev.ftb.mods.ftbquests.api.event.progress.ChapterProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.FileProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.QuestProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.TaskProgressEvent;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.quest.task.TechRebornEnergyTask;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import team.reborn.energy.api.EnergyStorage;

public class FTBQuestsFabric implements ModInitializer {
    private static FTBQuestsEventHandler eventHandler;

	@Override
	public void onInitialize() {
        FTBQuests quests = new FTBQuests();
		eventHandler = quests.eventHandler;

		ArgumentTypeRegistry.registerArgumentType(FTBQuestsAPI.id("change_progress"), ChangeProgressArgument.class, SingletonArgumentInfo.contextFree(ChangeProgressArgument::changeProgress));

		TechRebornEnergyTask.TYPE = TaskTypes.register(FTBQuestsAPI.id("tech_reborn_energy"), TechRebornEnergyTask::new, () -> Icon.getIcon(TechRebornEnergyTask.EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(TechRebornEnergyTask.FULL_TEXTURE.toString())));

		FabricEventHandler.init(eventHandler);

		registerFabricEventPosters();

		registerTransferHandlers();
	}

	public static FTBQuestsEventHandler getEventHandler() {
		return eventHandler;
	}

	private static void registerFabricEventPosters() {
		FabricEventHelper.registerFabricEventPoster(CustomFilterDisplayItemsEvent.Data.class, FTBQuestsClientEvents.CUSTOM_FILTER_DISPLAY_ITEMS);
		FabricEventHelper.registerFabricEventPoster(ClearFileCacheEvent.Data.class, FTBQuestsEvents.CLEAR_FILE_CACHE);
		FabricEventHelper.registerFabricEventPoster(CustomTaskEvent.Data.class, FTBQuestsEvents.CUSTOM_TASK);
		FabricEventHelper.registerFabricEventPoster(CustomRewardEvent.Data.class, FTBQuestsEvents.CUSTOM_REWARD);
		FabricEventHelper.registerFabricEventPoster(FileProgressEvent.Data.class, FTBQuestsEvents.FILE_PROGRESS);
		FabricEventHelper.registerFabricEventPoster(ChapterProgressEvent.Data.class, FTBQuestsEvents.CHAPTER_PROGRESS);
		FabricEventHelper.registerFabricEventPoster(QuestProgressEvent.Data.class, FTBQuestsEvents.QUEST_PROGRESS);
		FabricEventHelper.registerFabricEventPoster(TaskProgressEvent.Data.class, FTBQuestsEvents.TASK_PROGRESS);
    }

	private static void registerTransferHandlers() {
		ItemStorage.SIDED.registerForBlockEntity(
				((blockEntity, _) -> ((FabricTaskScreenBlockEntity) blockEntity).getItemStorage()), ModBlockEntityTypes.CORE_TASK_SCREEN.get()
		);
		ItemStorage.SIDED.registerForBlockEntity(
				((blockEntity, _) -> ((FabricTaskScreenAuxBlockEntity) blockEntity).getItemStorage()), ModBlockEntityTypes.AUX_TASK_SCREEN.get()
		);

		FluidStorage.SIDED.registerForBlockEntity(
				((blockEntity, _) -> ((FabricTaskScreenBlockEntity) blockEntity).getFluidStorage()), ModBlockEntityTypes.CORE_TASK_SCREEN.get()
		);
		FluidStorage.SIDED.registerForBlockEntity(
				((blockEntity, _) -> ((FabricTaskScreenAuxBlockEntity) blockEntity).getFluidStorage()), ModBlockEntityTypes.AUX_TASK_SCREEN.get()
		);

		EnergyStorage.SIDED.registerForBlockEntity(
				((blockEntity, _) -> ((FabricTaskScreenBlockEntity) blockEntity).getEnergyStorage()), ModBlockEntityTypes.CORE_TASK_SCREEN.get()
		);
		EnergyStorage.SIDED.registerForBlockEntity(
				((blockEntity, _) -> ((FabricTaskScreenAuxBlockEntity) blockEntity).getEnergyStorage()), ModBlockEntityTypes.AUX_TASK_SCREEN.get()
		);

		ItemStorage.SIDED.registerForBlockEntity(
				((blockEntity, _) -> ((FabricLootCrateOpenerBlockEntity) blockEntity).getItemStorage()), ModBlockEntityTypes.LOOT_CRATE_OPENER.get()
		);
	}
}
