package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.core.ItemFTBQ;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class QuestBookItem extends Item {
	public QuestBookItem() {
		super(new Properties().stacksTo(1).tab(FTBQuests.ITEM_GROUP));
		((ItemFTBQ) this).setCraftingRemainingItemFTBQ(this);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		if (world.isClientSide()) {
			FTBQuests.PROXY.openGui();
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (ClientQuestFile.exists() && ClientQuestFile.INSTANCE.disableGui && !ClientQuestFile.INSTANCE.self.getCanEdit()) {
			tooltip.add(new TranslatableComponent("item.ftbquests.book.disabled").withStyle(ChatFormatting.RED));
		} else {
			tooltip.add(new TranslatableComponent("item.ftbquests.book.tooltip").withStyle(ChatFormatting.GRAY));
		}
	}
}