package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public class MissingItem extends Item {
	public MissingItem(ResourceKey<Item> id) {
		super(ModItems.defaultProps().stacksTo(1).setId(id));
	}

	@Override
	public Component getName(ItemStack stack) {
		if (stack.has(ModDataComponents.MISSING_ITEM_DESC.get())) {
			//noinspection DataFlowIssue
			return Component.literal(stack.get(ModDataComponents.MISSING_ITEM_DESC.get()));
		} else {
			return super.getName(stack);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
		if (stack.has(ModDataComponents.MISSING_ITEM_DESC.get())) {
			consumer.accept(Component.translatable("item.ftbquests.missing_item").withStyle(ChatFormatting.RED));
		}
	}
}
