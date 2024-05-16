package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MissingItem extends Item {
	private static final ResourceLocation AIR = new ResourceLocation("minecraft:air");

	public static ItemStack readItem(CompoundTag tag, HolderLookup.Provider provider) {
		if (tag.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ResourceLocation id = new ResourceLocation(tag.getString("id"));

		if (id.equals(AIR)) {
			return ItemStack.EMPTY;
		}

		Item item = BuiltInRegistries.ITEM.get(id);

		if (item == Items.AIR) {
			return Util.make(new ItemStack(ModItems.MISSING_ITEM.get()),
					s -> s.set(ModDataComponents.MISSING_ITEM.get(), tag));
		}

		return ItemStack.parseOptional(provider, tag);
	}

	public static CompoundTag writeItem(ItemStack stack, HolderLookup.Provider provider) {
		if (stack.getItem() instanceof MissingItem && stack.has(ModDataComponents.MISSING_ITEM.get())) {
			return stack.get(ModDataComponents.MISSING_ITEM.get());
		}

		SNBTCompoundTag tag = new SNBTCompoundTag();
		stack.save(provider, tag);

		if (tag.size() == 2) {
			tag.singleLine();
		}

		return tag;
	}

	public MissingItem() {
		super(ModItems.defaultProps().stacksTo(1));
	}

	@Override
	public Component getName(ItemStack stack) {
		CompoundTag tag = stack.get(ModDataComponents.MISSING_ITEM.get());
		if (tag != null) {
			String id = tag.getString("id");
			int count = Math.max(1, tag.getInt("Count"));
            return count == 1 ?
					Component.literal(id) :
					Component.literal(count + "x " + id);
		}

		return super.getName(stack);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		if (stack.has(ModDataComponents.MISSING_ITEM.get())) {
			tooltip.add(Component.translatable("item.ftbquests.missing_item").withStyle(ChatFormatting.RED));
		}
	}
}
