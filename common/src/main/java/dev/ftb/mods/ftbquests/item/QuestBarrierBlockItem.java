package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftbquests.block.FTBQuestsBlocks;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
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

public class QuestBarrierBlockItem extends BlockItem {
	public QuestBarrierBlockItem() {
		super(FTBQuestsBlocks.BARRIER.get(), FTBQuestsItems.defaultProps());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		if (level != null) {
			tooltip.add(Component.translatable("item.ftbquests.barrier.nogui").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
			if (ClientQuestFile.exists() && !ClientQuestFile.INSTANCE.canEdit()) {
				tooltip.add(Component.translatable("item.ftbquests.barrier.disabled").withStyle(ChatFormatting.RED));
			} else {
				tooltip.add(Component.translatable("item.ftbquests.barrier.config").withStyle(ChatFormatting.GRAY));
			}
		}
	}
}
