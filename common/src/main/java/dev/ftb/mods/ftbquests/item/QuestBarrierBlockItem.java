package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.registry.ModBlocks;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class QuestBarrierBlockItem extends BlockItem {
	public QuestBarrierBlockItem() {
		super(ModBlocks.BARRIER.get(), ModItems.defaultProps());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		if (context.registries() != null) {
			tooltip.add(Component.translatable("item.ftbquests.barrier.nogui").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
			if (ClientQuestFile.exists() && !ClientQuestFile.INSTANCE.canEdit()) {
				tooltip.add(Component.translatable("item.ftbquests.barrier.disabled").withStyle(ChatFormatting.RED));
			} else {
				tooltip.add(Component.translatable("item.ftbquests.barrier.config").withStyle(ChatFormatting.GRAY));
			}
		}
	}
}
