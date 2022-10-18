package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.hooks.fluid.FluidStackHooks;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.FTBQuests;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public interface TaskTypes {
	Map<ResourceLocation, TaskType> TYPES = new LinkedHashMap<>();

	static TaskType register(ResourceLocation name, TaskType.Provider p, Supplier<Icon> i) {
		return TYPES.computeIfAbsent(name, id -> new TaskType(id, p, i));
	}

	TaskType ITEM = register(new ResourceLocation(FTBQuests.MOD_ID, "item"), ItemTask::new, () -> Icon.getIcon("minecraft:item/diamond"));
	TaskType CUSTOM = register(new ResourceLocation(FTBQuests.MOD_ID, "custom"), CustomTask::new, () -> Icons.COLOR_HSB);
	TaskType XP = register(new ResourceLocation(FTBQuests.MOD_ID, "xp"), XPTask::new, () -> Icon.getIcon("minecraft:item/experience_bottle"));
	TaskType DIMENSION = register(new ResourceLocation(FTBQuests.MOD_ID, "dimension"), DimensionTask::new, () -> Icon.getIcon("minecraft:block/nether_portal"));
	TaskType STAT = register(new ResourceLocation(FTBQuests.MOD_ID, "stat"), StatTask::new, () -> Icon.getIcon("minecraft:item/iron_sword"));
	TaskType KILL = register(new ResourceLocation(FTBQuests.MOD_ID, "kill"), KillTask::new, () -> Icon.getIcon("minecraft:item/diamond_sword"));
	TaskType LOCATION = register(new ResourceLocation(FTBQuests.MOD_ID, "location"), LocationTask::new, () -> Icon.getIcon("minecraft:item/compass_00"));
	TaskType CHECKMARK = register(new ResourceLocation(FTBQuests.MOD_ID, "checkmark"), CheckmarkTask::new, () -> Icons.ACCEPT_GRAY);
	TaskType ADVANCEMENT = register(new ResourceLocation(FTBQuests.MOD_ID, "advancement"), AdvancementTask::new, () -> Icon.getIcon("minecraft:item/wheat"));
	TaskType OBSERVATION = register(new ResourceLocation(FTBQuests.MOD_ID, "observation"), ObservationTask::new, () -> Icons.ART);
	TaskType BIOME = register(new ResourceLocation(FTBQuests.MOD_ID, "biome"), BiomeTask::new, () -> Icon.getIcon("minecraft:block/oak_sapling"));
	TaskType STRUCTURE = register(new ResourceLocation(FTBQuests.MOD_ID, "structure"), StructureTask::new, () -> Icon.getIcon("minecraft:item/filled_map"));
	TaskType STAGE = TaskTypes.register(new ResourceLocation(FTBQuests.MOD_ID, "gamestage"), StageTask::new, () -> Icons.CONTROLLER);
	TaskType FLUID = TaskTypes.register(new ResourceLocation(FTBQuests.MOD_ID, "fluid"), FluidTask::new, () -> Icon.getIcon(Optional.ofNullable(FluidStackHooks.getStillTexture(Fluids.WATER)).map(TextureAtlasSprite::getName).map(ResourceLocation::toString).orElse("missingno")).combineWith(Icon.getIcon(FluidTask.TANK_TEXTURE.toString())));

	static void init() {
	}
}
