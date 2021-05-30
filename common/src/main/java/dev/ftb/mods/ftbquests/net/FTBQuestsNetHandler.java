package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftblibrary.net.snm.SimpleNetworkManager;
import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public interface FTBQuestsNetHandler {
	SimpleNetworkManager NET = SimpleNetworkManager.create(FTBQuests.MOD_ID);

	PacketID SYNC_QUESTS = NET.registerS2C("sync_quests", SyncQuestsPacket::new);
	PacketID SYNC_TEAM_DATA = NET.registerS2C("sync_team_data", SyncTeamDataPacket::new);
	PacketID UPDATE_TASK_PROGRESS = NET.registerS2C("update_task_progress", UpdateTaskProgressPacket::new);
	PacketID SUBMIT_TASK = NET.registerC2S("submit_task", SubmitTaskPacket::new);
	PacketID CLAIM_REWARD = NET.registerC2S("claim_reward", ClaimRewardPacket::new);
	PacketID CLAIM_REWARD_RESPONSE = NET.registerS2C("claim_reward_response", ClaimRewardResponsePacket::new);
	PacketID SYNC_EDITING_MODE = NET.registerS2C("sync_editing_mode", SyncEditingModePacket::new);
	PacketID GET_EMERGENCY_ITEMS = NET.registerC2S("get_emergency_items", GetEmergencyItemsPacket::new);
	PacketID CREATE_OTHER_TEAM_DATA = NET.registerS2C("create_other_team_data", CreateOtherTeamDataPacket::new);
	PacketID CLAIM_ALL_REWARDS = NET.registerC2S("claim_all_rewards", ClaimAllRewardsPacket::new);
	PacketID CLAIM_CHOICE_REWARD = NET.registerC2S("claim_choice_reward", ClaimChoiceRewardPacket::new);
	PacketID DISPLAY_COMPLETION_TOAST = NET.registerS2C("display_completion_toast", DisplayCompletionToastPacket::new);
	PacketID DISPLAY_REWARD_TOAST = NET.registerS2C("display_reward_toast", DisplayRewardToastPacket::new);
	PacketID DISPLAY_ITEM_REWARD_TOAST = NET.registerS2C("display_item_reward_toast", DisplayItemRewardToastPacket::new);
	PacketID TOGGLE_PINNED = NET.registerC2S("toggle_pinned", TogglePinnedPacket::new);
	PacketID TOGGLE_PINNED_RESPONSE = NET.registerS2C("toggle_pinned_response", TogglePinnedResponsePacket::new);
	PacketID UPDATE_TEAM_DATA = NET.registerS2C("update_team_data", UpdateTeamDataPacket::new);
	PacketID SET_CUSTOM_IMAGE = NET.registerC2S("set_custom_image", SetCustomImagePacket::new);
	PacketID OBJECT_STARTED = NET.registerS2C("object_started", ObjectStartedPacket::new);
	PacketID OBJECT_COMPLETED = NET.registerS2C("object_completed", ObjectCompletedPacket::new);
	PacketID OBJECT_STARTED_RESET = NET.registerS2C("object_started_reset", ObjectStartedResetPacket::new);
	PacketID OBJECT_COMPLETED_RESET = NET.registerS2C("object_completed_reset", ObjectCompletedResetPacket::new);
	PacketID SYNC_LOCK = NET.registerS2C("sync_lock", SyncLockPacket::new);
	PacketID RESET_REWARD = NET.registerS2C("reset_reward", ResetRewardPacket::new);
	PacketID TEAM_DATA_CHANGED = NET.registerS2C("team_data_changed", TeamDataChangedPacket::new);

	PacketID CHANGE_PROGRESS = NET.registerC2S("change_progress", ChangeProgressPacket::new);
	PacketID CREATE_OBJECT = NET.registerC2S("create_object", CreateObjectPacket::new);
	PacketID CREATE_OBJECT_RESPONSE = NET.registerS2C("create_object_response", CreateObjectResponsePacket::new);
	PacketID CREATE_TASK_AT = NET.registerC2S("create_task_at", CreateTaskAtPacket::new);
	PacketID DELETE_OBJECT = NET.registerC2S("delete_object", DeleteObjectPacket::new);
	PacketID DELETE_OBJECT_RESPONSE = NET.registerS2C("delete_object_response", DeleteObjectResponsePacket::new);
	PacketID EDIT_OBJECT = NET.registerC2S("edit_object", EditObjectPacket::new);
	PacketID EDIT_OBJECT_RESPONSE = NET.registerS2C("edit_object_response", EditObjectResponsePacket::new);
	PacketID MOVE_CHAPTER = NET.registerC2S("move_chapter", MoveChapterPacket::new);
	PacketID MOVE_CHAPTER_RESPONSE = NET.registerS2C("move_chapter_response", MoveChapterResponsePacket::new);
	PacketID MOVE_QUEST = NET.registerC2S("move_quest", MoveQuestPacket::new);
	PacketID MOVE_QUEST_RESPONSE = NET.registerS2C("move_quest_response", MoveQuestResponsePacket::new);
	PacketID CHANGE_CHAPTER_GROUP = NET.registerC2S("change_chapter_group", ChangeChapterGroupPacket::new);
	PacketID CHANGE_CHAPTER_GROUP_RESPONSE = NET.registerS2C("change_chapter_group_response", ChangeChapterGroupResponsePacket::new);
	PacketID MOVE_CHAPTER_GROUP = NET.registerC2S("move_chapter_group", MoveChapterGroupPacket::new);
	PacketID MOVE_CHAPTER_GROUP_RESPONSE = NET.registerS2C("move_chapter_group_response", MoveChapterGroupResponsePacket::new);

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