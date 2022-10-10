package dev.ftb.mods.ftbquests.integration.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class QuestCategory implements IRecipeCategory<WrappedQuest> {
	public static final ResourceLocation UID = new ResourceLocation(FTBQuests.MOD_ID, "quest");
	public static final ResourceLocation TEXTURE = new ResourceLocation(FTBQuests.MOD_ID + ":textures/gui/jei/quest.png");

	private final IDrawable background;
	private final IDrawable icon;

	public QuestCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(TEXTURE, 0, 0, 144, 74);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(FTBQuestsItems.BOOK.get()));
	}

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<WrappedQuest> getRecipeClass() {
		return WrappedQuest.class;
	}

	@Override
	public Component getTitle() {
		return new TranslatableComponent("ftbquests.quests");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public RecipeType<WrappedQuest> getRecipeType() {
		return JEIRecipeTypes.QUEST;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, WrappedQuest recipe, IFocusGroup focuses) {
		int inputSize = Math.min(9, recipe.input.size());
		for (int i = 0; i < inputSize; i++) {
			builder.addSlot(RecipeIngredientRole.INPUT, (i % 3) * 18 + 1, (i / 3) * 18 + 21)
					.addItemStacks(recipe.input.get(i));
		}

		int outputSize = Math.min(9, recipe.output.size());
		for (int i = 0; i < outputSize; i++) {
			builder.addSlot(RecipeIngredientRole.OUTPUT, (i % 3) * 18 + (5 * 18) + 1, (i / 3) * 18 + 21)
					.addItemStacks(recipe.output.get(i));
		}
	}

	@Override
	public void draw(WrappedQuest recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
		Component text = recipe.quest.getTitle().copy().withStyle(ChatFormatting.UNDERLINE);
		Font font = Minecraft.getInstance().font;
		int w = font.width(text);
		float x = (background.getWidth() - w) / 2f;
		float y = 3f;
		boolean highlight = mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + font.lineHeight;
		font.draw(stack, text, x, y, highlight ? 0xFFA87A5E : 0xFF3F2E23);
	}

	@Override
	public boolean handleInput(WrappedQuest recipe, double mouseX, double mouseY, InputConstants.Key input) {
		if (input.getType() == InputConstants.Type.MOUSE && mouseY >= 0 && mouseY < 20) {
			recipe.openQuestGui();
			return true;
		}
		return false;
	}
}