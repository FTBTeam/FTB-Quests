package dev.ftb.mods.ftbquests.gametest;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftbquests.gametest.base.FTBQTestRegistrar;
import dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;

import static dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport.assertFalse;
import static dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport.assertTrue;

public final class ChapterProgressionTests {

	private ChapterProgressionTests() {}

	private static Json5Object progressionMode(String id) {
		Json5Object config = new Json5Object();
		config.addProperty("progression_mode", id);
		return config;
	}

	public static void register(FTBQTestRegistrar registrar) {
		registrar.add("chapter/completes_when_all_quests_complete", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest1 = QuestTestSupport.newQuest(chapter);
			Task task1 = QuestTestSupport.newCheckmarkTask(quest1);
			Quest quest2 = QuestTestSupport.newQuest(chapter);
			Task task2 = QuestTestSupport.newCheckmarkTask(quest2);
			TeamData data = QuestTestSupport.newTeam();

			QuestTestSupport.complete(data, task1);
			assertTrue(helper, data.isCompleted(quest1), "first quest should complete");
			assertFalse(helper, data.isCompleted(chapter), "chapter should not complete with one quest remaining");

			QuestTestSupport.complete(data, task2);
			assertTrue(helper, data.isCompleted(quest2), "second quest should complete");
			assertTrue(helper, data.isCompleted(chapter), "chapter should complete once all its quests are complete");
			helper.succeed();
		}));

		registrar.add("progression/linear_gates_tasks_flexible_allows", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest gate = QuestTestSupport.newQuest(chapter);
			Task gateTask = QuestTestSupport.newCheckmarkTask(gate);

			Quest linear = QuestTestSupport.newQuest(chapter, progressionMode("linear"));
			QuestTestSupport.newCheckmarkTask(linear);
			linear.addDependency(gate);

			Quest flexible = QuestTestSupport.newQuest(chapter, progressionMode("flexible"));
			QuestTestSupport.newCheckmarkTask(flexible);
			flexible.addDependency(gate);

			TeamData data = QuestTestSupport.newTeam();

			assertFalse(helper, data.canStartTasks(linear), "LINEAR quest cannot start tasks while its dependency is incomplete");
			assertTrue(helper, data.canStartTasks(flexible), "FLEXIBLE quest can start tasks even before its dependency completes");

			QuestTestSupport.complete(data, gateTask);

			assertTrue(helper, data.canStartTasks(linear), "LINEAR quest can start tasks once its dependency completes");
			helper.succeed();
		}));
	}
}
