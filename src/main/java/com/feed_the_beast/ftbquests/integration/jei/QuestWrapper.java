package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.RandomReward;
import com.feed_the_beast.ftbquests.quest.task.ItemTask;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.latmod.mods.itemfilters.api.ItemFiltersAPI;
import com.latmod.mods.itemfilters.filters.ORFilter;
import com.latmod.mods.itemfilters.item.ItemFiltersItems;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class QuestWrapper implements IRecipeWrapper
{
	public final Quest quest;
	public final String name;
	public final List<List<ItemStack>> input;
	public final List<List<ItemStack>> output;

	public QuestWrapper(Quest q)
	{
		quest = q;
		name = quest.getTitle();

		input = new ArrayList<>(5);
		output = new ArrayList<>(5);

		if (quest.tasks.size() == 1)
		{
			input.add(Collections.emptyList());
			input.add(Collections.emptyList());
			input.add(Collections.emptyList());
			input.add(Collections.emptyList());
		}

		for (Task task : quest.tasks)
		{
			if (task instanceof ItemTask)
			{
				ORFilter filter = new ORFilter();
				filter.items.addAll(((ItemTask) task).items);
				List<ItemStack> list = new ArrayList<>();
				filter.getValidItems(list);
				input.add(list);
				continue;
			}

			Object object = task.getIngredient();
			ItemStack stack = object instanceof ItemStack ? (ItemStack) object : ItemStack.EMPTY;

			if (!stack.isEmpty())
			{
				List<ItemStack> list = new ArrayList<>();
				ItemFiltersAPI.getValidItems(stack, list);
				input.add(list);
			}
			else if (task.getIcon() instanceof ItemIcon)
			{
				stack = ((ItemIcon) task.getIcon()).getStack().copy();
				stack.setStackDisplayName(task.getTitle());
				input.add(Collections.singletonList(stack));
			}
			else
			{
				stack = new ItemStack(Items.PAINTING);
				stack.setStackDisplayName(task.getTitle());
				stack.setTagInfo("icon", new NBTTagString(task.getIcon().toString()));
				input.add(Collections.singletonList(stack));
			}
		}

		if (quest.rewards.size() == 1)
		{
			output.add(Collections.emptyList());
			output.add(Collections.emptyList());
			output.add(Collections.emptyList());
			output.add(Collections.emptyList());
		}

		for (QuestReward reward : quest.rewards)
		{
			Object object = reward.getIngredient();
			ItemStack stack = object instanceof ItemStack ? (ItemStack) object : ItemStack.EMPTY;

			if (!stack.isEmpty())
			{
				output.add(Collections.singletonList(stack.copy()));
			}
			else if (reward instanceof RandomReward)
			{
				List<ItemStack> list = new ArrayList<>();
				RewardTable table = ((RandomReward) reward).getTable();

				if (table.hideTooltip)
				{
					ItemStack unknown = new ItemStack(ItemFiltersItems.MISSING);
					unknown.setStackDisplayName("Unknown Reward");
					list.add(unknown);
				}
				else
				{
					for (WeightedReward reward1 : table.rewards)
					{
						Object object1 = reward1.reward.getIngredient();

						if (object1 instanceof ItemStack)
						{
							list.add((ItemStack) object1);
						}
					}
				}

				output.add(list);
			}
			else if (reward.getIcon() instanceof ItemIcon)
			{
				stack = ((ItemIcon) reward.getIcon()).getStack().copy();
				stack.setStackDisplayName(reward.getTitle());
				output.add(Collections.singletonList(stack));
			}
			else
			{
				stack = new ItemStack(Items.PAINTING);
				stack.setStackDisplayName(reward.getTitle());
				stack.setTagInfo("icon", new NBTTagString(reward.getIcon().toString()));
				output.add(Collections.singletonList(stack));
			}
		}
	}

	@Override
	public void getIngredients(IIngredients ingredients)
	{
		ingredients.setInputLists(VanillaTypes.ITEM, input);
		ingredients.setOutputLists(VanillaTypes.ITEM, output);
	}

	@Override
	public void drawInfo(Minecraft mc, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		String s = TextFormatting.UNDERLINE + quest.getTitle();
		int w = mc.fontRenderer.getStringWidth(s);
		int h = 9;
		int x = (recipeWidth - w) / 2;
		int y = 3;
		mc.fontRenderer.drawString(s, x, y, (mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + h) ? 0xFFA87A5E : 0xFF3F2E23);
	}

	@Override
	public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton)
	{
		if (mouseY >= 0 && mouseY < 20 && !ClientQuestFile.INSTANCE.disableGui)
		{
			ClientQuestFile.INSTANCE.questTreeGui.open(quest);
			return true;
		}

		return false;
	}
}