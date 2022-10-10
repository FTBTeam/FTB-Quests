package dev.ftb.mods.ftbquests.integration.jei;

import com.google.common.collect.ImmutableList;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.WrappedIngredient;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.RandomReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.latvian.mods.itemfilters.api.ItemFiltersAPI;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class WrappedQuest {
    final Quest quest;
    final List<List<ItemStack>> input;
    final List<List<ItemStack>> output;

    public WrappedQuest(Quest q, List<Reward> rewards) {
        quest = q;

        input = new ArrayList<>(5);
        output = new ArrayList<>(5);

        if (quest.tasks.size() == 1) {
            // padding to center the ingredient in a 3x3 grid
            input.add(Collections.emptyList());
            input.add(Collections.emptyList());
            input.add(Collections.emptyList());
            input.add(Collections.emptyList());
        }

        for (Task task : quest.tasks) {
            if (task instanceof ItemTask itemTask) {
                input.add(Collections.singletonList(itemTask.item));
            } else {
                Object object = task.getIngredient();
                ItemStack stack = object instanceof ItemStack ? (ItemStack) object : ItemStack.EMPTY;

                if (!stack.isEmpty()) {
                    List<ItemStack> list = new ArrayList<>();
                    ItemFiltersAPI.getDisplayItemStacks(stack, list);
                    input.add(List.copyOf(list));
                } else if (task.getIcon() instanceof ItemIcon itemIcon) {
                    stack = itemIcon.getStack().copy();
                    stack.setHoverName(task.getTitle());
                    input.add(Collections.singletonList(stack));
                } else {
                    stack = new ItemStack(Items.PAINTING);
                    stack.setHoverName(task.getTitle());
                    stack.addTagElement("icon", StringTag.valueOf(task.getIcon().toString()));
                    input.add(Collections.singletonList(stack));
                }
            }
        }

        if (rewards.size() == 1) {
            // padding to center the ingredient in a 3x3 grid
            output.add(Collections.emptyList());
            output.add(Collections.emptyList());
            output.add(Collections.emptyList());
            output.add(Collections.emptyList());
        }

        for (Reward reward : rewards) {
            Object object = reward.getIngredient();
            ItemStack stack = ItemStack.EMPTY;
            if (object instanceof ItemStack s) {
                stack = s;
            } else if (object instanceof WrappedIngredient w && w.wrappedIngredient instanceof ItemStack s) {
                stack = s;
            }

            if (!stack.isEmpty()) {
                output.add(Collections.singletonList(stack.copy()));
            } else if (reward instanceof RandomReward r) {
                RewardTable table = r.getTable();
                if (table != null) {
                    ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
                    if (table.hideTooltip) {
                        ItemStack unknown = new ItemStack(Items.BARRIER);
                        unknown.setHoverName(new TextComponent("Unknown Reward"));
                        builder.add(unknown);
                    } else {
                        for (WeightedReward wr : table.rewards) {
                            if (wr.reward.getIngredient() instanceof ItemStack s) {
                                builder.add(s);
                            }
                        }
                    }
                    output.add(builder.build());
                }
            } else if (reward.getIcon() instanceof ItemIcon itemIcon) {
                stack = itemIcon.getStack().copy();
                stack.setHoverName(reward.getTitle());
                output.add(Collections.singletonList(stack));
            } else {
                stack = new ItemStack(Items.PAINTING);
                stack.setHoverName(reward.getTitle());
                stack.addTagElement("icon", StringTag.valueOf(reward.getIcon().toString()));
                output.add(Collections.singletonList(stack));
            }
        }
    }

	public boolean hasInput(ItemStack stack) {
		for (var l : input) {
			for (var stack1 : l) {
				if (ItemStack.isSame(stack1, stack)) return true;
			}
		}
		return false;
	}

	public boolean hasOutput(ItemStack stack) {
		for (var l : output) {
			for (var stack1 : l) {
				if (ItemStack.isSame(stack1, stack)) return true;
			}
		}
		return false;
	}

    void openQuestGui() {
        ClientQuestFile.INSTANCE.questScreen.open(quest, true);
    }
}