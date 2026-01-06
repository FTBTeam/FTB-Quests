package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluids;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public interface TaskTypes {
	Map<Identifier, TaskType> TYPES = new LinkedHashMap<>();

	static TaskType register(Identifier name, TaskType.Provider provider, Supplier<Icon> iconSupplier) {
		return TYPES.computeIfAbsent(name, id -> new TaskType(id, provider, iconSupplier));
	}

	TaskType ITEM = register(FTBQuestsAPI.id("item"), ItemTask::new,
			() -> Icon.getIcon("minecraft:item/diamond"));
	TaskType CUSTOM = register(FTBQuestsAPI.id("custom"), CustomTask::new,
			() -> Icons.COLOR_HSB);
	TaskType XP = register(FTBQuestsAPI.id("xp"), XPTask::new,
			() -> Icon.getIcon("minecraft:item/experience_bottle"));
	TaskType DIMENSION = register(FTBQuestsAPI.id("dimension"), DimensionTask::new,
			() -> Icon.getIcon("minecraft:block/nether_portal"));
	TaskType STAT = register(FTBQuestsAPI.id("stat"), StatTask::new,
			() -> Icon.getIcon("minecraft:item/iron_sword"));
	TaskType KILL = register(FTBQuestsAPI.id("kill"), KillTask::new,
			() -> Icon.getIcon("minecraft:item/diamond_sword"));
	TaskType LOCATION = register(FTBQuestsAPI.id("location"), LocationTask::new,
			() -> Icon.getIcon("minecraft:item/compass_00"));
	TaskType CHECKMARK = register(FTBQuestsAPI.id("checkmark"), CheckmarkTask::new,
			() -> Icons.ACCEPT_GRAY);
	TaskType ADVANCEMENT = register(FTBQuestsAPI.id("advancement"), AdvancementTask::new,
			() -> Icon.getIcon("minecraft:item/wheat"));
	TaskType OBSERVATION = register(FTBQuestsAPI.id("observation"), ObservationTask::new,
			() -> Icons.ART);
	TaskType BIOME = register(FTBQuestsAPI.id("biome"), BiomeTask::new,
			() -> Icon.getIcon("minecraft:block/oak_sapling"));
	TaskType STRUCTURE = register(FTBQuestsAPI.id("structure"), StructureTask::new,
			() -> Icon.getIcon("minecraft:item/filled_map"));
	TaskType STAGE = TaskTypes.register(FTBQuestsAPI.id("gamestage"), StageTask::new,
			() -> Icons.CONTROLLER);
	TaskType FLUID = TaskTypes.register(FTBQuestsAPI.id("fluid"), FluidTask::new,
			() -> Icon.getIcon(Optional.ofNullable(ClientUtils.getStillTexture(FluidStack.create(Fluids.WATER, 1000L)))
							.map(Identifier::toString)
							.orElse("missingno")).withTint(Color4I.rgb(0x8080FF))
					.combineWith(Icon.getIcon(FluidTask.TANK_TEXTURE.toString()))
	);

	static void init() {
	}
}
