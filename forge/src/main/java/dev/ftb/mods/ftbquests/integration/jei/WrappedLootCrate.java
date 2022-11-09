package dev.ftb.mods.ftbquests.integration.jei;

import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class WrappedLootCrate {
	public final LootCrate crate;
	public final ItemStack crateStack;
	public final List<WeightedReward> sortedRewards;
	public final List<ItemStack> outputs;
	public final List<List<ItemStack>> cycledOutputs;

	public WrappedLootCrate(LootCrate c) {
		crate = c;
		crateStack = crate.createStack();
		outputs = new ArrayList<>(c.table.rewards.size());
		sortedRewards = c.table.rewards.stream().sorted(WeightedReward::compareTo).toList();

		for (WeightedReward reward : sortedRewards) {
			Object object = reward.reward.getIngredient();
			ItemStack stack = object instanceof ItemStack ? (ItemStack) object : ItemStack.EMPTY;

			if (!stack.isEmpty()) {
				outputs.add(stack.copy());
			} else if (reward.reward.getIcon() instanceof ItemIcon) {
				stack = ((ItemIcon) reward.reward.getIcon()).getStack().copy();
				stack.setHoverName(reward.reward.getTitle());
				outputs.add(stack);
			} else {
				stack = new ItemStack(Items.PAINTING);
				stack.setHoverName(reward.reward.getTitle());
				stack.addTagElement("icon", StringTag.valueOf(reward.reward.getIcon().toString()));
				outputs.add(stack);
			}
		}

		if (outputs.size() <= LootCrateCategory.ITEMS) {
			cycledOutputs = new ArrayList<>(outputs.size());

			for (ItemStack stack : outputs) {
				cycledOutputs.add(Collections.singletonList(stack));
			}
		} else {
			// too many items to fit in display; cycle them
			cycledOutputs = new ArrayList<>(LootCrateCategory.ITEMS);

			for (int i = 0; i < LootCrateCategory.ITEMS; i++) {
				cycledOutputs.add(new ArrayList<>());
			}

			for (int i = 0; i < outputs.size(); i++) {
				cycledOutputs.get(i % LootCrateCategory.ITEMS).add(outputs.get(i));
			}
		}
	}
}