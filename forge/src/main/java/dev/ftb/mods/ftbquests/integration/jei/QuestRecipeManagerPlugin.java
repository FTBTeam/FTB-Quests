package dev.ftb.mods.ftbquests.integration.jei;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public enum QuestRecipeManagerPlugin implements IRecipeManagerPlugin {
    INSTANCE;

    private final List<WrappedQuest> wrappedQuestsCache = new ArrayList<>();
    private boolean needsRefresh = true;

    private List<WrappedQuest> getWrappedQuests() {
        if (needsRefresh) {
            rebuildWrappedQuests();
            needsRefresh = false;
        }
        return wrappedQuestsCache;
    }

    private void rebuildWrappedQuests() {
        wrappedQuestsCache.clear();

        if (ClientQuestFile.exists()) {
            for (ChapterGroup group : ClientQuestFile.INSTANCE.chapterGroups) {
                for (Chapter chapter : group.chapters) {
                    for (Quest quest : chapter.quests) {
                        if (ClientQuestFile.INSTANCE.self.canStartTasks(quest) && !quest.rewards.isEmpty() && !quest.disableJEI.get(ClientQuestFile.INSTANCE.defaultQuestDisableJEI)) {
                            List<Reward> rewards = quest.rewards.stream()
                                    .filter(reward -> reward.getAutoClaimType() != RewardAutoClaim.INVISIBLE && reward.getIngredient() != null)
                                    .toList();
                            if (!rewards.isEmpty()) {
                                wrappedQuestsCache.add(new WrappedQuest(quest, rewards));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Called whenever client quests are updated (either by GUI or by sync from server)
     */
    public void refresh() {
        needsRefresh = true;
    }

    @Override
    public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        if (recipeCategory instanceof QuestCategory && focus.getTypedValue().getIngredient() instanceof ItemStack stack) {
            // (List<T>) casts should be safe since we've verified the category
            if (stack.getItem() == FTBQuestsItems.BOOK.get() && focus.getRole() == RecipeIngredientRole.CATALYST) {
                //noinspection unchecked
                return (List<T>) getWrappedQuests();
            }
            return switch (focus.getRole()) {
                case INPUT -> //noinspection unchecked
                        (List<T>) findQuestsWithInput(stack);
                case OUTPUT -> //noinspection unchecked
                        (List<T>) findQuestsWithOutput(stack);
                default -> Collections.emptyList();
            };
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
        if (recipeCategory instanceof QuestCategory) {
            // safe to cast since we verified the category already
            //noinspection unchecked
            return (List<T>) getWrappedQuests();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
        V value = focus.getTypedValue().getIngredient();
        if( !(value instanceof ItemStack stack) ) return Collections.emptyList();

        if (focus.getRole() == RecipeIngredientRole.INPUT && stack.getItem() == FTBQuestsItems.BOOK.get()) {
            return List.of(JEIRecipeTypes.QUEST);
        }
        return Collections.emptyList();
    }

    @Override
    public <V> List<ResourceLocation> getRecipeCategoryUids(IFocus<V> focus) {
        // TODO 1.19 delete method
        return List.of(new ResourceLocation(FTBQuests.MOD_ID, "quest"));
    }

    // TODO caching for these
    private List<WrappedQuest> findQuestsWithInput(ItemStack stack) {
        return getWrappedQuests().stream()
                .filter(q -> q.hasInput(stack))
                .toList();
    }

    private List<WrappedQuest> findQuestsWithOutput(ItemStack stack) {
        return getWrappedQuests().stream()
                .filter(q -> q.hasOutput(stack))
                .toList();
    }
}