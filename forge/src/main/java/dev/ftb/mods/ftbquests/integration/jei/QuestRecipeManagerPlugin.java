package dev.ftb.mods.ftbquests.integration.jei;

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
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public enum QuestRecipeManagerPlugin implements IRecipeManagerPlugin {
    INSTANCE;

    private final List<WrappedQuest> wrappedQuestsCache = new ArrayList<>();
    private final ItemStackToListCache<WrappedQuest> inputCache = new ItemStackToListCache<>();
    private final ItemStackToListCache<WrappedQuest> outputCache = new ItemStackToListCache<>();
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

        inputCache.clear();
        outputCache.clear();
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
                default -> List.of();
            };
        } else {
            return List.of();
        }
    }

    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
        // safe to cast since we verified the category already
        //noinspection unchecked
        return recipeCategory instanceof QuestCategory ? (List<T>) getWrappedQuests() : List.of();
    }

    @Override
    public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
        if (focus.getTypedValue().getIngredient() instanceof ItemStack stack) {
            if (focus.getRole() == RecipeIngredientRole.INPUT && (stack.getItem() == FTBQuestsItems.BOOK.get() || !findQuestsWithInput(stack).isEmpty())
                    || focus.getRole() == RecipeIngredientRole.OUTPUT && !findQuestsWithOutput(stack).isEmpty()) {
                return List.of(JEIRecipeTypes.QUEST);
            }
        }

        return List.of();

    }

    private List<WrappedQuest> findQuestsWithInput(ItemStack stack) {
        return inputCache.getList(stack, k ->
                getWrappedQuests().stream()
                        .filter(q -> q.hasInput(stack))
                        .toList()
        );
    }

    private List<WrappedQuest> findQuestsWithOutput(ItemStack stack) {
        return outputCache.getList(stack, k -> getWrappedQuests().stream()
                .filter(q -> q.hasOutput(stack))
                .toList()
        );
    }
}