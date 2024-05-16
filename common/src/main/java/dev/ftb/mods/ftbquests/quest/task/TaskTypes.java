package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public interface TaskTypes {
	Map<ResourceLocation, TaskType> TYPES = new LinkedHashMap<>();

	static TaskType register(ResourceLocation name, TaskType.Provider provider, Supplier<Icon> iconSupplier) {
		return TYPES.computeIfAbsent(name, id -> new TaskType(id, provider, iconSupplier));
	}

	TaskType ITEM = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "item"), ItemTask::new,
			() -> Icon.getIcon("minecraft:item/diamond"));
	TaskType CUSTOM = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "custom"), CustomTask::new,
			() -> Icons.COLOR_HSB);
	TaskType XP = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "xp"), XPTask::new,
			() -> Icon.getIcon("minecraft:item/experience_bottle"));
	TaskType DIMENSION = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "dimension"), DimensionTask::new,
			() -> Icon.getIcon("minecraft:block/nether_portal"));
	TaskType STAT = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "stat"), StatTask::new,
			() -> Icon.getIcon("minecraft:item/iron_sword"));
	TaskType KILL = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "kill"), KillTask::new,
			() -> Icon.getIcon("minecraft:item/diamond_sword"));
	TaskType LOCATION = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "location"), LocationTask::new,
			() -> Icon.getIcon("minecraft:item/compass_00"));
	TaskType CHECKMARK = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "checkmark"), CheckmarkTask::new,
			() -> Icons.ACCEPT_GRAY);
	TaskType ADVANCEMENT = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "advancement"), AdvancementTask::new,
			() -> Icon.getIcon("minecraft:item/wheat"));
	TaskType OBSERVATION = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "observation"), ObservationTask::new,
			() -> Icons.ART);
	TaskType BIOME = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "biome"), BiomeTask::new,
			() -> Icon.getIcon("minecraft:block/oak_sapling"));
	TaskType STRUCTURE = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "structure"), StructureTask::new,
			() -> Icon.getIcon("minecraft:item/filled_map"));
	TaskType STAGE = TaskTypes.register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "gamestage"), StageTask::new,
			() -> Icons.CONTROLLER);
	TaskType FLUID = TaskTypes.register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "fluid"), FluidTask::new,
			() -> Icon.getIcon(Optional.ofNullable(ClientUtils.getStillTexture(FluidStack.create(Fluids.WATER, 1000L)))
							.map(ResourceLocation::toString)
							.orElse("missingno")).withTint(Color4I.rgb(0x8080FF))
					.combineWith(Icon.getIcon(FluidTask.TANK_TEXTURE.toString()))
	);

	static void init() {
	}
}
