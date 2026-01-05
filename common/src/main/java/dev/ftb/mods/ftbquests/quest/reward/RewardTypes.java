package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface RewardTypes {
	Map<Identifier, RewardType> TYPES = new LinkedHashMap<>();

	static RewardType register(Identifier name, RewardType.Provider typeProvider, Supplier<Icon> iconSupplier, boolean availableByDefault) {
		return TYPES.computeIfAbsent(name, id -> new RewardType(id, typeProvider, iconSupplier, availableByDefault));
	}

	static RewardType register(Identifier name, RewardType.Provider typeProvider, Supplier<Icon> iconSupplier) {
		return register(name, typeProvider, iconSupplier, true);
	}

	RewardType ITEM = register(FTBQuestsAPI.rl("item"), ItemReward::new, () -> Icon.getIcon("minecraft:item/diamond"));
	RewardType CHOICE = register(FTBQuestsAPI.rl("choice"), ChoiceReward::new, () -> Icons.COLOR_RGB).setExcludeFromListRewards(true);
	RewardType ALL_TABLE = register(FTBQuestsAPI.rl("all_table"), AllTableReward::new, () -> Icons.COLOR_HSB).setExcludeFromListRewards(true);
	RewardType RANDOM = register(FTBQuestsAPI.rl("random"), RandomReward::new, () -> Icons.DICE).setExcludeFromListRewards(true);
	RewardType LOOT = register(FTBQuestsAPI.rl("loot"), LootReward::new, () -> Icons.MONEY_BAG).setExcludeFromListRewards(true);
	RewardType COMMAND = register(FTBQuestsAPI.rl("command"), CommandReward::new, () -> Icon.getIcon("minecraft:block/command_block_back"));
	RewardType CUSTOM = register(FTBQuestsAPI.rl("custom"), CustomReward::new, () -> Icons.COLOR_HSB);
	RewardType XP = register(FTBQuestsAPI.rl("xp"), XPReward::new, () -> Icon.getIcon("minecraft:item/experience_bottle"));
	RewardType XP_LEVELS = register(FTBQuestsAPI.rl("xp_levels"), XPLevelsReward::new, () -> Icon.getIcon("minecraft:item/experience_bottle"));
	RewardType ADVANCEMENT = register(FTBQuestsAPI.rl("advancement"), AdvancementReward::new, () -> Icon.getIcon("minecraft:item/wheat"));
	RewardType TOAST = register(FTBQuestsAPI.rl("toast"), ToastReward::new, () -> Icon.getIcon("minecraft:item/oak_sign"));
	RewardType STAGE = RewardTypes.register(FTBQuestsAPI.rl("gamestage"), StageReward::new, () -> Icons.CONTROLLER);
	RewardType CURRENCY = RewardTypes.register(FTBQuestsAPI.rl("currency"), CurrencyReward::new, () -> Icons.MONEY, false);

	static void init() {
	}
}
