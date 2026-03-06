package dev.ftb.mods.ftbquests.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import dev.ftb.mods.ftbquests.block.entity.BaseBarrierBlockEntity;
import dev.ftb.mods.ftbquests.registry.ModBlocks;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;

import java.util.function.Consumer;

public class StageBarrierBlockItem extends BlockItem {
	public StageBarrierBlockItem(ResourceKey<Item> id) {
		super(ModBlocks.STAGE_BARRIER.get(), ModItems.defaultProps().setId(id));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag flag) {
		if (context.registries() != null) {
			consumer.accept(Component.translatable("item.ftbquests.barrier.rightclick").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
			BaseBarrierBlockEntity.BarrierSavedData data = stack.get(ModDataComponents.BARRIER_SAVED.get());
			if (data != null) {
				data.addTooltipInfo(data, consumer, "stage_barrier");
			}
		}
	}
}
