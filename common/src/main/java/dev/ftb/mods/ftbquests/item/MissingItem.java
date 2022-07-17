package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.FTBQuests;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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

		SNBTCompoundTag tag = new SNBTCompoundTag();
		stack.save(tag);

		if (tag.size() == 2) {
			tag.singleLine();
		}

		return tag;
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
				return Component.literal(id);
			} else {
				return Component.literal(c + "x " + id);
			}
		}

		return super.getName(stack);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (stack.hasTag() && stack.getTag().contains("Item")) {
			tooltip.add(Component.translatable("item.ftbquests.missing_item").withStyle(ChatFormatting.RED));
		}
	}
}
