package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.block.FTBQuestsBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StageBarrierBlockItem extends BlockItem {
	public StageBarrierBlockItem() {
		super(FTBQuestsBlocks.STAGE_BARRIER.get(), new Properties().tab(FTBQuests.ITEM_GROUP));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("item.ftbquests.barrier.nogui").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("item.ftbquests.stage_barrier.config").withStyle(ChatFormatting.GRAY));
	}
}
