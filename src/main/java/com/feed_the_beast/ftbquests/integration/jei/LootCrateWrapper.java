package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class LootCrateWrapper implements /*IRecipeWrapper, */ITooltipCallback<ItemStack>
{
	public final LootCrate crate;
	public final IFormattableTextComponent name;
	public final ItemStack itemStack;
	public final List<ItemStack> items;
	public final List<WeightedReward> rewards;
	public final List<List<ItemStack>> itemLists;

	public LootCrateWrapper(LootCrate c)
	{
		crate = c;
		name = crate.table.getTitle();
		itemStack = crate.createStack();
		items = new ArrayList<>(c.table.rewards.size());

		rewards = new ArrayList<>(c.table.rewards);
		rewards.sort(null);

		for (WeightedReward reward : rewards)
		{
			Object object = reward.reward.getIngredient();
			ItemStack stack = object instanceof ItemStack ? (ItemStack) object : ItemStack.EMPTY;

			if (!stack.isEmpty())
			{
				items.add(stack.copy());
			}
			else if (reward.reward.getIcon() instanceof ItemIcon)
			{
				stack = ((ItemIcon) reward.reward.getIcon()).getStack().copy();
				stack.setDisplayName(reward.reward.getTitle());
				items.add(stack);
			}
			else
			{
				stack = new ItemStack(Items.PAINTING);
				stack.setDisplayName(reward.reward.getTitle());
				stack.setTagInfo("icon", StringNBT.valueOf(reward.reward.getIcon().toString()));
				items.add(stack);
			}
		}

		if (items.size() <= LootCrateCategory.ITEMS)
		{
			itemLists = new ArrayList<>(items.size());

			for (ItemStack stack : items)
			{
				itemLists.add(Collections.singletonList(stack));
			}
		}
		else
		{
			itemLists = new ArrayList<>(LootCrateCategory.ITEMS);

			for (int i = 0; i < LootCrateCategory.ITEMS; i++)
			{
				itemLists.add(new ArrayList<>());
			}

			for (int i = 0; i < items.size(); i++)
			{
				itemLists.get(i % LootCrateCategory.ITEMS).add(items.get(i));
			}
		}
	}

	//FIXME: @Override
	public void getIngredients(IIngredients ingredients)
	{
		ingredients.setInput(VanillaTypes.ITEM, itemStack);
		ingredients.setOutputLists(VanillaTypes.ITEM, itemLists);
	}

	private String chance(String type, int w, int t)
	{
		String s = I18n.format("ftbquests.loot.entitytype." + type) + ": " + WeightedReward.chanceString(w, t);

		if (w > 0)
		{
			s += " (1 in " + StringUtils.formatDouble00(1D / ((double) w / (double) t)) + ")";
		}

		return s;
	}

	//FIXME: @Override
	public void drawInfo(MatrixStack matrixStack, Minecraft mc, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		GuiHelper.drawItem(matrixStack, itemStack, 0, 0, 2, 2, true, null);
		mc.fontRenderer.func_243246_a(matrixStack, crate.table.getTitle().mergeStyle(TextFormatting.UNDERLINE), 36, 0, 0xFF222222);

		int total = ClientQuestFile.INSTANCE.lootCrateNoDrop.passive;

		for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables)
		{
			if (table.lootCrate != null)
			{
				total += table.lootCrate.drops.passive;
			}
		}

		mc.fontRenderer.drawString(matrixStack, chance("passive", crate.drops.passive, total), 36, 10, 0xFF222222);

		total = ClientQuestFile.INSTANCE.lootCrateNoDrop.monster;

		for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables)
		{
			if (table.lootCrate != null)
			{
				total += table.lootCrate.drops.monster;
			}
		}

		mc.fontRenderer.drawString(matrixStack, chance("monster", crate.drops.monster, total), 36, 19, 0xFF222222);

		total = ClientQuestFile.INSTANCE.lootCrateNoDrop.boss;

		for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables)
		{
			if (table.lootCrate != null)
			{
				total += table.lootCrate.drops.boss;
			}
		}

		mc.fontRenderer.drawString(matrixStack, chance("boss", crate.drops.boss, total), 36, 28, 0xFF222222);
	}

	@Override
	public void onTooltip(int slot, boolean input, ItemStack ingredient, List<ITextComponent> tooltip)
	{
		if (slot > 0 && slot - 1 < items.size())
		{
			for (int i = 0; i < items.size(); i++)
			{
				if (items.get(i) == ingredient)
				{
					tooltip.add(new TranslationTextComponent("jei.ftbquests.lootcrates.chance", TextFormatting.GOLD + WeightedReward.chanceString(rewards.get(i).weight, crate.table.getTotalWeight(true))).mergeStyle(TextFormatting.GRAY));
					return;
				}
			}
		}
	}
}