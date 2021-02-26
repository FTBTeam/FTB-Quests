package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageCreateObject extends MessageBase {
	private final long parent;
	private final QuestObjectType type;
	private final CompoundTag nbt;
	private final CompoundTag extra;

	MessageCreateObject(FriendlyByteBuf buffer) {
		parent = buffer.readLong();
		type = QuestObjectType.NAME_MAP.read(buffer);
		nbt = buffer.readNbt();
		extra = buffer.readNbt();
	}

	public MessageCreateObject(QuestObjectBase o, @Nullable CompoundTag e) {
		parent = o.getParentID();
		type = o.getObjectType();
		nbt = new CompoundTag();
		o.writeData(nbt);
		extra = e;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(parent);
		QuestObjectType.NAME_MAP.write(buffer, type);
		buffer.writeNbt(nbt);
		buffer.writeNbt(extra);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context)) {
			QuestObjectBase object = ServerQuestFile.INSTANCE.create(type, parent, extra == null ? new CompoundTag() : extra);
			object.readData(nbt);
			object.id = ServerQuestFile.INSTANCE.newID();
			object.onCreated();
			object.getQuestFile().refreshIDMap();
			object.getQuestFile().clearCachedData();
			object.getQuestFile().save();

			new MessageCreateObjectResponse(object, extra).sendToAll();
		}
	}
}