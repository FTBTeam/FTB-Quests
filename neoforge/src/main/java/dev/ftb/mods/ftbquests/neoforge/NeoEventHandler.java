package dev.ftb.mods.ftbquests.neoforge;

import dev.ftb.mods.ftbquests.FTBQuestsEventHandler;
import dev.ftb.mods.ftbquests.api.FTBQuestsTags;
import dev.ftb.mods.ftbquests.api.neoforge.FTBQuestsEvent;
import dev.ftb.mods.ftbquests.block.QuestBarrierBlock;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.CustomTask;
import dev.ftb.mods.ftbquests.registry.ModItems;
import dev.ftb.mods.ftbquests.util.DeferredInventoryDetection;
import dev.ftb.mods.ftbteams.api.neoforge.FTBTeamsEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;

public class NeoEventHandler {
	static void init(FTBQuestsEventHandler eventHandler) {

		IEventBus bus = NeoForge.EVENT_BUS;

		bus.addListener(ServerAboutToStartEvent.class, event ->
				eventHandler.serverAboutToStart(event.getServer()));
		bus.addListener(ServerStartedEvent.class, event ->
				eventHandler.serverStarted(event.getServer()));
		bus.addListener(ServerStoppedEvent.class, event ->
				eventHandler.serverStopped(event.getServer()));
		bus.addListener(RegisterCommandsEvent.class, event ->
				eventHandler.registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection()));
		bus.addListener(LevelEvent.Save.class, event ->
				eventHandler.worldSaved((ServerLevel) event.getLevel()));

		bus.addListener(FTBQuestsEvent.ClearFileCache.class, event ->
				eventHandler.fileCacheClear(event.getEventData().file()));

		bus.addListener(FTBTeamsEvent.TeamPlayerLoggedIn.class, event ->
				eventHandler.playerLoggedIn(event.getEventData()));
		bus.addListener(FTBTeamsEvent.TeamCreated.class, event ->
				eventHandler.teamCreated(event.getEventData()));
		bus.addListener(FTBTeamsEvent.PlayerChangedTeam.class, event ->
				eventHandler.playerChangedTeam(event.getEventData()));

		bus.addListener(LivingDeathEvent.class, event ->
				eventHandler.playerKill(event.getEntity(), event.getSource()));
		bus.addListener(PlayerTickEvent.Post.class, event ->
				eventHandler.playerTick(event.getEntity()));
		bus.addListener(PlayerEvent.ItemCraftedEvent.class, event ->
				eventHandler.itemCrafted(event.getEntity(), event.getCrafting(), event.getInventory()));
		bus.addListener(PlayerEvent.ItemSmeltedEvent.class, event ->
				eventHandler.itemCrafted(event.getEntity(), event.getSmelting(), event.getEntity().getInventory()));
		bus.addListener(PlayerEvent.Clone.class, event ->
				eventHandler.cloned((ServerPlayer) event.getOriginal(), (ServerPlayer) event.getEntity(), !event.isWasDeath()));
		bus.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, event ->
				eventHandler.changedDimension((ServerPlayer) event.getEntity(), event.getFrom(), event.getTo()));
		bus.addListener(PlayerContainerEvent.Open.class, event ->
				eventHandler.containerOpened(event.getEntity(), event.getContainer()));

		bus.addListener(ServerTickEvent.Post.class, event -> {
			DeferredInventoryDetection.tick(event.getServer());
			QuestBarrierBlock.TeleportTicker.tick(event.getServer());
			CustomTask.TaskSync.tick(event.getServer());
		});

		bus.addListener(NeoEventHandler::livingDrops);
		bus.addListener(EventPriority.HIGH, NeoEventHandler::dropsEvent);
	}

	private static void livingDrops(LivingDropsEvent event) {
		LivingEntity living = event.getEntity();

		if (living.level().isClientSide() || living instanceof Player || living.is(FTBQuestsTags.EntityTypes.NO_LOOT_CRATES)) {
			return;
		}
		if (!ServerQuestFile.exists() || !ServerQuestFile.getInstance().isDropLootCrates()) {
			return;
		}

		ServerQuestFile.getInstance().makeRandomLootCrate(living, living.level().getRandom()).ifPresent(crate -> {
			ItemEntity itemEntity = new ItemEntity(living.level(), living.getX(), living.getY(), living.getZ(), crate.createStack());
			itemEntity.setPickUpDelay(10);
			event.getDrops().add(itemEntity);
		});
	}

	private static void dropsEvent(LivingDropsEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)
				|| player instanceof FakePlayer
				|| player.level().getGameRules().get(GameRules.KEEP_INVENTORY)
				|| ServerQuestFile.getInstance().dropBookOnDeath()) {
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
}
