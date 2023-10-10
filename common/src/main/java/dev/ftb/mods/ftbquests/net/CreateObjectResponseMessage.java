package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class CreateObjectResponseMessage extends BaseS2CMessage {
	private final long id;
	private final long parent;
	private final QuestObjectType type;
	private final CompoundTag nbt;
	private final CompoundTag extra;
	private final UUID creator;

	public CreateObjectResponseMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		parent = buffer.readLong();
		type = QuestObjectType.NAME_MAP.read(buffer);
		nbt = buffer.readNbt();
		extra = buffer.readNbt();
		creator = buffer.readBoolean() ? buffer.readUUID() : Util.NIL_UUID;
	}

	public CreateObjectResponseMessage(QuestObjectBase o, @Nullable CompoundTag e) {
		this(o, e, Util.NIL_UUID);
	}

	public CreateObjectResponseMessage(QuestObjectBase o, @Nullable CompoundTag e, UUID creator) {
		id = o.id;
		parent = o.getParentID();
		type = o.getObjectType();
		nbt = new CompoundTag();
		o.writeData(nbt);
		extra = e;
		this.creator = creator;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.CREATE_OBJECT_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeLong(parent);
		QuestObjectType.NAME_MAP.write(buffer, type);
		buffer.writeNbt(nbt);
		buffer.writeNbt(extra);
		if (creator.equals(Util.NIL_UUID)) {
			buffer.writeBoolean(false);
		} else {
			buffer.writeBoolean(true);
			buffer.writeUUID(creator);
		}
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuestsNetClient.createObject(id, parent, type, nbt, extra, creator);
	}
}