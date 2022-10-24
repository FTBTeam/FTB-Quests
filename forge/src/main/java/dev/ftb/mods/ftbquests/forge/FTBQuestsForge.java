package dev.ftb.mods.ftbquests.forge;

import com.google.common.base.Suppliers;
import dev.architectury.platform.Platform;
import dev.architectury.platform.forge.EventBuses;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.FTBQuestsTags;
import dev.ftb.mods.ftbquests.command.ChangeProgressArgument;
import dev.ftb.mods.ftbquests.command.QuestObjectArgument;
import dev.ftb.mods.ftbquests.integration.StageHelper;
import dev.ftb.mods.ftbquests.integration.gamestages.GameStagesStageHelper;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.quest.task.forge.ForgeEnergyTask;
import dev.ftb.mods.ftbteams.FTBTeams;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Iterator;

@Mod(FTBQuests.MOD_ID)
public class FTBQuestsForge {
	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registry.COMMAND_ARGUMENT_TYPE_REGISTRY, FTBTeams.MOD_ID);
	private static final RegistryObject<SingletonArgumentInfo<ChangeProgressArgument>> CHANGE_PROGRESS = COMMAND_ARGUMENT_TYPES.register("change_progress", () -> ArgumentTypeInfos.registerByClass(ChangeProgressArgument.class, SingletonArgumentInfo.contextFree(ChangeProgressArgument::changeProgress)));
	private static final RegistryObject<SingletonArgumentInfo<QuestObjectArgument>> QUEST_OBJECT = COMMAND_ARGUMENT_TYPES.register("quest_object", () -> ArgumentTypeInfos.registerByClass(QuestObjectArgument.class, SingletonArgumentInfo.contextFree(QuestObjectArgument::new)));

	public FTBQuestsForge() {
		EventBuses.registerModEventBus(FTBQuests.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
		COMMAND_ARGUMENT_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());

		FTBQuests quests = new FTBQuests();

		ForgeEnergyTask.TYPE = TaskTypes.register(new ResourceLocation(FTBQuests.MOD_ID, "forge_energy"), ForgeEnergyTask::new, () -> Icon.getIcon(ForgeEnergyTask.EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(ForgeEnergyTask.FULL_TEXTURE.toString())));

		if (Platform.isModLoaded("gamestages") && !Platform.isModLoaded("kubejs")) {
			StageHelper.instance = Suppliers.memoize(GameStagesStageHelper::new);
		}

		FMLJavaModLoadingContext.get().getModEventBus().<FMLCommonSetupEvent>addListener(event -> quests.setup());

		MinecraftForge.EVENT_BUS.addListener(FTBQuestsForge::livingDrops);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, FTBQuestsForge::dropsEvent);
	}

	private static void livingDrops(LivingDropsEvent event) {
		LivingEntity e = event.getEntity();

		if (e.level.isClientSide || e instanceof Player || e.getType().is(FTBQuestsTags.EntityTypes.NO_LOOT_CRATES)) {
			return;
		}

		if (ServerQuestFile.INSTANCE == null || !ServerQuestFile.INSTANCE.dropLootCrates) {
			return;
		}

		LootCrate crate = ServerQuestFile.INSTANCE.getRandomLootCrate(e, e.level.random);

		if (crate != null) {
			ItemEntity ei = new ItemEntity(e.level, e.getX(), e.getY(), e.getZ(), crate.createStack());
			ei.setPickUpDelay(10);
			event.getDrops().add(ei);
		}
	}

	private static void dropsEvent(LivingDropsEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}

		if (player instanceof FakePlayer || player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
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
