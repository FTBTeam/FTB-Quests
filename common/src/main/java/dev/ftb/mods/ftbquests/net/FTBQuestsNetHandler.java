package dev.ftb.mods.ftbquests.net;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import dev.ftb.mods.ftbquests.FTBQuests;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public interface FTBQuestsNetHandler {
	SimpleNetworkManager NET = SimpleNetworkManager.create(FTBQuests.MOD_ID);

	MessageType SYNC_QUESTS = NET.registerS2C("sync_quests", SyncQuestsMessage::new);
	MessageType SYNC_TEAM_DATA = NET.registerS2C("sync_team_data", SyncTeamDataMessage::new);
	MessageType UPDATE_TASK_PROGRESS = NET.registerS2C("update_task_progress", UpdateTaskProgressMessage::new);
	MessageType SUBMIT_TASK = NET.registerC2S("submit_task", SubmitTaskMessage::new);
	MessageType CLAIM_REWARD = NET.registerC2S("claim_reward", ClaimRewardMessage::new);
	MessageType CLAIM_REWARD_RESPONSE = NET.registerS2C("claim_reward_response", ClaimRewardResponseMessage::new);
	MessageType SYNC_EDITING_MODE = NET.registerS2C("sync_editing_mode", SyncEditingModeMessage::new);
	MessageType GET_EMERGENCY_ITEMS = NET.registerC2S("get_emergency_items", GetEmergencyItemsMessage::new);
	MessageType CREATE_OTHER_TEAM_DATA = NET.registerS2C("create_other_team_data", CreateOtherTeamDataMessage::new);
	MessageType CLAIM_ALL_REWARDS = NET.registerC2S("claim_all_rewards", ClaimAllRewardsMessage::new);
	MessageType CLAIM_CHOICE_REWARD = NET.registerC2S("claim_choice_reward", ClaimChoiceRewardMessage::new);
	MessageType DISPLAY_COMPLETION_TOAST = NET.registerS2C("display_completion_toast", DisplayCompletionToastMessage::new);
	MessageType DISPLAY_REWARD_TOAST = NET.registerS2C("display_reward_toast", DisplayRewardToastMessage::new);
	MessageType DISPLAY_ITEM_REWARD_TOAST = NET.registerS2C("display_item_reward_toast", DisplayItemRewardToastMessage::new);
	MessageType TOGGLE_PINNED = NET.registerC2S("toggle_pinned", TogglePinnedMessage::new);
	MessageType TOGGLE_PINNED_RESPONSE = NET.registerS2C("toggle_pinned_response", TogglePinnedResponseMessage::new);
	MessageType UPDATE_TEAM_DATA = NET.registerS2C("update_team_data", UpdateTeamDataMessage::new);
	MessageType SET_CUSTOM_IMAGE = NET.registerC2S("set_custom_image", SetCustomImageMessage::new);
	MessageType OBJECT_STARTED = NET.registerS2C("object_started", ObjectStartedMessage::new);
	MessageType OBJECT_COMPLETED = NET.registerS2C("object_completed", ObjectCompletedMessage::new);
	MessageType OBJECT_STARTED_RESET = NET.registerS2C("object_started_reset", ObjectStartedResetMessage::new);
	MessageType OBJECT_COMPLETED_RESET = NET.registerS2C("object_completed_reset", ObjectCompletedResetMessage::new);
	MessageType SYNC_LOCK = NET.registerS2C("sync_lock", SyncLockMessage::new);
	MessageType RESET_REWARD = NET.registerS2C("reset_reward", ResetRewardMessage::new);
	MessageType TEAM_DATA_CHANGED = NET.registerS2C("team_data_changed", TeamDataChangedMessage::new);

	MessageType CHANGE_PROGRESS = NET.registerC2S("change_progress", ChangeProgressMessage::new);
	MessageType CREATE_OBJECT = NET.registerC2S("create_object", CreateObjectMessage::new);
	MessageType CREATE_OBJECT_RESPONSE = NET.registerS2C("create_object_response", CreateObjectResponseMessage::new);
	MessageType CREATE_TASK_AT = NET.registerC2S("create_task_at", CreateTaskAtMessage::new);
	MessageType DELETE_OBJECT = NET.registerC2S("delete_object", DeleteObjectMessage::new);
	MessageType DELETE_OBJECT_RESPONSE = NET.registerS2C("delete_object_response", DeleteObjectResponseMessage::new);
	MessageType EDIT_OBJECT = NET.registerC2S("edit_object", EditObjectMessage::new);
	MessageType EDIT_OBJECT_RESPONSE = NET.registerS2C("edit_object_response", EditObjectResponseMessage::new);
	MessageType MOVE_CHAPTER = NET.registerC2S("move_chapter", MoveChapterMessage::new);
	MessageType MOVE_CHAPTER_RESPONSE = NET.registerS2C("move_chapter_response", MoveChapterResponseMessage::new);
	MessageType MOVE_QUEST = NET.registerC2S("move_quest", MoveQuestMessage::new);
	MessageType MOVE_QUEST_RESPONSE = NET.registerS2C("move_quest_response", MoveQuestResponseMessage::new);
	MessageType CHANGE_CHAPTER_GROUP = NET.registerC2S("change_chapter_group", ChangeChapterGroupMessage::new);
	MessageType CHANGE_CHAPTER_GROUP_RESPONSE = NET.registerS2C("change_chapter_group_response", ChangeChapterGroupResponseMessage::new);
	MessageType MOVE_CHAPTER_GROUP = NET.registerC2S("move_chapter_group", MoveChapterGroupMessage::new);
	MessageType MOVE_CHAPTER_GROUP_RESPONSE = NET.registerS2C("move_chapter_group_response", MoveChapterGroupResponseMessage::new);

	static void init() {
	}

	@ExpectPlatform
	static void writeItemType(FriendlyByteBuf buffer, ItemStack stack) {
		throw new AssertionError();
	}

	@ExpectPlatform
	static ItemStack readItemType(FriendlyByteBuf buffer) {
		throw new AssertionError();
	}
}