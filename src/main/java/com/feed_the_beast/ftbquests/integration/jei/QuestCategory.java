package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
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
public class QuestCategory implements IRecipeCategory<QuestEntry>
{
	public static final String UID = "ftbquests.quests";

	private final IDrawable background;
	//private final IDrawable arrow;
	private final ItemStack iconStack;
	private final IDrawable icon;
	private final String localizedName;

	public QuestCategory(IGuiHelper guiHelper)
	{
		background = guiHelper.createBlankDrawable(8 * 18 + 2, 3 * 18 + 2);
		//arrow = guiHelper.createDrawable(new ResourceLocation("ftbquests", "textures/gui/arrow.png"), 0, 0, 22, 15);
		iconStack = new ItemStack(FTBQuestsItems.BOOK);
		icon = new IDrawable()
		{
			@Override
			public int getWidth()
			{
				return 16;
			}

			@Override
			public int getHeight()
			{
				return 16;
			}

			@Override
			public void draw(Minecraft minecraft, int xOffset, int yOffset)
			{
				GuiHelper.drawItem(iconStack, xOffset, yOffset, true);
			}
		};

		localizedName = I18n.format("ftbquests.quests") + " [WIP]";
	}

	@Override
	public String getUid()
	{
		return UID;
	}

	@Override
	public String getTitle()
	{
		return localizedName;
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
	public void setRecipe(IRecipeLayout layout, QuestEntry entry, IIngredients ingredients)
	{
		IGuiItemStackGroup stacks = layout.getItemStacks();

		for (int i = 0; i < 9; i++)
		{
			stacks.init(i, true, (i % 3) * 18, (i / 3) * 18);

			if (i < entry.input.size())
			{
				stacks.set(i, entry.input.get(i));
			}
			else
			{
				stacks.set(i, ItemStack.EMPTY);
			}
		}

		for (int i = 0; i < 9; i++)
		{
			stacks.init(i + 9, false, (i % 3) * 18 + (5 * 18), (i / 3) * 18);

			if (i < entry.output.size())
			{
				stacks.set(i + 9, entry.output.get(i));
			}
			else
			{
				stacks.set(i + 9, ItemStack.EMPTY);
			}
		}
	}
}