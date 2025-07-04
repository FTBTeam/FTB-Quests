package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftblibrary.core.ItemFTBL;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class QuestBookItem extends Item {
	public QuestBookItem() {
		super(ModItems.defaultProps().stacksTo(1));
		((ItemFTBL) this).setCraftingRemainingItemFTBL(this);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		if (world.isClientSide()) {
			FTBQuestsClient.openGui();
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		if (ClientQuestFile.exists() && ClientQuestFile.INSTANCE.isDisableGui() && !ClientQuestFile.INSTANCE.canEdit()) {
			tooltip.add(Component.translatable("item.ftbquests.book.disabled").withStyle(ChatFormatting.RED));
		} else {
			tooltip.add(Component.translatable("item.ftbquests.book.tooltip").withStyle(ChatFormatting.GRAY));
		}
	}
}
