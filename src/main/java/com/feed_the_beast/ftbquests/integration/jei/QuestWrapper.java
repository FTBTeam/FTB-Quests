package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class QuestWrapper implements IRecipeWrapper
{
	public final Quest quest;
	public final String name;
	public final List<ItemStack> input;
	public final List<ItemStack> output;

	public QuestWrapper(Quest q)
	{
		quest = q;
		name = quest.getDisplayName().getFormattedText();

		input = new ArrayList<>();
		output = new ArrayList<>();

		for (QuestTask task : quest.tasks)
		{
			Object object = task.getJEIFocus();
			ItemStack stack = object instanceof ItemStack ? (ItemStack) object : ItemStack.EMPTY;

			if (!stack.isEmpty())
			{
				input.add(stack.copy());
			}
			else if (task.getIcon() instanceof ItemIcon)
			{
				stack = ((ItemIcon) task.getIcon()).getStack().copy();
				stack.setStackDisplayName(task.getDisplayName().getFormattedText());
				input.add(stack);
			}
			else
			{
				stack = new ItemStack(Items.PAINTING);
				stack.setStackDisplayName(task.getDisplayName().getFormattedText());
				stack.setTagInfo("icon", new NBTTagString(task.getIcon().toString()));
				input.add(stack);
			}
		}

		for (QuestReward reward : quest.rewards)
		{
			Object object = reward.getJEIFocus();
			ItemStack stack = object instanceof ItemStack ? (ItemStack) object : ItemStack.EMPTY;

			if (!stack.isEmpty())
			{
				input.add(stack.copy());
			}
			else if (reward.getIcon() instanceof ItemIcon)
			{
				stack = ((ItemIcon) reward.getIcon()).getStack().copy();
				stack.setStackDisplayName(reward.getDisplayName().getFormattedText());
				input.add(stack);
			}
			else
			{
				stack = new ItemStack(Items.PAINTING);
				stack.setStackDisplayName(reward.getDisplayName().getFormattedText());
				stack.setTagInfo("icon", new NBTTagString(reward.getIcon().toString()));
				input.add(stack);
			}
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void getIngredients(IIngredients ingredients)
	{
		ingredients.setInputs(ItemStack.class, input);
		ingredients.setOutputs(ItemStack.class, output);
	}
}