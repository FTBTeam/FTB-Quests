package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public class QuestCategory implements IRecipeCategory<QuestWrapper>
{
	public static final String UID = "ftbquests.quests";

	private final IDrawable background;
	//private final IDrawable arrow;
	private final IDrawable icon;

	public QuestCategory(IGuiHelper guiHelper)
	{
		background = guiHelper.createBlankDrawable(8 * 18 + 2, 3 * 18 + 2);
		//arrow = guiHelper.createDrawable(new ResourceLocation("ftbquests", "textures/gui/arrow.png"), 0, 0, 22, 15);
		icon = guiHelper.createDrawableIngredient(new ItemStack(FTBQuestsItems.BOOK));
	}

	@Override
	public String getUid()
	{
		return UID;
	}

	@Override
	public String getTitle()
	{
		return I18n.format("ftbquests.quests") + " [WIP]";
	}

	@Override
	public String getModName()
	{
		return FTBQuests.MOD_NAME;
	}

	@Override
	public IDrawable getBackground()
	{
		return background;
	}

	@Override
	public IDrawable getIcon()
	{
		return icon;
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
		//arrow.draw(minecraft, 75, 18);
	}

	@Override
	public void setRecipe(IRecipeLayout layout, QuestWrapper entry, IIngredients ingredients)
	{
		IGuiItemStackGroup stacks = layout.getItemStacks();
		int is = Math.min(9, entry.input.size());

		for (int i = 0; i < is; i++)
		{
			stacks.init(i, true, (i % 3) * 18, (i / 3) * 18);
		}

		for (int i = 0; i < Math.min(9, entry.output.size()); i++)
		{
			stacks.init(i + is, false, (i % 3) * 18 + (5 * 18), (i / 3) * 18);
		}

		stacks.set(ingredients);
	}
}