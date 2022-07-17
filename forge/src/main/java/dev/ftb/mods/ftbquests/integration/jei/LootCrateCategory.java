//package dev.ftb.mods.ftbquests.integration.jei;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import dev.ftb.mods.ftblibrary.ui.GuiHelper;
//import dev.ftb.mods.ftbquests.FTBQuests;
//import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
//import mezz.jei.api.gui.IRecipeLayout;
//import mezz.jei.api.gui.drawable.IDrawable;
//import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
//import mezz.jei.api.helpers.IGuiHelper;
//import mezz.jei.api.ingredients.IIngredients;
//import mezz.jei.api.recipe.category.IRecipeCategory;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.TranslatableComponent;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ItemStack;
//
///**
// * @author LatvianModder
// */
//public class LootCrateCategory implements IRecipeCategory<LootCrateWrapper> {
//	public static final ResourceLocation UID = new ResourceLocation(FTBQuests.MOD_ID, "lootcrates");
//
//	public static final int ITEMSX = 10;
//	public static final int ITEMSY = 5;
//	public static final int ITEMS = ITEMSX * ITEMSY;
//
//	private final IDrawable background;
//	private final IDrawable icon;
//
//	public LootCrateCategory(IGuiHelper guiHelper) {
//		background = guiHelper.createBlankDrawable(ITEMSX * 18, ITEMSY * 18 + 36);
//		icon = new IDrawable() {
//			@Override
//			public int getWidth() {
//				return 16;
//			}
//
//			@Override
//			public int getHeight() {
//				return 16;
//			}
//
//			@Override
//			public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
//				matrixStack.pushPose();
//				matrixStack.translate(xOffset + 8, yOffset + 8, 100);
//
//				if (!LootCrateRegistry.INSTANCE.list.isEmpty()) {
//					GuiHelper.drawItem(matrixStack, LootCrateRegistry.INSTANCE.list.get((int) ((System.currentTimeMillis() / 1000L) % LootCrateRegistry.INSTANCE.list.size())).itemStack, 0, true, null);
//				} else {
//					GuiHelper.drawItem(matrixStack, new ItemStack(FTBQuestsItems.LOOTCRATE.get()), 0, true, null);
//				}
//
//				matrixStack.popPose();
//			}
//		};
//	}
//
//	@Override
//	public ResourceLocation getUid() {
//		return UID;
//	}
//
//	@Override
//	public Class<LootCrateWrapper> getRecipeClass() {
//		return LootCrateWrapper.class;
//	}
//
//	@Override
//	public Component getTitle() {
//		return Component.translatable("jei.ftbquests.lootcrates");
//	}
//
//	@Override
//	public IDrawable getBackground() {
//		return background;
//	}
//
//	@Override
//	public IDrawable getIcon() {
//		return icon;
//	}
//
//	@Override
//	public void setIngredients(LootCrateWrapper wrapper, IIngredients iIngredients) {
//		//FIXME
//	}
//
//	@Override
//	public void setRecipe(IRecipeLayout layout, LootCrateWrapper entry, IIngredients ingredients) {
//		IGuiItemStackGroup stacks = layout.getItemStacks();
//		stacks.addTooltipCallback(entry);
//
//		for (int slot = 0; slot < Math.min(ITEMS, entry.items.size()); slot++) {
//			stacks.init(slot + 1, false, (slot % ITEMSX) * 18, (slot / ITEMSX) * 18 + 36);
//		}
//
//		stacks.set(ingredients);
//	}
//}
