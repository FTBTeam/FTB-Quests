package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MissingItem extends Item {
	public MissingItem() {
		super(ModItems.defaultProps().stacksTo(1));
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
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		if (stack.has(ModDataComponents.MISSING_ITEM_DESC.get())) {
			tooltip.add(Component.translatable("item.ftbquests.missing_item").withStyle(ChatFormatting.RED));
		}
	}
}
