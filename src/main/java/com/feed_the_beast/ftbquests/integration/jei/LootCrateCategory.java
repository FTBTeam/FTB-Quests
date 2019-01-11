package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public class LootCrateCategory implements IRecipeCategory<LootCrateEntry>
{
	public static final String UID = "ftbquests.lootcrates";

	public static final int ITEMSX = 10;
	public static final int ITEMSY = 7;

	private final IDrawable background;
	//private final IDrawable arrow;
	private final IDrawable icon;
	private final String localizedName;

	public LootCrateCategory(IGuiHelper guiHelper)
	{
		background = guiHelper.createBlankDrawable(ITEMSX * 18 + 2, ITEMSY * 18);
		//arrow = guiHelper.createDrawable(new ResourceLocation("ftbquests", "textures/gui/arrow.png"), 0, 0, 22, 15);
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

		localizedName = I18n.format("jei.ftbquests.lootcrates");
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
	public void setRecipe(IRecipeLayout layout, LootCrateEntry entry, IIngredients ingredients)
	{
		layout.getItemStacks().addTooltipCallback(entry);
		layout.getItemStacks().init(0, true, 0, 0);
		layout.getItemStacks().set(0, entry.itemStack);

		int slot = 1;

		for (ItemStack stack : entry.items)
		{
			layout.getItemStacks().init(slot, false, (slot % ITEMSX) * 18, (slot / ITEMSX) * 18);
			layout.getItemStacks().set(slot, stack);
			slot++;

			if (slot == ITEMSX * ITEMSY)
			{
				break;
			}
		}
	}
}