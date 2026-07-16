package dev.ftb.mods.ftbquests.gametest;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftbquests.gametest.base.FTBQTestRegistrar;
import dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;

import java.util.UUID;

import static dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport.assertFalse;
import static dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport.assertTrue;

public final class ResetTests {

	private ResetTests() {}

	public static void register(FTBQTestRegistrar registrar) {
		registrar.add("reset/reset_progress_keeps_completion_flag", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest = QuestTestSupport.newQuest(chapter);
			Task task = QuestTestSupport.newCheckmarkTask(quest);
			TeamData data = QuestTestSupport.newTeam();

			QuestTestSupport.complete(data, task);
			assertTrue(helper, data.isCompleted(task), "task should be complete");

			data.resetProgress(task);

			assertTrue(helper, data.getProgress(task) == 0L, "resetProgress should clear task progress");
			assertTrue(helper, data.isCompleted(task), "resetProgress leaves the completion flag set (diverges from setProgress(0))");
			helper.succeed();
		}));

		registrar.add("reset/set_progress_zero_clears_completion_flag", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest = QuestTestSupport.newQuest(chapter);
			Task task = QuestTestSupport.newCheckmarkTask(quest);
			TeamData data = QuestTestSupport.newTeam();

			QuestTestSupport.complete(data, task);
			assertTrue(helper, data.isCompleted(task), "task should be complete");

			data.setProgress(task, 0L);

			assertTrue(helper, data.getProgress(task) == 0L, "setProgress(0) should clear task progress");
			assertFalse(helper, data.isCompleted(task), "setProgress(0) should clear the completion flag");
			helper.succeed();
		}));

		registrar.add("reset/set_completed_null_invalidates_dependency_cache", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest questA = QuestTestSupport.newQuest(chapter);
			Task taskA = QuestTestSupport.newCheckmarkTask(questA);
			Quest questB = QuestTestSupport.newQuest(chapter);
			QuestTestSupport.newCheckmarkTask(questB);
			questB.addDependency(questA);

			TeamData data = QuestTestSupport.newTeam();

			QuestTestSupport.complete(data, taskA);
			assertTrue(helper, data.areDependenciesComplete(questB), "B dependencies complete after A completes (caches true)");

			data.setCompleted(questA.id, null);

			assertFalse(helper, data.isCompleted(questA), "A should no longer be completed");
			assertFalse(helper, data.areDependenciesComplete(questB), "B dependency cache should be invalidated by setCompleted(null)");
			helper.succeed();
		}));

		registrar.add("reset/repeatable_completion_count_and_cooldown", 20, helper -> helper.runAfterDelay(1, () -> {
			Json5Object chapterConfig = new Json5Object();
			chapterConfig.addProperty("default_repeatable_quest", true);
			Chapter chapter = QuestTestSupport.newChapter(chapterConfig);

			Json5Object questConfig = new Json5Object();
			questConfig.addProperty("repeat_cooldown", 60);
			Quest quest = QuestTestSupport.newQuest(chapter, questConfig);
			Task task = QuestTestSupport.newCheckmarkTask(quest);
			Reward reward = QuestTestSupport.newXpReward(quest);

			TeamData data = QuestTestSupport.newTeam();
			UUID player = UUID.randomUUID();

			assertTrue(helper, quest.canBeRepeated(), "quest should be repeatable via chapter default");

			QuestTestSupport.complete(data, task);
			assertTrue(helper, data.isCompleted(quest), "quest should complete");
			assertTrue(helper, data.getCompletionCount(quest) == 0, "completion count starts at 0 before a reward is claimed");

			data.markRewardAsClaimed(player, reward, System.currentTimeMillis());

			assertTrue(helper, data.getCompletionCount(quest) == 1, "claiming the reward on a repeatable quest bumps the completion count");
			assertTrue(helper, data.getProgress(task) == 0L, "task progress should reset after a repeatable completion");
			assertTrue(helper, data.getMilliSecondsUntilRepeatable(quest) > 0L, "a cooldown should be active");

			data.clearRepeatCooldown(quest);
			assertTrue(helper, data.getMilliSecondsUntilRepeatable(quest) == 0L, "clearRepeatCooldown should clear the cooldown");
			helper.succeed();
		}));
	}
}
