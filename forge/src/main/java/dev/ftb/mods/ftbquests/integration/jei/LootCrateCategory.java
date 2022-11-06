package dev.ftb.mods.ftbquests.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
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

import java.util.List;

/**
 * @author LatvianModder
 */
public class LootCrateCategory implements IRecipeCategory<WrappedLootCrate> {
	public static final ResourceLocation UID = new ResourceLocation(FTBQuests.MOD_ID, "loot_crate");

	public static final int ITEMSX = 10;
	public static final int ITEMSY = 5;
	public static final int ITEMS = ITEMSX * ITEMSY;

	private final IDrawable background;
	private final IDrawable icon;

	public LootCrateCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(ITEMSX * 18, ITEMSY * 18 + 36);
		icon = new IDrawable() {
			@Override
			public int getWidth() {
				return 16;
			}

			@Override
			public int getHeight() {
				return 16;
			}

			@Override
			public void draw(PoseStack poseStack, int xOffset, int yOffset) {
				poseStack.pushPose();
				poseStack.translate(xOffset + 8, yOffset + 8, 100);

				List<WrappedLootCrate> crates = LootCrateRecipeManagerPlugin.INSTANCE.getWrappedLootCrates();
				if (!crates.isEmpty()) {
					GuiHelper.drawItem(poseStack, crates.get((int) ((System.currentTimeMillis() / 1000L) % crates.size())).crateStack, 0, true, null);
				} else {
					GuiHelper.drawItem(poseStack, new ItemStack(FTBQuestsItems.LOOTCRATE.get()), 0, true, null);
				}

				poseStack.popPose();
			}
		};
	}

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<WrappedLootCrate> getRecipeClass() {
		return WrappedLootCrate.class;
	}

	@Override
	public RecipeType<WrappedLootCrate> getRecipeType() {
		return JEIRecipeTypes.LOOT_CRATE;
	}

	@Override
	public Component getTitle() {
		return new TranslatableComponent("jei.ftbquests.lootcrates");
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
	public void setRecipe(IRecipeLayoutBuilder builder, WrappedLootCrate recipe, IFocusGroup focuses) {
		for (int slot = 0; slot < Math.min(ITEMS, recipe.outputs.size()); slot++) {
			int finalSlot = slot;
			builder.addSlot(RecipeIngredientRole.OUTPUT, (slot % ITEMSX) * 18, (slot / ITEMSX) * 18 + 36)
					.addItemStacks(recipe.cycledOutputs.get(slot))
					.addTooltipCallback((recipeSlotView, tooltip) -> recipeSlotView.getDisplayedIngredient()
							.flatMap(ingr -> ingr.getIngredient(VanillaTypes.ITEM_STACK)).ifPresent(stack -> {
								if (ItemStack.isSame(stack, recipe.outputs.get(finalSlot))) {
									String chanceStr = ChatFormatting.GOLD + WeightedReward.chanceString(
											recipe.sortedRewards.get(finalSlot).weight,
											recipe.crate.table.getTotalWeight(true)
									);
									tooltip.add(new TranslatableComponent("jei.ftbquests.lootcrates.chance", chanceStr)
											.withStyle(ChatFormatting.GRAY));
								}
							})
					);
		}
	}

	@Override
	public void draw(WrappedLootCrate recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
		LootCrate crate = recipe.crate;

		Font font = Minecraft.getInstance().font;

		font.drawShadow(poseStack, crate.table.getMutableTitle().withStyle(ChatFormatting.UNDERLINE), 0, 0, 0xFFFFFF00);

		int total = ClientQuestFile.INSTANCE.lootCrateNoDrop.passive;
		for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables) {
			if (table.lootCrate != null) {
				total += table.lootCrate.drops.passive;
			}
		}
		Component p = chance("passive", crate.drops.passive, total);

		total = ClientQuestFile.INSTANCE.lootCrateNoDrop.monster;
		for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables) {
			if (table.lootCrate != null) {
				total += table.lootCrate.drops.monster;
			}
		}
		Component m = chance("monster", crate.drops.monster, total);

		total = ClientQuestFile.INSTANCE.lootCrateNoDrop.boss;
		for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables) {
			if (table.lootCrate != null) {
				total += table.lootCrate.drops.boss;
			}
		}
		Component b = chance("boss", crate.drops.boss, total);

		int w = Math.max(font.width(p), Math.max(font.width(m), font.width(b)));
		int drawX = background.getWidth() - w - 2;
		font.draw(poseStack, p, drawX, 0, 0xFF404040);
		font.draw(poseStack, m, drawX, font.lineHeight, 0xFF404040);
		font.draw(poseStack, b, drawX, font.lineHeight * 2, 0xFF404040);
	}

	private Component chance(String type, int w, int t) {
		return new TranslatableComponent("ftbquests.loot.entitytype." + type).append(": " + WeightedReward.chanceString(w, t, true));
	}
}