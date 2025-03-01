package dev.ftb.mods.ftbquests.neoforge;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.api.FTBQuestsTags;
import dev.ftb.mods.ftbquests.block.neoforge.NeoForgeLootCrateOpenerBlockEntity;
import dev.ftb.mods.ftbquests.block.neoforge.NeoForgeTaskScreenAuxBlockEntity;
import dev.ftb.mods.ftbquests.block.neoforge.NeoForgeTaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.quest.task.neoforge.ForgeEnergyTask;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.Iterator;

@Mod(FTBQuestsAPI.MOD_ID)
public class FTBQuestsNeoForge {
	public FTBQuestsNeoForge(IEventBus modEventBus) {
		ArgumentTypes.COMMAND_ARGUMENT_TYPES.register(modEventBus);

		FTBQuests quests = new FTBQuests();

		ForgeEnergyTask.TYPE = TaskTypes.register(FTBQuestsAPI.rl("forge_energy"), ForgeEnergyTask::new,
				() -> Icon.getIcon(ForgeEnergyTask.EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(ForgeEnergyTask.FULL_TEXTURE.toString())));

		modEventBus.<FMLCommonSetupEvent>addListener(event -> quests.setup());

		if (FMLEnvironment.dist == Dist.CLIENT) {
			ClientSetup.init(modEventBus);
		}

		NeoForge.EVENT_BUS.addListener(FTBQuestsNeoForge::livingDrops);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, FTBQuestsNeoForge::dropsEvent);

		modEventBus.addListener(FTBQuestsNeoForge::registerCaps);
	}

	private static void livingDrops(LivingDropsEvent event) {
		LivingEntity living = event.getEntity();

		if (living.level().isClientSide || living instanceof Player || living.getType().is(FTBQuestsTags.EntityTypes.NO_LOOT_CRATES)) {
			return;
		}
		if (ServerQuestFile.INSTANCE == null || !ServerQuestFile.INSTANCE.isDropLootCrates()) {
			return;
		}

		ServerQuestFile.INSTANCE.makeRandomLootCrate(living, living.level().random).ifPresent(crate -> {
			ItemEntity itemEntity = new ItemEntity(living.level(), living.getX(), living.getY(), living.getZ(), crate.createStack());
			itemEntity.setPickUpDelay(10);
			event.getDrops().add(itemEntity);
		});
	}

	private static void dropsEvent(LivingDropsEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)
				|| player instanceof FakePlayer
				|| player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
				|| ServerQuestFile.INSTANCE.dropBookOnDeath()) {
			return;
		}

		Iterator<ItemEntity> iterator = event.getDrops().iterator();

		while (iterator.hasNext()) {
			ItemEntity drop = iterator.next();
			ItemStack stack = drop.getItem();

			if (stack.getItem() == ModItems.BOOK.get() && player.addItem(stack)) {
				iterator.remove();
			}
		}
	}

	public static void registerCaps(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntityTypes.CORE_TASK_SCREEN.get(),
				(be, side) -> ((NeoForgeTaskScreenBlockEntity) be).getItemHandler());
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntityTypes.CORE_TASK_SCREEN.get(),
				(be, side) -> ((NeoForgeTaskScreenBlockEntity) be).getFluidHandler());
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntityTypes.CORE_TASK_SCREEN.get(),
				(be, side) -> ((NeoForgeTaskScreenBlockEntity) be).getEnergyHandler());

		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntityTypes.AUX_TASK_SCREEN.get(),
				(be, side) -> ((NeoForgeTaskScreenAuxBlockEntity) be).getItemHandler());
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntityTypes.AUX_TASK_SCREEN.get(),
				(be, side) -> ((NeoForgeTaskScreenAuxBlockEntity) be).getFluidHandler());
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntityTypes.AUX_TASK_SCREEN.get(),
				(be, side) -> ((NeoForgeTaskScreenAuxBlockEntity) be).getEnergyHandler());

		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntityTypes.LOOT_CRATE_OPENER.get(),
				(be, side) -> ((NeoForgeLootCrateOpenerBlockEntity) be).getLootCrateHandler());
	}
}
