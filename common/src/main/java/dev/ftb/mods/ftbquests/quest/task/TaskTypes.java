package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.FTBQuests;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class TaskTypes {
	public static final HashMap<ResourceLocation, TaskType> TYPES = new LinkedHashMap<>();

	public static TaskType register(ResourceLocation name, TaskType.Provider p, Supplier<Icon> i) {
		return TYPES.computeIfAbsent(name, id -> new TaskType(id, p, i));
	}

	public static final TaskType ITEM = register(new ResourceLocation(FTBQuests.MOD_ID, "item"), ItemTask::new, () -> Icon.getIcon("minecraft:item/diamond"));
	public static final TaskType CUSTOM = register(new ResourceLocation(FTBQuests.MOD_ID, "custom"), CustomTask::new, () -> Icons.COLOR_HSB);
	public static final TaskType XP = register(new ResourceLocation(FTBQuests.MOD_ID, "xp"), XPTask::new, () -> Icon.getIcon("minecraft:item/experience_bottle"));
	public static final TaskType DIMENSION = register(new ResourceLocation(FTBQuests.MOD_ID, "dimension"), DimensionTask::new, () -> Icon.getIcon("minecraft:block/nether_portal"));
	public static final TaskType STAT = register(new ResourceLocation(FTBQuests.MOD_ID, "stat"), StatTask::new, () -> Icon.getIcon("minecraft:item/iron_sword"));
	public static final TaskType KILL = register(new ResourceLocation(FTBQuests.MOD_ID, "kill"), KillTask::new, () -> Icon.getIcon("minecraft:item/diamond_sword"));
	public static final TaskType LOCATION = register(new ResourceLocation(FTBQuests.MOD_ID, "location"), LocationTask::new, () -> Icon.getIcon("minecraft:item/compass_00"));
	public static final TaskType CHECKMARK = register(new ResourceLocation(FTBQuests.MOD_ID, "checkmark"), CheckmarkTask::new, () -> Icons.ACCEPT_GRAY);
	public static final TaskType ADVANCEMENT = register(new ResourceLocation(FTBQuests.MOD_ID, "advancement"), AdvancementTask::new, () -> Icon.getIcon("minecraft:item/wheat"));
	public static final TaskType OBSERVATION = register(new ResourceLocation(FTBQuests.MOD_ID, "observation"), ObservationTask::new, () -> Icons.ART);
	public static final TaskType BIOME = register(new ResourceLocation(FTBQuests.MOD_ID, "biome"), BiomeTask::new, () -> Icon.getIcon("minecraft:block/oak_sapling"));

	public static TaskType FLUID;
	public static TaskType FORGE_ENERGY;

	public static void init() {
	}
}