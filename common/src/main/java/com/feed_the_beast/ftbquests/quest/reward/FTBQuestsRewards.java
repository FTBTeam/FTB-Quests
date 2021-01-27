package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import me.shedaniel.architectury.registry.DeferredRegister;
import me.shedaniel.architectury.registry.RegistrySupplier;
import net.minecraft.resources.ResourceKey;

/**
 * @author LatvianModder
 */
public class FTBQuestsRewards
{
	public static DeferredRegister<RewardType> TYPES = DeferredRegister.create(FTBQuests.MOD_ID, (ResourceKey) RewardType.getRegistry().key());
	public static RegistrySupplier<RewardType> ITEM = TYPES.register("item", () -> new RewardType(ItemReward::new).setIcon(Icon.getIcon("minecraft:item/diamond")));
	public static RegistrySupplier<RewardType> CHOICE = TYPES.register("choice", () -> new RewardType(ChoiceReward::new).setIcon(GuiIcons.COLOR_RGB).setExcludeFromListRewards(true));
	public static RegistrySupplier<RewardType> RANDOM = TYPES.register("random", () -> new RewardType(RandomReward::new).setIcon(GuiIcons.DICE).setExcludeFromListRewards(true));
	public static RegistrySupplier<RewardType> LOOT = TYPES.register("loot", () -> new RewardType(LootReward::new).setIcon(GuiIcons.MONEY_BAG).setExcludeFromListRewards(true));
	public static RegistrySupplier<RewardType> COMMAND = TYPES.register("command", () -> new RewardType(CommandReward::new).setIcon(Icon.getIcon("minecraft:block/command_block_back")));
	public static RegistrySupplier<RewardType> CUSTOM = TYPES.register("custom", () -> new RewardType(CustomReward::new).setIcon(GuiIcons.COLOR_HSB));
	public static RegistrySupplier<RewardType> XP = TYPES.register("xp", () -> new RewardType(XPReward::new).setIcon(Icon.getIcon("minecraft:item/experience_bottle")));
	public static RegistrySupplier<RewardType> XP_LEVELS = TYPES.register("xp_levels", () -> new RewardType(XPLevelsReward::new).setIcon(Icon.getIcon("minecraft:item/experience_bottle")));
	public static RegistrySupplier<RewardType> ADVANCEMENT = TYPES.register("advancement", () -> new RewardType(AdvancementReward::new).setIcon(Icon.getIcon("minecraft:item/wheat")));
	public static RegistrySupplier<RewardType> TOAST = TYPES.register("toast", () -> new RewardType(ToastReward::new).setIcon(Icon.getIcon("minecraft:item/oak_sign")));

	public static void register() {
		TYPES.register();
	}
}