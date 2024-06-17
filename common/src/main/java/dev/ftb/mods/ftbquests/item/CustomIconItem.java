package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

public class CustomIconItem extends Item {
	private static final ResourceLocation FALLBACK_ICON = ResourceLocation.withDefaultNamespace("textures/misc/unknown_pack.png");

	public CustomIconItem() {
		super(ModItems.defaultProps().stacksTo(1)
				.component(ModDataComponents.CUSTOM_ICON.get(), FALLBACK_ICON)
		);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		if (level.isClientSide()) {
			FTBQuestsClient.openCustomIconGui(player, interactionHand);
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(interactionHand));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable("item.ftbquests.custom_icon.tooltip").withStyle(ChatFormatting.GRAY));

		String icon = FTBQuests.getComponent(stack, ModDataComponents.CUSTOM_ICON)
				.map(ResourceLocation::toString)
				.orElse("-");
		tooltip.add(Component.literal(icon).withStyle(ChatFormatting.DARK_GRAY));
	}

	public static Icon getIcon(ItemStack stack) {
		if (stack.getItem() instanceof CustomIconItem) {
			ResourceLocation icon =  FTBQuests.getComponent(stack, ModDataComponents.CUSTOM_ICON).orElse(FALLBACK_ICON);
			return Icon.getIcon(icon);
		} else {
			return ItemIcon.getItemIcon(stack);
		}
	}

	public static void setIcon(ItemStack stack, @Nullable ResourceLocation texture) {
		if (texture == null || texture.equals(ImageResourceConfig.NONE)) {
			stack.remove(ModDataComponents.CUSTOM_ICON.get());
		} else {
			stack.set(ModDataComponents.CUSTOM_ICON.get(), texture);
		}
	}
}
