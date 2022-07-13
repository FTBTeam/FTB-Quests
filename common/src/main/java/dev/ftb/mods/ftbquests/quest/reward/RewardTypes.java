package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.FTBQuests;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public interface RewardTypes {
	Map<ResourceLocation, RewardType> TYPES = new LinkedHashMap<>();

	static RewardType register(ResourceLocation name, RewardType.Provider p, Supplier<Icon> i) {
		return TYPES.computeIfAbsent(name, id -> new RewardType(id, p, i));
	}

	RewardType ITEM = register(new ResourceLocation(FTBQuests.MOD_ID, "item"), ItemReward::new, () -> Icon.getIcon("minecraft:item/diamond"));
	RewardType CHOICE = register(new ResourceLocation(FTBQuests.MOD_ID, "choice"), ChoiceReward::new, () -> Icons.COLOR_RGB).setExcludeFromListRewards(true);
	RewardType RANDOM = register(new ResourceLocation(FTBQuests.MOD_ID, "random"), RandomReward::new, () -> Icons.DICE).setExcludeFromListRewards(true);
	RewardType LOOT = register(new ResourceLocation(FTBQuests.MOD_ID, "loot"), LootReward::new, () -> Icons.MONEY_BAG).setExcludeFromListRewards(true);
	RewardType COMMAND = register(new ResourceLocation(FTBQuests.MOD_ID, "command"), CommandReward::new, () -> Icon.getIcon("minecraft:block/command_block_back"));
	RewardType CUSTOM = register(new ResourceLocation(FTBQuests.MOD_ID, "custom"), CustomReward::new, () -> Icons.COLOR_HSB);
	RewardType XP = register(new ResourceLocation(FTBQuests.MOD_ID, "xp"), XPReward::new, () -> Icon.getIcon("minecraft:item/experience_bottle"));
	RewardType XP_LEVELS = register(new ResourceLocation(FTBQuests.MOD_ID, "xp_levels"), XPLevelsReward::new, () -> Icon.getIcon("minecraft:item/experience_bottle"));
	RewardType ADVANCEMENT = register(new ResourceLocation(FTBQuests.MOD_ID, "advancement"), AdvancementReward::new, () -> Icon.getIcon("minecraft:item/wheat"));
	RewardType TOAST = register(new ResourceLocation(FTBQuests.MOD_ID, "toast"), ToastReward::new, () -> Icon.getIcon("minecraft:item/oak_sign"));
	RewardType STAGE = RewardTypes.register(new ResourceLocation(FTBQuests.MOD_ID, "gamestage"), StageReward::new, () -> Icons.CONTROLLER);

	static void init() {
	}
}
