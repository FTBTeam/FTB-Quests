package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class RewardTypes {
	public static final HashMap<ResourceLocation, RewardType> TYPES = new LinkedHashMap<>();

	public static RewardType register(ResourceLocation name, RewardType.Provider p, Supplier<Icon> i) {
		return TYPES.computeIfAbsent(name, id -> new RewardType(id, p, i));
	}

	public static RewardType ITEM = register(new ResourceLocation(FTBQuests.MOD_ID, "item"), ItemReward::new, () -> Icon.getIcon("minecraft:item/diamond"));
	public static RewardType CHOICE = register(new ResourceLocation(FTBQuests.MOD_ID, "choice"), ChoiceReward::new, () -> GuiIcons.COLOR_RGB).setExcludeFromListRewards(true);
	public static RewardType RANDOM = register(new ResourceLocation(FTBQuests.MOD_ID, "random"), RandomReward::new, () -> GuiIcons.DICE).setExcludeFromListRewards(true);
	public static RewardType LOOT = register(new ResourceLocation(FTBQuests.MOD_ID, "loot"), LootReward::new, () -> GuiIcons.MONEY_BAG).setExcludeFromListRewards(true);
	public static RewardType COMMAND = register(new ResourceLocation(FTBQuests.MOD_ID, "command"), CommandReward::new, () -> Icon.getIcon("minecraft:block/command_block_back"));
	public static RewardType CUSTOM = register(new ResourceLocation(FTBQuests.MOD_ID, "custom"), CustomReward::new, () -> GuiIcons.COLOR_HSB);
	public static RewardType XP = register(new ResourceLocation(FTBQuests.MOD_ID, "xp"), XPReward::new, () -> Icon.getIcon("minecraft:item/experience_bottle"));
	public static RewardType XP_LEVELS = register(new ResourceLocation(FTBQuests.MOD_ID, "xp_levels"), XPLevelsReward::new, () -> Icon.getIcon("minecraft:item/experience_bottle"));
	public static RewardType ADVANCEMENT = register(new ResourceLocation(FTBQuests.MOD_ID, "advancement"), AdvancementReward::new, () -> Icon.getIcon("minecraft:item/wheat"));
	public static RewardType TOAST = register(new ResourceLocation(FTBQuests.MOD_ID, "toast"), ToastReward::new, () -> Icon.getIcon("minecraft:item/oak_sign"));

	public static void init() {
	}
}