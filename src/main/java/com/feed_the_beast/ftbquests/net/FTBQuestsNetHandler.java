package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class FTBQuestsNetHandler
{
	public static SimpleChannel MAIN;
	private static final String GENERAL_VERSION = "1";

	public static void init()
	{
		MAIN = NetworkRegistry.ChannelBuilder
				.named(new ResourceLocation(FTBQuests.MOD_ID, "main"))
				.clientAcceptedVersions(GENERAL_VERSION::equals)
				.serverAcceptedVersions(GENERAL_VERSION::equals)
				.networkProtocolVersion(() -> GENERAL_VERSION)
				.simpleChannel();

		int id = 0;

		// Game
		MAIN.registerMessage(++id, MessageSyncQuests.class, MessageSyncQuests::write, MessageSyncQuests::new, MessageSyncQuests::handle);
		MAIN.registerMessage(++id, MessageUpdateTaskProgress.class, MessageUpdateTaskProgress::write, MessageUpdateTaskProgress::new, MessageUpdateTaskProgress::handle);
		MAIN.registerMessage(++id, MessageSubmitTask.class, MessageSubmitTask::write, MessageSubmitTask::new, MessageSubmitTask::handle);
		MAIN.registerMessage(++id, MessageClaimReward.class, MessageClaimReward::write, MessageClaimReward::new, MessageClaimReward::handle);
		MAIN.registerMessage(++id, MessageClaimRewardResponse.class, MessageClaimRewardResponse::write, MessageClaimRewardResponse::new, MessageClaimRewardResponse::handle);
		MAIN.registerMessage(++id, MessageSyncEditingMode.class, MessageSyncEditingMode::write, MessageSyncEditingMode::new, MessageSyncEditingMode::handle);
		MAIN.registerMessage(++id, MessageGetEmergencyItems.class, MessageGetEmergencyItems::write, MessageGetEmergencyItems::new, MessageGetEmergencyItems::handle);
		MAIN.registerMessage(++id, MessageCreatePlayerData.class, MessageCreatePlayerData::write, MessageCreatePlayerData::new, MessageCreatePlayerData::handle);
		MAIN.registerMessage(++id, MessageClaimAllRewards.class, MessageClaimAllRewards::write, MessageClaimAllRewards::new, MessageClaimAllRewards::handle);
		MAIN.registerMessage(++id, MessageClaimChoiceReward.class, MessageClaimChoiceReward::write, MessageClaimChoiceReward::new, MessageClaimChoiceReward::handle);
		MAIN.registerMessage(++id, MessageDisplayCompletionToast.class, MessageDisplayCompletionToast::write, MessageDisplayCompletionToast::new, MessageDisplayCompletionToast::handle);
		MAIN.registerMessage(++id, MessageDisplayRewardToast.class, MessageDisplayRewardToast::write, MessageDisplayRewardToast::new, MessageDisplayRewardToast::handle);
		MAIN.registerMessage(++id, MessageDisplayItemRewardToast.class, MessageDisplayItemRewardToast::write, MessageDisplayItemRewardToast::new, MessageDisplayItemRewardToast::handle);
		MAIN.registerMessage(++id, MessageTogglePinned.class, MessageTogglePinned::write, MessageTogglePinned::new, MessageTogglePinned::handle);
		MAIN.registerMessage(++id, MessageTogglePinnedResponse.class, MessageTogglePinnedResponse::write, MessageTogglePinnedResponse::new, MessageTogglePinnedResponse::handle);

		// Editing
		MAIN.registerMessage(++id, MessageChangeProgress.class, MessageChangeProgress::write, MessageChangeProgress::new, MessageChangeProgress::handle);
		MAIN.registerMessage(++id, MessageChangeProgressResponse.class, MessageChangeProgressResponse::write, MessageChangeProgressResponse::new, MessageChangeProgressResponse::handle);
		MAIN.registerMessage(++id, MessageCreateObject.class, MessageCreateObject::write, MessageCreateObject::new, MessageCreateObject::handle);
		MAIN.registerMessage(++id, MessageCreateObjectResponse.class, MessageCreateObjectResponse::write, MessageCreateObjectResponse::new, MessageCreateObjectResponse::handle);
		MAIN.registerMessage(++id, MessageCreateTaskAt.class, MessageCreateTaskAt::write, MessageCreateTaskAt::new, MessageCreateTaskAt::handle);
		MAIN.registerMessage(++id, MessageDeleteObject.class, MessageDeleteObject::write, MessageDeleteObject::new, MessageDeleteObject::handle);
		MAIN.registerMessage(++id, MessageDeleteObjectResponse.class, MessageDeleteObjectResponse::write, MessageDeleteObjectResponse::new, MessageDeleteObjectResponse::handle);
		MAIN.registerMessage(++id, MessageEditObject.class, MessageEditObject::write, MessageEditObject::new, MessageEditObject::handle);
		MAIN.registerMessage(++id, MessageEditObjectResponse.class, MessageEditObjectResponse::write, MessageEditObjectResponse::new, MessageEditObjectResponse::handle);
		MAIN.registerMessage(++id, MessageMoveChapter.class, MessageMoveChapter::write, MessageMoveChapter::new, MessageMoveChapter::handle);
		MAIN.registerMessage(++id, MessageMoveChapterResponse.class, MessageMoveChapterResponse::write, MessageMoveChapterResponse::new, MessageMoveChapterResponse::handle);
		MAIN.registerMessage(++id, MessageMoveQuest.class, MessageMoveQuest::write, MessageMoveQuest::new, MessageMoveQuest::handle);
		MAIN.registerMessage(++id, MessageMoveQuestResponse.class, MessageMoveQuestResponse::write, MessageMoveQuestResponse::new, MessageMoveQuestResponse::handle);
	}
}