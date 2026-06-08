package dev.ftb.mods.ftbquests.gametest;

import dev.ftb.mods.ftbquests.gametest.base.FTBQTestRegistrar;
import dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;

import static dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport.assertFalse;
import static dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport.assertTrue;

public final class ProgressionTests {

	private ProgressionTests() {}

	public static void register(FTBQTestRegistrar registrar) {
		registrar.add("progression/file_instance_exists", 20, helper -> helper.runAfterDelay(1, () -> {
			assertTrue(helper, ServerQuestFile.exists(), "ServerQuestFile instance should exist on a running server");
			assertTrue(helper, QuestTestSupport.file() != null, "file() should return the server quest file");
			helper.succeed();
		}));

		registrar.add("progression/single_task_completes_quest", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest = QuestTestSupport.newQuest(chapter);
			Task task = QuestTestSupport.newCheckmarkTask(quest);
			TeamData data = QuestTestSupport.newTeam();

			assertFalse(helper, data.isCompleted(task), "task should start incomplete");
			assertFalse(helper, data.isCompleted(quest), "quest should start incomplete");

			data.setProgress(task, task.getMaxProgress());

			assertTrue(helper, data.isCompleted(task), "task should be complete after full progress");
			assertTrue(helper, data.isCompleted(quest), "quest should complete when its only task completes");
			helper.succeed();
		}));

		registrar.add("progression/multi_task_requires_all", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest = QuestTestSupport.newQuest(chapter);
			Task taskA = QuestTestSupport.newCheckmarkTask(quest);
			Task taskB = QuestTestSupport.newCheckmarkTask(quest);
			TeamData data = QuestTestSupport.newTeam();

			data.setProgress(taskA, taskA.getMaxProgress());

			assertTrue(helper, data.isCompleted(taskA), "task A should be complete");
			assertFalse(helper, data.isCompleted(quest), "quest should not complete with one task remaining");

			data.setProgress(taskB, taskB.getMaxProgress());

			assertTrue(helper, data.isCompleted(taskB), "task B should be complete");
			assertTrue(helper, data.isCompleted(quest), "quest should complete when all tasks are complete");
			helper.succeed();
		}));

		registrar.add("progression/reset_clears_completion", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest = QuestTestSupport.newQuest(chapter);
			Task task = QuestTestSupport.newCheckmarkTask(quest);
			TeamData data = QuestTestSupport.newTeam();

			data.setProgress(task, task.getMaxProgress());
			assertTrue(helper, data.isCompleted(quest), "quest should be complete before reset");

			data.setProgress(task, 0L);

			assertFalse(helper, data.isCompleted(task), "task should be incomplete after reset");
			assertTrue(helper, data.getProgress(task) == 0L, "task progress should be zero after reset");
			helper.succeed();
		}));

		registrar.add("progression/dependency_gates_completion", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest questA = QuestTestSupport.newQuest(chapter);
			Task taskA = QuestTestSupport.newCheckmarkTask(questA);
			Quest questB = QuestTestSupport.newQuest(chapter);
			QuestTestSupport.newCheckmarkTask(questB);
			questB.addDependency(questA);
			QuestTestSupport.file().refreshIDMap();

			TeamData data = QuestTestSupport.newTeam();

			assertTrue(helper, questB.hasDependency(questA), "quest B should depend on quest A");
			assertFalse(helper, data.areDependenciesComplete(questB), "B dependencies should be incomplete before A completes");

			data.setProgress(taskA, taskA.getMaxProgress());

			assertTrue(helper, data.isCompleted(questA), "quest A should complete");
			assertTrue(helper, data.areDependenciesComplete(questB), "B dependencies should be complete after A completes");
			helper.succeed();
		}));
	}
}
