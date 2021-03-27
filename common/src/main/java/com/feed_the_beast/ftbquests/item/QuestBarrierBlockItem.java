package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.block.FTBQuestsBlocks;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QuestBarrierBlockItem extends BlockItem {
	public QuestBarrierBlockItem() {
		super(FTBQuestsBlocks.BARRIER.get(), new Properties().tab(FTBQuests.ITEM_GROUP));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(new TranslatableComponent("item.ftbquests.barrier.nogui").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		if (ClientQuestFile.exists() && !ClientQuestFile.INSTANCE.self.getCanEdit()) {
			tooltip.add(new TranslatableComponent("item.ftbquests.barrier.disabled").withStyle(ChatFormatting.RED));
		} else {
			tooltip.add(new TranslatableComponent("item.ftbquests.barrier.config").withStyle(ChatFormatting.GRAY));
		}
	}
}
