package dev.ftb.mods.ftbquests.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.FTBQuestsTags;
import dev.ftb.mods.ftbquests.command.ChangeProgressArgument;
import dev.ftb.mods.ftbquests.command.QuestObjectArgument;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.quest.task.forge.ForgeEnergyTask;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Iterator;

@Mod(FTBQuests.MOD_ID)
public class FTBQuestsForge {
	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, FTBTeamsAPI.MOD_ID);
	private static final RegistryObject<SingletonArgumentInfo<ChangeProgressArgument>> CHANGE_PROGRESS = COMMAND_ARGUMENT_TYPES.register("change_progress", () -> ArgumentTypeInfos.registerByClass(ChangeProgressArgument.class, SingletonArgumentInfo.contextFree(ChangeProgressArgument::changeProgress)));
	private static final RegistryObject<SingletonArgumentInfo<QuestObjectArgument>> QUEST_OBJECT = COMMAND_ARGUMENT_TYPES.register("quest_object", () -> ArgumentTypeInfos.registerByClass(QuestObjectArgument.class, SingletonArgumentInfo.contextFree(QuestObjectArgument::new)));

	public FTBQuestsForge() {
		EventBuses.registerModEventBus(FTBQuests.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
		COMMAND_ARGUMENT_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());

		FTBQuests quests = new FTBQuests();

		ForgeEnergyTask.TYPE = TaskTypes.register(new ResourceLocation(FTBQuests.MOD_ID, "forge_energy"), ForgeEnergyTask::new,
				() -> Icon.getIcon(ForgeEnergyTask.EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(ForgeEnergyTask.FULL_TEXTURE.toString())));

		FMLJavaModLoadingContext.get().getModEventBus().<FMLCommonSetupEvent>addListener(event -> quests.setup());

		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSetup::init);

		MinecraftForge.EVENT_BUS.addListener(FTBQuestsForge::livingDrops);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, FTBQuestsForge::dropsEvent);
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
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}

		if (player instanceof FakePlayer || player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			return;
		}

		Iterator<ItemEntity> iterator = event.getDrops().iterator();

		while (iterator.hasNext()) {
			ItemEntity drop = iterator.next();
			ItemStack stack = drop.getItem();

			if (stack.getItem() == FTBQuestsItems.BOOK.get() && player.addItem(stack)) {
				iterator.remove();
			}
		}
	}
}
