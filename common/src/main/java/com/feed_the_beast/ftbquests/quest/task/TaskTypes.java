package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

/**
 * @author LatvianModder
 */
public class TaskTypes
{
	public static final HashMap<ResourceLocation, TaskType> TYPES = new HashMap<>();

	public static TaskType register(ResourceLocation name, TaskType.Provider p)
	{
		return TYPES.computeIfAbsent(name, id -> new TaskType(id, p));
	}

	public static final TaskType ITEM = register(new ResourceLocation(FTBQuests.MOD_ID, "item"), ItemTask::new).setIcon(Icon.getIcon("minecraft:item/diamond"));
	//	public static final TaskType FLUID = register(new ResourceLocation(FTBQuests.MOD_ID, "fluid"), FluidTask::new).setIcon(Icon.getIcon(Optional.ofNullable(FluidStackHooks.getStillTexture(Fluids.WATER)).map(TextureAtlasSprite::getName).map(ResourceLocation::toString).orElse("missingno")).combineWith(Icon.getIcon(FluidTask.TANK_TEXTURE.toString())));
	public static final TaskType FORGE_ENERGY = register(new ResourceLocation(FTBQuests.MOD_ID, "forge_energy"), ForgeEnergyTask::new).setIcon(Icon.getIcon(ForgeEnergyTask.EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(ForgeEnergyTask.FULL_TEXTURE.toString())));
	public static final TaskType CUSTOM = register(new ResourceLocation(FTBQuests.MOD_ID, "custom"), CustomTask::new).setIcon(GuiIcons.COLOR_HSB);
	public static final TaskType XP = register(new ResourceLocation(FTBQuests.MOD_ID, "xp"), XPTask::new).setIcon(Icon.getIcon("minecraft:item/experience_bottle"));
	public static final TaskType DIMENSION = register(new ResourceLocation(FTBQuests.MOD_ID, "dimension"), DimensionTask::new).setIcon(Icon.getIcon("minecraft:block/nether_portal"));
	public static final TaskType STAT = register(new ResourceLocation(FTBQuests.MOD_ID, "stat"), StatTask::new).setIcon(Icon.getIcon("minecraft:item/iron_sword"));
	public static final TaskType KILL = register(new ResourceLocation(FTBQuests.MOD_ID, "kill"), KillTask::new).setIcon(Icon.getIcon("minecraft:item/diamond_sword"));
	public static final TaskType LOCATION = register(new ResourceLocation(FTBQuests.MOD_ID, "location"), LocationTask::new).setIcon(Icon.getIcon("minecraft:item/compass_00"));
	public static final TaskType CHECKMARK = register(new ResourceLocation(FTBQuests.MOD_ID, "checkmark"), CheckmarkTask::new).setIcon(GuiIcons.ACCEPT_GRAY);
	public static final TaskType ADVANCEMENT = register(new ResourceLocation(FTBQuests.MOD_ID, "advancement"), AdvancementTask::new).setIcon(Icon.getIcon("minecraft:item/wheat"));
	public static final TaskType OBSERVATION = register(new ResourceLocation(FTBQuests.MOD_ID, "observation"), ObservationTask::new).setIcon(GuiIcons.ART);
	public static final TaskType BIOME = register(new ResourceLocation(FTBQuests.MOD_ID, "biome"), BiomeTask::new).setIcon(Icon.getIcon("minecraft:blocks/sapling_oak"));

	public static void init()
	{
	}
}