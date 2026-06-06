package dev.ftb.mods.ftbquests.gametest;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftbquests.gametest.base.FTBQTestRegistrar;
import dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;

import java.util.Date;

import static dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport.assertFalse;
import static dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport.assertTrue;

public final class OptionalDependencyTests {

	private OptionalDependencyTests() {}

	private static Json5Object dependencyRequirement(String id) {
		Json5Object config = new Json5Object();
		config.addProperty("dependency_requirement", id);
		return config;
	}

	public static void register(FTBQTestRegistrar registrar) {
		registrar.add("optional/either_or_one_optional_completes_quest", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest = QuestTestSupport.newQuest(chapter);
			Task optionalA = QuestTestSupport.newCheckmarkTask(quest, true);
			QuestTestSupport.newCheckmarkTask(quest, true);
			TeamData data = QuestTestSupport.newTeam();

			assertFalse(helper, data.isCompleted(quest), "all-optional quest starts incomplete");

			QuestTestSupport.complete(data, optionalA);

			assertTrue(helper, data.isCompleted(quest), "completing one of two optional tasks completes an either/or quest");
			helper.succeed();
		}));

		registrar.add("optional/required_blocks_optional_does_not", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest = QuestTestSupport.newQuest(chapter);
			Task required = QuestTestSupport.newCheckmarkTask(quest, false);
			Task optional = QuestTestSupport.newCheckmarkTask(quest, true);

			TeamData onlyOptional = QuestTestSupport.newTeam();
			QuestTestSupport.complete(onlyOptional, optional);
			assertFalse(helper, onlyOptional.isCompleted(quest), "completing only the optional task must not complete the quest");

			TeamData onlyRequired = QuestTestSupport.newTeam();
			QuestTestSupport.complete(onlyRequired, required);
			assertTrue(helper, onlyRequired.isCompleted(quest), "completing the required task completes the quest, optional ignored");
			helper.succeed();
		}));

		registrar.add("dependency/one_completed_unlocks", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest questA = QuestTestSupport.newQuest(chapter);
			Task taskA = QuestTestSupport.newCheckmarkTask(questA);
			Quest questB = QuestTestSupport.newQuest(chapter);
			QuestTestSupport.newCheckmarkTask(questB);

			Quest target = QuestTestSupport.newQuest(chapter, dependencyRequirement("one_completed"));
			QuestTestSupport.newCheckmarkTask(target);
			target.addDependency(questA);
			target.addDependency(questB);
			QuestTestSupport.file().refreshIDMap();

			TeamData data = QuestTestSupport.newTeam();
			assertFalse(helper, data.areDependenciesComplete(target), "ONE_COMPLETED unsatisfied before any dependency completes");

			QuestTestSupport.complete(data, taskA);

			assertTrue(helper, data.areDependenciesComplete(target), "ONE_COMPLETED satisfied when a single dependency completes");
			helper.succeed();
		}));

		registrar.add("dependency/min_required_dependencies", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest questA = QuestTestSupport.newQuest(chapter);
			Task taskA = QuestTestSupport.newCheckmarkTask(questA);
			Quest questB = QuestTestSupport.newQuest(chapter);
			Task taskB = QuestTestSupport.newCheckmarkTask(questB);
			Quest questC = QuestTestSupport.newQuest(chapter);
			QuestTestSupport.newCheckmarkTask(questC);

			Json5Object config = new Json5Object();
			config.addProperty("min_required_dependencies", 2);
			Quest target = QuestTestSupport.newQuest(chapter, config);
			QuestTestSupport.newCheckmarkTask(target);
			target.addDependency(questA);
			target.addDependency(questB);
			target.addDependency(questC);
			QuestTestSupport.file().refreshIDMap();

			TeamData data = QuestTestSupport.newTeam();

			QuestTestSupport.complete(data, taskA);
			assertFalse(helper, data.areDependenciesComplete(target), "1 of 3 completed is below the required minimum of 2");

			QuestTestSupport.complete(data, taskB);
			assertTrue(helper, data.areDependenciesComplete(target), "2 of 3 completed meets the required minimum of 2");
			helper.succeed();
		}));

		registrar.add("dependency/all_started_vs_completed", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest questA = QuestTestSupport.newQuest(chapter);
			QuestTestSupport.newCheckmarkTask(questA);

			Quest target = QuestTestSupport.newQuest(chapter, dependencyRequirement("all_started"));
			QuestTestSupport.newCheckmarkTask(target);
			target.addDependency(questA);
			QuestTestSupport.file().refreshIDMap();

			TeamData data = QuestTestSupport.newTeam();
			assertFalse(helper, data.areDependenciesComplete(target), "ALL_STARTED unsatisfied before the dependency is started");

			data.setStarted(questA.id, new Date());

			assertFalse(helper, data.isCompleted(questA), "dependency is started but not completed");
			assertTrue(helper, data.areDependenciesComplete(target), "ALL_STARTED is satisfied by a started (not completed) dependency");
			helper.succeed();
		}));

		registrar.add("exclusion/max_completable_excludes_sibling", 20, helper -> helper.runAfterDelay(1, () -> {
			Json5Object hubConfig = new Json5Object();
			hubConfig.addProperty("max_completable_dependents", 1);

			Chapter chapter = QuestTestSupport.newChapter();
			Quest hub = QuestTestSupport.newQuest(chapter, hubConfig);
			Task hubTask = QuestTestSupport.newCheckmarkTask(hub);

			Quest branchB = QuestTestSupport.newQuest(chapter);
			Task branchBTask = QuestTestSupport.newCheckmarkTask(branchB);
			branchB.addDependency(hub);

			Quest branchC = QuestTestSupport.newQuest(chapter);
			QuestTestSupport.newCheckmarkTask(branchC);
			branchC.addDependency(hub);
			QuestTestSupport.file().refreshIDMap();

			TeamData data = QuestTestSupport.newTeam();

			QuestTestSupport.complete(data, hubTask);
			assertFalse(helper, data.isExcludedByOtherQuestline(branchC), "sibling not excluded before the other branch completes");

			QuestTestSupport.complete(data, branchBTask);

			assertTrue(helper, data.isExcludedByOtherQuestline(branchC), "completing branch B excludes branch C (max 1 completable dependent)");
			assertFalse(helper, data.isExcludedByOtherQuestline(branchB), "the completed branch is not itself excluded");
			helper.succeed();
		}));
	}
}
