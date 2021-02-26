package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.integration.gamestages.GameStagesIntegration;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.task.TaskTypes;
import com.feed_the_beast.ftbquests.quest.task.forge.ForgeEnergyTask;
import com.feed_the_beast.ftbquests.quest.task.forge.ForgeFluidTask;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import me.shedaniel.architectury.hooks.FluidStackHooks;
import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.platform.forge.EventBuses;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Iterator;
import java.util.Optional;

@Mod(FTBQuests.MOD_ID)
public class FTBQuestsForge {
	public FTBQuestsForge() {
		EventBuses.registerModEventBus(FTBQuests.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

		new FTBQuests();

		TaskTypes.FLUID = TaskTypes.register(new ResourceLocation(FTBQuests.MOD_ID, "fluid"), ForgeFluidTask::new, () -> Icon.getIcon(Optional.ofNullable(FluidStackHooks.getStillTexture(Fluids.WATER)).map(TextureAtlasSprite::getName).map(ResourceLocation::toString).orElse("missingno")).combineWith(Icon.getIcon(ForgeFluidTask.TANK_TEXTURE.toString())));
		TaskTypes.FORGE_ENERGY = TaskTypes.register(new ResourceLocation(FTBQuests.MOD_ID, "forge_energy"), ForgeEnergyTask::new, () -> Icon.getIcon(ForgeEnergyTask.EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(ForgeEnergyTask.FULL_TEXTURE.toString())));

		if (Platform.isModLoaded("gamestages")) {
			new GameStagesIntegration().init();
		}

		MinecraftForge.EVENT_BUS.addListener(FTBQuestsForge::livingDrops);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, FTBQuestsForge::dropsEvent);
	}

	private static void livingDrops(LivingDropsEvent event) {
		LivingEntity e = event.getEntityLiving();

		if (e.level.isClientSide || e instanceof Player) {
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
		if (!(event.getEntity() instanceof ServerPlayer)) {
			return;
		}

		ServerPlayer player = (ServerPlayer) event.getEntity();

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
