package dev.ftb.mods.ftbquests.item;

import com.mojang.datafixers.util.Either;
import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.config.EntityFaceConfig;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.icon.EntityIconLoader;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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
	public Component getName(ItemStack stack) {
		return getCustomComponent(stack)
				.map(res -> (Component) res.map(
						rl -> Component.translatable("ftbquests.custom_icon.texture", rl.toString()),
						et -> Component.translatable("ftbquests.custom_icon.entity", et.getDescription())
				))
				.orElse(super.getName(stack));
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
		return getCustomComponent(stack)
				.map(res -> res.map(Icon::getIcon, EntityIconLoader::getIcon))
				.orElse(stack.getItem() instanceof CustomIconItem ? Icon.getIcon(FALLBACK_ICON) : ItemIcon.getItemIcon(stack));
	}

	public static void setIcon(ItemStack stack, @Nullable ResourceLocation texture) {
		stack.remove(ModDataComponents.ENTITY_FACE_ICON.get());
		if (texture == null || texture.equals(ImageResourceConfig.NONE)) {
			stack.remove(ModDataComponents.CUSTOM_ICON.get());
		} else {
			stack.set(ModDataComponents.CUSTOM_ICON.get(), texture);
		}
	}

	public static void setFaceIcon(ItemStack stack, ResourceLocation value) {
		stack.remove(ModDataComponents.CUSTOM_ICON.get());
		if (value == null || value.equals(EntityFaceConfig.NONE)) {
			stack.remove(ModDataComponents.ENTITY_FACE_ICON.get());
		} else {
			stack.set(ModDataComponents.ENTITY_FACE_ICON.get(), value);
		}
	}

	public static void setFaceIcon(ItemStack stack, EntityType<?> value) {
		setFaceIcon(stack, RegistrarManager.getId(value, Registries.ENTITY_TYPE));
	}

	private static Optional<Either<ResourceLocation,EntityType<?>>> getCustomComponent(ItemStack stack) {
		if (stack.has(ModDataComponents.CUSTOM_ICON.get())) {
			return Optional.of(Either.left(stack.get(ModDataComponents.CUSTOM_ICON.get())));
		} else if (stack.has(ModDataComponents.ENTITY_FACE_ICON.get())) {
			return Optional.of(Either.right(BuiltInRegistries.ENTITY_TYPE.get(stack.get(ModDataComponents.ENTITY_FACE_ICON.get()))));
		}
		return Optional.empty();
	}
}
