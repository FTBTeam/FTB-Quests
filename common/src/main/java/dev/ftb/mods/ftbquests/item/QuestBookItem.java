package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftblibrary.core.ItemFTBL;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class QuestBookItem extends Item {
	public QuestBookItem(ResourceKey<Item> id) {
		super(ModItems.defaultProps().stacksTo(1).setId(id));
		((ItemFTBL) this).setCraftingRemainingItemFTBL(this);
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		if (world.isClientSide()) {
			FTBQuestsClient.openGui();
		}

//        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
		return InteractionResult.SUCCESS;
	}

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag flagIn) {
        if (ClientQuestFile.exists() && ClientQuestFile.INSTANCE.isDisableGui() && !ClientQuestFile.INSTANCE.canEdit()) {
            consumer.accept(Component.translatable("item.ftbquests.book.disabled").withStyle(ChatFormatting.RED));
        } else {
            consumer.accept(Component.translatable("item.ftbquests.book.tooltip").withStyle(ChatFormatting.GRAY));
        }
    }
}
