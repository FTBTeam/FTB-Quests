package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;

public class FTBQuestsNetHandler {
	public static void init() {
		NetworkHelper.registerC2S(ChangeChapterGroupMessage.TYPE, ChangeChapterGroupMessage.STREAM_CODEC, ChangeChapterGroupMessage::handle);
		NetworkHelper.registerC2S(ChangeProgressMessage.TYPE, ChangeProgressMessage.STREAM_CODEC, ChangeProgressMessage::handle);
		NetworkHelper.registerC2S(ClaimAllRewardsMessage.TYPE, ClaimAllRewardsMessage.STREAM_CODEC, ClaimAllRewardsMessage::handle);
		NetworkHelper.registerC2S(ClaimChoiceRewardMessage.TYPE, ClaimChoiceRewardMessage.STREAM_CODEC, ClaimChoiceRewardMessage::handle);
		NetworkHelper.registerC2S(ClaimRewardMessage.TYPE, ClaimRewardMessage.STREAM_CODEC, ClaimRewardMessage::handle);
		NetworkHelper.registerC2S(CopyChapterImageMessage.TYPE, CopyChapterImageMessage.STREAM_CODEC, CopyChapterImageMessage::handle);
		NetworkHelper.registerC2S(CopyQuestMessage.TYPE, CopyQuestMessage.STREAM_CODEC, CopyQuestMessage::handle);
		NetworkHelper.registerC2S(CreateObjectMessage.TYPE, CreateObjectMessage.STREAM_CODEC, CreateObjectMessage::handle);
		NetworkHelper.registerC2S(CreateQuestAndTaskMessage.TYPE, CreateQuestAndTaskMessage.STREAM_CODEC, CreateQuestAndTaskMessage::handle);
		NetworkHelper.registerC2S(DeleteObjectMessage.TYPE, DeleteObjectMessage.STREAM_CODEC, DeleteObjectMessage::handle);
		NetworkHelper.registerC2S(EditObjectMessage.TYPE, EditObjectMessage.STREAM_CODEC, EditObjectMessage::handle);
		NetworkHelper.registerC2S(ForceSaveMessage.TYPE, ForceSaveMessage.STREAM_CODEC, ForceSaveMessage::handle);
		NetworkHelper.registerC2S(GetEmergencyItemsMessage.TYPE, GetEmergencyItemsMessage.STREAM_CODEC, GetEmergencyItemsMessage::handle);
		NetworkHelper.registerC2S(MoveChapterGroupMessage.TYPE, MoveChapterGroupMessage.STREAM_CODEC, MoveChapterGroupMessage::handle);
		NetworkHelper.registerC2S(MoveChapterMessage.TYPE, MoveChapterMessage.STREAM_CODEC, MoveChapterMessage::handle);
		NetworkHelper.registerC2S(MoveMovableMessage.TYPE, MoveMovableMessage.STREAM_CODEC, MoveMovableMessage::handle);
		NetworkHelper.registerC2S(RequestTeamDataMessage.TYPE, RequestTeamDataMessage.STREAM_CODEC, RequestTeamDataMessage::handle);
		NetworkHelper.registerC2S(RequestTranslationTableMessage.TYPE, RequestTranslationTableMessage.STREAM_CODEC, RequestTranslationTableMessage::handle);
		NetworkHelper.registerC2S(SetCustomImageMessage.TYPE, SetCustomImageMessage.STREAM_CODEC, SetCustomImageMessage::handle);
		NetworkHelper.registerC2S(SubmitTaskMessage.TYPE, SubmitTaskMessage.STREAM_CODEC, SubmitTaskMessage::handle);
		NetworkHelper.registerC2S(SyncStructuresRequestMessage.TYPE, SyncStructuresRequestMessage.STREAM_CODEC, SyncStructuresRequestMessage::handle);
		NetworkHelper.registerC2S(SyncTranslationMessageToServer.TYPE, SyncTranslationMessageToServer.STREAM_CODEC, SyncTranslationMessageToServer::handle);
		NetworkHelper.registerC2S(TaskScreenConfigResponseMessage.TYPE, TaskScreenConfigResponseMessage.STREAM_CODEC, TaskScreenConfigResponseMessage::handle);
		NetworkHelper.registerC2S(ToggleChapterPinnedMessage.TYPE, ToggleChapterPinnedMessage.STREAM_CODEC, ToggleChapterPinnedMessage::handle);
		NetworkHelper.registerC2S(ToggleEditingModeMessage.TYPE, ToggleEditingModeMessage.STREAM_CODEC, ToggleEditingModeMessage::handle);
		NetworkHelper.registerC2S(TogglePinnedMessage.TYPE, TogglePinnedMessage.STREAM_CODEC, TogglePinnedMessage::handle);

		NetworkHelper.registerS2C(ChangeChapterGroupResponseMessage.TYPE, ChangeChapterGroupResponseMessage.STREAM_CODEC, ChangeChapterGroupResponseMessage::handle);
		NetworkHelper.registerS2C(ClaimRewardResponseMessage.TYPE, ClaimRewardResponseMessage.STREAM_CODEC, ClaimRewardResponseMessage::handle);
		NetworkHelper.registerS2C(ClearDisplayCacheMessage.TYPE, ClearDisplayCacheMessage.STREAM_CODEC, ClearDisplayCacheMessage::handle);
		NetworkHelper.registerS2C(CreateObjectResponseMessage.TYPE, CreateObjectResponseMessage.STREAM_CODEC, CreateObjectResponseMessage::handle);
		NetworkHelper.registerS2C(CreateOtherTeamDataMessage.TYPE, CreateOtherTeamDataMessage.STREAM_CODEC, CreateOtherTeamDataMessage::handle);
		NetworkHelper.registerS2C(DeleteObjectResponseMessage.TYPE, DeleteObjectResponseMessage.STREAM_CODEC, DeleteObjectResponseMessage::handle);
		NetworkHelper.registerS2C(DisplayCompletionToastMessage.TYPE, DisplayCompletionToastMessage.STREAM_CODEC, DisplayCompletionToastMessage::handle);
		NetworkHelper.registerS2C(DisplayItemRewardToastMessage.TYPE, DisplayItemRewardToastMessage.STREAM_CODEC, DisplayItemRewardToastMessage::handle);
		NetworkHelper.registerS2C(DisplayRewardToastMessage.TYPE, DisplayRewardToastMessage.STREAM_CODEC, DisplayRewardToastMessage::handle);
		NetworkHelper.registerS2C(EditObjectResponseMessage.TYPE, EditObjectResponseMessage.STREAM_CODEC, EditObjectResponseMessage::handle);
		NetworkHelper.registerS2C(MoveChapterGroupResponseMessage.TYPE, MoveChapterGroupResponseMessage.STREAM_CODEC, MoveChapterGroupResponseMessage::handle);
		NetworkHelper.registerS2C(MoveChapterResponseMessage.TYPE, MoveChapterResponseMessage.STREAM_CODEC, MoveChapterResponseMessage::handle);
		NetworkHelper.registerS2C(MoveMovableResponseMessage.TYPE, MoveMovableResponseMessage.STREAM_CODEC, MoveMovableResponseMessage::handle);
		NetworkHelper.registerS2C(ObjectCompletedMessage.TYPE, ObjectCompletedMessage.STREAM_CODEC, ObjectCompletedMessage::handle);
		NetworkHelper.registerS2C(ObjectCompletedResetMessage.TYPE, ObjectCompletedResetMessage.STREAM_CODEC, ObjectCompletedResetMessage::handle);
		NetworkHelper.registerS2C(ObjectStartedMessage.TYPE, ObjectStartedMessage.STREAM_CODEC, ObjectStartedMessage::handle);
		NetworkHelper.registerS2C(ObjectStartedResetMessage.TYPE, ObjectStartedResetMessage.STREAM_CODEC, ObjectStartedResetMessage::handle);
		NetworkHelper.registerS2C(OpenQuestBookMessage.TYPE, OpenQuestBookMessage.STREAM_CODEC, OpenQuestBookMessage::handle);
		NetworkHelper.registerS2C(ResetRewardMessage.TYPE, ResetRewardMessage.STREAM_CODEC, ResetRewardMessage::handle);
		NetworkHelper.registerS2C(SyncEditingModeMessage.TYPE, SyncEditingModeMessage.STREAM_CODEC, SyncEditingModeMessage::handle);
		NetworkHelper.registerS2C(SyncEditorPermissionMessage.TYPE, SyncEditorPermissionMessage.STREAM_CODEC, SyncEditorPermissionMessage::handle);
		NetworkHelper.registerS2C(SyncLockMessage.TYPE, SyncLockMessage.STREAM_CODEC, SyncLockMessage::handle);
		NetworkHelper.registerS2C(SyncQuestsMessage.TYPE, SyncQuestsMessage.STREAM_CODEC, SyncQuestsMessage::handle);
		NetworkHelper.registerS2C(SyncRewardBlockingMessage.TYPE, SyncRewardBlockingMessage.STREAM_CODEC, SyncRewardBlockingMessage::handle);
		NetworkHelper.registerS2C(SyncStructuresResponseMessage.TYPE, SyncStructuresResponseMessage.STREAM_CODEC, SyncStructuresResponseMessage::handle);
		NetworkHelper.registerS2C(SyncTeamDataMessage.TYPE, SyncTeamDataMessage.STREAM_CODEC, SyncTeamDataMessage::handle);
		NetworkHelper.registerS2C(SyncTranslationMessageToClient.TYPE, SyncTranslationMessageToClient.STREAM_CODEC, SyncTranslationMessageToClient::handle);
		NetworkHelper.registerS2C(SyncTranslationTableMessage.TYPE, SyncTranslationTableMessage.STREAM_CODEC, SyncTranslationTableMessage::handle);
		NetworkHelper.registerS2C(TaskScreenConfigRequestMessage.TYPE, TaskScreenConfigRequestMessage.STREAM_CODEC, TaskScreenConfigRequestMessage::handle);
		NetworkHelper.registerS2C(TeamDataChangedMessage.TYPE, TeamDataChangedMessage.STREAM_CODEC, TeamDataChangedMessage::handle);
		NetworkHelper.registerS2C(ToggleChapterPinnedResponseMessage.TYPE, ToggleChapterPinnedResponseMessage.STREAM_CODEC, ToggleChapterPinnedResponseMessage::handle);
		NetworkHelper.registerS2C(TogglePinnedResponseMessage.TYPE, TogglePinnedResponseMessage.STREAM_CODEC, TogglePinnedResponseMessage::handle);
		NetworkHelper.registerS2C(UpdateTaskProgressMessage.TYPE, UpdateTaskProgressMessage.STREAM_CODEC, UpdateTaskProgressMessage::handle);
		NetworkHelper.registerS2C(UpdateTeamDataMessage.TYPE, UpdateTeamDataMessage.STREAM_CODEC, UpdateTeamDataMessage::handle);
	}
}