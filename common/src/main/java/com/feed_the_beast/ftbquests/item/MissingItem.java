package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftbquests.FTBQuests;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class MissingItem extends Item {
	private static final ResourceLocation AIR = new ResourceLocation("minecraft:air");

	public static ItemStack readItem(CompoundTag tag) {
		if (tag.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ResourceLocation id = new ResourceLocation(tag.getString("id"));

		if (id.equals(AIR)) {
			return ItemStack.EMPTY;
		}

		Item item = Registry.ITEM.get(id);

		if (item == Items.AIR) {
			ItemStack stack = new ItemStack(FTBQuestsItems.MISSING_ITEM.get());
			stack.addTagElement("Item", tag);
			return stack;
		}

		return ItemStack.of(tag);
	}

	public static CompoundTag writeItem(ItemStack stack) {
		if (stack.getItem() instanceof MissingItem && stack.hasTag() && stack.getTag().contains("Item")) {
			return stack.getTag().getCompound("Item");
		}

		return stack.save(new CompoundTag());
	}

	public MissingItem() {
		super(new Properties().stacksTo(1).tab(FTBQuests.ITEM_GROUP));
	}

	@Override
	public Component getName(ItemStack stack) {
		if (stack.hasTag() && stack.getTag().contains("Item")) {
			CompoundTag tag = stack.getTag().getCompound("Item");
			String id = tag.getString("id");
			int c = Math.max(1, tag.getInt("Count"));

			if (c == 1) {
				return new TextComponent(id);
			} else {
				return new TextComponent(c + "x " + id);
			}
		}

		return super.getName(stack);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (stack.hasTag() && stack.getTag().contains("Item")) {
			tooltip.add(new TranslatableComponent("item.ftbquests.missing_item").withStyle(ChatFormatting.RED));
		}
	}
}
