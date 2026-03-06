package dev.ftb.mods.ftbquests.quest.reward;

import net.minecraft.resources.Identifier;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface RewardTypes {
	Map<Identifier, RewardType> TYPES = new LinkedHashMap<>();

	static RewardType register(Identifier name, RewardType.Provider typeProvider, Supplier<Icon<?>> iconSupplier, boolean availableByDefault) {
		return TYPES.computeIfAbsent(name, id -> new RewardType(id, typeProvider, iconSupplier, availableByDefault));
	}

	static RewardType register(Identifier name, RewardType.Provider typeProvider, Supplier<Icon<?>> iconSupplier) {
		return register(name, typeProvider, iconSupplier, true);
	}

	RewardType ITEM = register(FTBQuestsAPI.id("item"), ItemReward::new, () -> Icon.getIcon("minecraft:item/diamond"));
	RewardType CHOICE = register(FTBQuestsAPI.id("choice"), ChoiceReward::new, () -> Icons.COLOR_RGB).setExcludeFromListRewards(true);
	RewardType ALL_TABLE = register(FTBQuestsAPI.id("all_table"), AllTableReward::new, () -> Icons.COLOR_HSB).setExcludeFromListRewards(true);
	RewardType RANDOM = register(FTBQuestsAPI.id("random"), RandomReward::new, () -> Icons.DICE).setExcludeFromListRewards(true);
	RewardType LOOT = register(FTBQuestsAPI.id("loot"), LootReward::new, () -> Icons.MONEY_BAG).setExcludeFromListRewards(true);
	RewardType COMMAND = register(FTBQuestsAPI.id("command"), CommandReward::new, () -> Icon.getIcon("minecraft:block/command_block_back"));
	RewardType CUSTOM = register(FTBQuestsAPI.id("custom"), CustomReward::new, () -> Icons.COLOR_HSB);
	RewardType XP = register(FTBQuestsAPI.id("xp"), XPReward::new, () -> Icon.getIcon("minecraft:item/experience_bottle"));
	RewardType XP_LEVELS = register(FTBQuestsAPI.id("xp_levels"), XPLevelsReward::new, () -> Icon.getIcon("minecraft:item/experience_bottle"));
	RewardType ADVANCEMENT = register(FTBQuestsAPI.id("advancement"), AdvancementReward::new, () -> Icon.getIcon("minecraft:item/wheat"));
	RewardType TOAST = register(FTBQuestsAPI.id("toast"), ToastReward::new, () -> Icon.getIcon("minecraft:item/oak_sign"));
	RewardType STAGE = RewardTypes.register(FTBQuestsAPI.id("gamestage"), StageReward::new, () -> Icons.CONTROLLER);
	RewardType CURRENCY = RewardTypes.register(FTBQuestsAPI.id("currency"), CurrencyReward::new, () -> Icons.MONEY, false);

	static void init() {
	}
}
