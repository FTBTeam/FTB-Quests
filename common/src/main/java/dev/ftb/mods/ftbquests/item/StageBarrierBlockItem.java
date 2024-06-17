package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftbquests.registry.ModBlocks;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class StageBarrierBlockItem extends BlockItem {
	public StageBarrierBlockItem() {
		super(ModBlocks.STAGE_BARRIER.get(), ModItems.defaultProps());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("item.ftbquests.barrier.nogui").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("item.ftbquests.stage_barrier.config").withStyle(ChatFormatting.GRAY));
	}
}
