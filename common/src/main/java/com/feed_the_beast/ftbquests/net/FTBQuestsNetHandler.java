package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import me.shedaniel.architectury.annotations.ExpectPlatform;
import me.shedaniel.architectury.networking.NetworkChannel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class FTBQuestsNetHandler {
	public static NetworkChannel MAIN;

	private static <T extends MessageBase> void register(Class<T> c, Function<FriendlyByteBuf, T> s) {
		MAIN.register(c, MessageBase::write, s, MessageBase::handle);
	}

	public static void init() {
		MAIN = NetworkChannel.create(new ResourceLocation(FTBQuests.MOD_ID, "main"));

		// Game
		register(MessageSyncQuests.class, MessageSyncQuests::new);
		register(MessageUpdateTaskProgress.class, MessageUpdateTaskProgress::new);
		register(MessageSubmitTask.class, MessageSubmitTask::new);
		register(MessageClaimReward.class, MessageClaimReward::new);
		register(MessageClaimRewardResponse.class, MessageClaimRewardResponse::new);
		register(MessageSyncEditingMode.class, MessageSyncEditingMode::new);
		register(MessageGetEmergencyItems.class, MessageGetEmergencyItems::new);
		register(MessageCreatePlayerData.class, MessageCreatePlayerData::new);
		register(MessageClaimAllRewards.class, MessageClaimAllRewards::new);
		register(MessageClaimChoiceReward.class, MessageClaimChoiceReward::new);
		register(MessageDisplayCompletionToast.class, MessageDisplayCompletionToast::new);
		register(MessageDisplayRewardToast.class, MessageDisplayRewardToast::new);
		register(MessageDisplayItemRewardToast.class, MessageDisplayItemRewardToast::new);
		register(MessageTogglePinned.class, MessageTogglePinned::new);
		register(MessageTogglePinnedResponse.class, MessageTogglePinnedResponse::new);
		register(MessageUpdatePlayerData.class, MessageUpdatePlayerData::new);
		register(MessageSetCustomImage.class, MessageSetCustomImage::new);

		// Editing
		register(MessageChangeProgress.class, MessageChangeProgress::new);
		register(MessageChangeProgressResponse.class, MessageChangeProgressResponse::new);
		register(MessageCreateObject.class, MessageCreateObject::new);
		register(MessageCreateObjectResponse.class, MessageCreateObjectResponse::new);
		register(MessageCreateTaskAt.class, MessageCreateTaskAt::new);
		register(MessageDeleteObject.class, MessageDeleteObject::new);
		register(MessageDeleteObjectResponse.class, MessageDeleteObjectResponse::new);
		register(MessageEditObject.class, MessageEditObject::new);
		register(MessageEditObjectResponse.class, MessageEditObjectResponse::new);
		register(MessageMoveChapter.class, MessageMoveChapter::new);
		register(MessageMoveChapterResponse.class, MessageMoveChapterResponse::new);
		register(MessageMoveQuest.class, MessageMoveQuest::new);
		register(MessageMoveQuestResponse.class, MessageMoveQuestResponse::new);
	}

	@ExpectPlatform
	public static void writeItemType(FriendlyByteBuf buffer, ItemStack stack) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static ItemStack readItemType(FriendlyByteBuf buffer) {
		throw new AssertionError();
	}
}