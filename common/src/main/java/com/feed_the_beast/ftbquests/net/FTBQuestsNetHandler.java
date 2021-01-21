package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkChannel;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Function;

public class FTBQuestsNetHandler
{
	public static NetworkChannel MAIN;
	private static final String GENERAL_VERSION = "1";

	private static <T extends MessageBase> void register(NetworkManager.Side side, Class<T> c, Function<FriendlyByteBuf, T> s)
	{
		MAIN.register(side, c, MessageBase::write, s, MessageBase::handle);
	}

	public static void init()
	{
	    MAIN = NetworkChannel.create(new ResourceLocation(FTBQuests.MOD_ID, "main"));

		id = 0;

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

		id = 100;

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

	public static void writeItemType(FriendlyByteBuf buffer, ItemStack stack)
	{
	    buffer.writeItem(stack);
		if (stack.isEmpty())
		{
			buffer.writeVarInt(-1);
		}
		else
		{
			buffer.writeVarInt(Item.getId(stack.getItem()));
			buffer.writeNbt(stack.getTag());
			buffer.writeNbt((CompoundTag) stack.save(new CompoundTag()).get("ForgeCaps"));
		}
	}

	public static ItemStack readItemType(FriendlyByteBuf buffer)
	{
		int id = buffer.readVarInt();

		if (id == -1)
		{
			return ItemStack.EMPTY;
		}
		else
		{
			CompoundTag tag = buffer.readNbt();
			CompoundTag caps = buffer.readNbt();
			ItemStack item = new ItemStack(Item.byId(id), 1, caps);
			item.setTag(tag);
			return item;
		}
	}
}