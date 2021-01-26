package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import me.shedaniel.architectury.hooks.FluidStackHooks;
import me.shedaniel.architectury.registry.DeferredRegister;
import me.shedaniel.architectury.registry.RegistrySupplier;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;

/**
 * @author LatvianModder
 */
public class FTBQuestsTasks
{
	public static final DeferredRegister<TaskType> TYPES = DeferredRegister.create(FTBQuests.MOD_ID, (ResourceKey) TaskType.getRegistry().key());
	public static final RegistrySupplier<TaskType> ITEM = TYPES.register("item", () -> new TaskType(ItemTask::new).setIcon(Icon.getIcon("minecraft:item/diamond")));
//	public static final RegistrySupplier<TaskType> FLUID = TYPES.register("fluid", () -> new TaskType(FluidTask::new).setIcon(Icon.getIcon(Optional.ofNullable(FluidStackHooks.getStillTexture(Fluids.WATER)).map(TextureAtlasSprite::getName).map(ResourceLocation::toString).orElse("missingno")).combineWith(Icon.getIcon(FluidTask.TANK_TEXTURE.toString()))));
	public static final RegistrySupplier<TaskType> FORGE_ENERGY = TYPES.register("forge_energy", () -> new TaskType(ForgeEnergyTask::new).setIcon(Icon.getIcon(ForgeEnergyTask.EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(ForgeEnergyTask.FULL_TEXTURE.toString()))));
	public static final RegistrySupplier<TaskType> CUSTOM = TYPES.register("custom", () -> new TaskType(CustomTask::new).setIcon(GuiIcons.COLOR_HSB));
	public static final RegistrySupplier<TaskType> XP = TYPES.register("xp", () -> new TaskType(XPTask::new).setIcon(Icon.getIcon("minecraft:item/experience_bottle")));
	public static final RegistrySupplier<TaskType> DIMENSION = TYPES.register("dimension", () -> new TaskType(DimensionTask::new).setIcon(Icon.getIcon("minecraft:block/nether_portal")));
	public static final RegistrySupplier<TaskType> STAT = TYPES.register("stat", () -> new TaskType(StatTask::new).setIcon(Icon.getIcon("minecraft:item/iron_sword")));
	public static final RegistrySupplier<TaskType> KILL = TYPES.register("kill", () -> new TaskType(KillTask::new).setIcon(Icon.getIcon("minecraft:item/diamond_sword")));
	public static final RegistrySupplier<TaskType> LOCATION = TYPES.register("location", () -> new TaskType(LocationTask::new).setIcon(Icon.getIcon("minecraft:item/compass_00")));
	public static final RegistrySupplier<TaskType> CHECKMARK = TYPES.register("checkmark", () -> new TaskType(CheckmarkTask::new).setIcon(GuiIcons.ACCEPT_GRAY));
	public static final RegistrySupplier<TaskType> ADVANCEMENT = TYPES.register("advancement", () -> new TaskType(AdvancementTask::new).setIcon(Icon.getIcon("minecraft:item/wheat")));
	public static final RegistrySupplier<TaskType> OBSERVATION = TYPES.register("observation", () -> new TaskType(ObservationTask::new).setIcon(GuiIcons.ART));
	public static final RegistrySupplier<TaskType> BIOME = TYPES.register("biome", () -> new TaskType(BiomeTask::new).setIcon(Icon.getIcon("minecraft:blocks/sapling_oak")));

	public static void register()
	{
		TYPES.register();
	}
}