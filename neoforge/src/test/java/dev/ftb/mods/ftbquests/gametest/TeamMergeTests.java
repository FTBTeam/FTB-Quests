package dev.ftb.mods.ftbquests.gametest;

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

public final class TeamMergeTests {

	private TeamMergeTests() {}

	public static void register(FTBQTestRegistrar registrar) {
		registrar.add("team/merge_data_combines_progress", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest1 = QuestTestSupport.newQuest(chapter);
			Task task1 = QuestTestSupport.newCheckmarkTask(quest1);
			Quest quest2 = QuestTestSupport.newQuest(chapter);
			Task task2 = QuestTestSupport.newCheckmarkTask(quest2);

			TeamData incoming = QuestTestSupport.newTeam();
			QuestTestSupport.complete(incoming, task1);

			TeamData party = QuestTestSupport.newTeam();
			QuestTestSupport.complete(party, task2);

			party.mergeData(incoming);

			assertTrue(helper, party.isCompleted(quest1), "merged progress from the joining player should be present");
			assertTrue(helper, party.isCompleted(quest2), "the party's own progress should be retained");
			assertTrue(helper, party.getProgress(task1) == task1.getMaxProgress(), "task progress should be max-merged from the joining player");
			helper.succeed();
		}));

		registrar.add("team/copy_data_clones_state", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest = QuestTestSupport.newQuest(chapter);
			Task task = QuestTestSupport.newCheckmarkTask(quest);

			TeamData source = QuestTestSupport.newTeam();
			QuestTestSupport.complete(source, task);
			source.setRewardsBlocked(true);

			TeamData target = QuestTestSupport.newTeam();
			target.copyData(source);

			assertTrue(helper, target.isCompleted(quest), "copied data should include completion state");
			assertTrue(helper, target.getProgress(task) == task.getMaxProgress(), "copied data should include task progress");
			assertTrue(helper, target.areRewardsBlocked(), "copied data should include the rewards-blocked flag");
			helper.succeed();
		}));

		registrar.add("team/merge_claimed_rewards_only_for_own_uuid", 20, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest = QuestTestSupport.newQuest(chapter);
			QuestTestSupport.newCheckmarkTask(quest);
			Reward reward = QuestTestSupport.newXpReward(quest);

			UUID leavingPlayer = UUID.randomUUID();
			UUID otherPlayer = UUID.randomUUID();

			TeamData party = QuestTestSupport.newTeam();
			party.markRewardAsClaimed(leavingPlayer, reward, System.currentTimeMillis());
			party.markRewardAsClaimed(otherPlayer, reward, System.currentTimeMillis());

			TeamData playerTeam = new TeamData(leavingPlayer, true);
			QuestTestSupport.file().addData(playerTeam, true);

			playerTeam.mergeClaimedRewards(party);

			assertTrue(helper, playerTeam.isRewardClaimed(leavingPlayer, reward), "leaving player keeps rewards they personally claimed");
			assertFalse(helper, playerTeam.isRewardClaimed(otherPlayer, reward), "rewards claimed by other members are not carried over");
			helper.succeed();
		}));
	}
}
