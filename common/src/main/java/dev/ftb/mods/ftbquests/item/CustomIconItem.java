package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.FTBQuests;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author LatvianModder
 */
public class CustomIconItem extends Item {
	public CustomIconItem() {
		super(new Properties().stacksTo(1).tab(FTBQuests.ITEM_GROUP));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		if (level.isClientSide()) {
			FTBQuests.PROXY.openCustomIconGui(player, interactionHand);
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(interactionHand));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(new TranslatableComponent("item.ftbquests.custom_icon.tooltip").withStyle(ChatFormatting.GRAY));

		if (stack.hasTag() && stack.getTag().contains("Icon")) {
			tooltip.add(new TextComponent(stack.getTag().getString("Icon")).withStyle(ChatFormatting.DARK_GRAY));
		} else {
			tooltip.add(new TextComponent("-").withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	public static Icon getIcon(ItemStack stack) {
		if (stack.getItem() instanceof CustomIconItem) {
			if (stack.hasTag() && stack.getTag().contains("Icon")) {
				return Icon.getIcon(stack.getTag().getString("Icon"));
			}

			return Icon.getIcon("minecraft:textures/misc/unknown_pack.png");
		}

		return ItemIcon.getItemIcon(stack);
	}
}
