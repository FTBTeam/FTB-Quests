package dev.ftb.mods.ftbquests.quest;

@FunctionalInterface
public interface Excludable {
    /**
     * Check for mutually exclusive questlines. A quest is considered <em>excluded</em> if:
     * <ol>
     *     <li>It has 1 or more dependents (quests that must be completed first)</li>
     *     <li>Any of those dependents has <code>maxCompletableDeps > 0</code></li>
     *     <li>Such dependents already have at least <code>maxCompletableDeps</code> dependencies completed</li>
     * </ol>
     * <p>This should generally called via {@link TeamData#isExcludedByOtherQuestline(QuestObject)}, which handles caching
     * of the result for performance.</p>
     * @param teamData the team to check
     * @return true if the quest is excluded; it will not be shown (outside edit mode), progressable, or completable
     */
    boolean isQuestObjectExcluded(TeamData teamData);
}
