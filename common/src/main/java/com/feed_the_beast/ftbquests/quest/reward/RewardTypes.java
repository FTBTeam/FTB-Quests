package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

/**
 * @author LatvianModder
 */
public class RewardTypes
{
	public static final HashMap<ResourceLocation, RewardType> TYPES = new HashMap<>();

	public static RewardType register(ResourceLocation name, RewardType.Provider p)
	{
		return TYPES.computeIfAbsent(name, id -> new RewardType(id, p));
	}

	public static RewardType ITEM = register(new ResourceLocation("item"), ItemReward::new).setIcon(Icon.getIcon("minecraft:item/diamond"));
	public static RewardType CHOICE = register(new ResourceLocation("choice"), ChoiceReward::new).setIcon(GuiIcons.COLOR_RGB).setExcludeFromListRewards(true);
	public static RewardType RANDOM = register(new ResourceLocation("random"), RandomReward::new).setIcon(GuiIcons.DICE).setExcludeFromListRewards(true);
	public static RewardType LOOT = register(new ResourceLocation("loot"), LootReward::new).setIcon(GuiIcons.MONEY_BAG).setExcludeFromListRewards(true);
	public static RewardType COMMAND = register(new ResourceLocation("command"), CommandReward::new).setIcon(Icon.getIcon("minecraft:block/command_block_back"));
	public static RewardType CUSTOM = register(new ResourceLocation("custom"), CustomReward::new).setIcon(GuiIcons.COLOR_HSB);
	public static RewardType XP = register(new ResourceLocation("xp"), XPReward::new).setIcon(Icon.getIcon("minecraft:item/experience_bottle"));
	public static RewardType XP_LEVELS = register(new ResourceLocation("xp_levels"), XPLevelsReward::new).setIcon(Icon.getIcon("minecraft:item/experience_bottle"));
	public static RewardType ADVANCEMENT = register(new ResourceLocation("advancement"), AdvancementReward::new).setIcon(Icon.getIcon("minecraft:item/wheat"));
	public static RewardType TOAST = register(new ResourceLocation("toast"), ToastReward::new).setIcon(Icon.getIcon("minecraft:item/oak_sign"));

	public static void init()
	{
	}
}