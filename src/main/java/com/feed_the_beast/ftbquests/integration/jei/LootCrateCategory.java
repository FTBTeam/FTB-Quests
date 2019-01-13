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
public class LootCrateCategory implements IRecipeCategory<LootCrateWrapper>
{
	public static final String UID = "ftbquests.lootcrates";

	public static final int ITEMSX = 10;
	public static final int ITEMSY = 6;

	private final IDrawable background;
	private final IDrawable icon;

	public LootCrateCategory(IGuiHelper guiHelper)
	{
		background = guiHelper.createBlankDrawable(ITEMSX * 18 + 2, (ITEMSY + 1) * 18);
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
				if (!LootCrateRegistry.INSTANCE.list.isEmpty())
				{
					GuiHelper.drawItem(LootCrateRegistry.INSTANCE.list.get((int) ((System.currentTimeMillis() / 1000L) % LootCrateRegistry.INSTANCE.list.size())).itemStack, xOffset, yOffset, true);
				}
				else
				{
					GuiHelper.drawItem(new ItemStack(FTBQuestsItems.LOOTCRATE), xOffset, yOffset, true);
				}
			}
		};
	}

	@Override
	public String getUid()
	{
		return UID;
	}

	@Override
	public String getTitle()
	{
		return I18n.format("jei.ftbquests.lootcrates");
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
	public void setRecipe(IRecipeLayout layout, LootCrateWrapper entry, IIngredients ingredients)
	{
		IGuiItemStackGroup stacks = layout.getItemStacks();

		stacks.addTooltipCallback(entry);
		stacks.init(0, true, 81, 0);

		for (int slot = 0; slot < Math.min(ITEMSX * ITEMSY, entry.items.size()); slot++)
		{
			stacks.init(slot + 1, false, (slot % ITEMSX) * 18, (slot / ITEMSX) * 18 + 18);
		}

		stacks.set(ingredients);
	}
}