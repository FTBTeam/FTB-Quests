package dev.ftb.mods.ftbquests.item;

import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5Ops;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.IdentifierException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.function.Consumer;

public class MissingItem extends Item {
	public MissingItem(ResourceKey<Item> id) {
		super(ModItems.defaultProps().stacksTo(1).setId(id));
	}

	/**
	 * Create a MissingItem with component data describing the original item's id, count & components - enough data
	 * to attempt to restore it at a later date, e.g. if a missing mod is installed. This is used when the "id" field
	 * of the Json5 object isn't a known item ID in this instance's item registry.
	 *
	 * @param json the Json5 object for the item (a serialized ItemStack)
	 * @return a new MissingItem itemstack
	 */
	public static ItemStack createFromJson(Json5Object json) {
		return Util.make(new ItemStack(ModItems.MISSING_ITEM.get()), stack -> {
			String id = Json5Util.getString(json, "id").orElse("unknown");
			int count = Math.max(1, Json5Util.getInt(json, "count").orElse(1));
			Json5Element components = json.get("components");

			// save as much info as we can about the item - we might be able to reconstruct it at a later date
			stack.set(ModDataComponents.MISSING_ITEM_ID.get(), id);
            stack.set(ModDataComponents.MISSING_ITEM_COUNT.get(), count);
			if (components != null) {
				stack.set(ModDataComponents.MISSING_ITEM_DATA.get(), components);
			}
        });
	}

	/**
	 * See if it's possible to restore an item from a MissingItem's component data.
	 *
	 * @param stack the stack to try and restore
	 * @param provider lookup provider
	 * @return if the stack isn't a MissingItem or doesn't have the right component data, just return it;
	 * otherwise try to recreate the original item from the MissingItem's component data
	 */
	public static ItemStack maybeRestoreItem(ItemStack stack, HolderLookup.Provider provider) {
		if (stack.getItem() instanceof MissingItem && stack.has(ModDataComponents.MISSING_ITEM_ID.get())) {
			// we've read a missing item, but it's possible the item has become valid since last run
			// e.g. if a mod is now installed which wasn't before
			String idStr = stack.get(ModDataComponents.MISSING_ITEM_ID.get());
			assert idStr != null;
			if (idStr.contains(" ")) {
				// could be a legacy string, e.g. "10x minecraft:oak_planks"
				return tryParseLegacy(stack, idStr);
			}
			Identifier id = Identifier.tryParse(idStr);
			if (id == null) {
				return stack;
			}
			return BuiltInRegistries.ITEM.get(id).map(itemRef -> {
				int count = stack.getOrDefault(ModDataComponents.MISSING_ITEM_COUNT.get(), 1);
				Json5Element components = stack.getOrDefault(ModDataComponents.MISSING_ITEM_DATA.get(), new Json5Object());
				RegistryOps<Json5Element> ops = provider.createSerializationContext(Json5Ops.INSTANCE);
				var patch = DataComponentPatch.CODEC.parse(ops, components).result().orElse(DataComponentPatch.EMPTY);
				FTBQuests.LOGGER.info("restored missing item {}", itemRef.value());
				return new ItemStack(itemRef, count, patch);
			}).orElse(stack);
		} else {
			return stack;
		}
	}

    private static ItemStack tryParseLegacy(ItemStack stack, String id) {
        String[] fields = id.split(" ", 2);
		if (fields.length == 2 && fields[0].endsWith("x") && fields[0].length() > 1) {
			String countStr = fields[0].substring(0, fields[0].length() - 1);
			if (NumberUtils.isParsable(countStr)) {
				try {
					return BuiltInRegistries.ITEM.get(Identifier.parse(fields[1]))
							.map(item -> new ItemStack(item, Integer.parseInt(countStr)))
							.orElse(stack);
				} catch (IdentifierException e) {
					return stack;
				}
			}
		}
		return stack;
    }

	@Override
	public Component getName(ItemStack stack) {
		if (stack.has(ModDataComponents.MISSING_ITEM_ID.get())) {
			//noinspection DataFlowIssue
			return Component.literal(stack.get(ModDataComponents.MISSING_ITEM_ID.get()));
		} else {
			return super.getName(stack);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
		if (stack.has(ModDataComponents.MISSING_ITEM_ID.get())) {
			consumer.accept(Component.translatable("item.ftbquests.missing_item").withStyle(ChatFormatting.RED));
		}
	}
}
