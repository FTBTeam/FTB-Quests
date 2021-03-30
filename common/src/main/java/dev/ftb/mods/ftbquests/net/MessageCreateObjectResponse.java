package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageCreateObjectResponse extends MessageBase {
	private final long id;
	private final long parent;
	private final QuestObjectType type;
	private final CompoundTag nbt;
	private final CompoundTag extra;

	public MessageCreateObjectResponse(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		parent = buffer.readLong();
		type = QuestObjectType.NAME_MAP.read(buffer);
		nbt = buffer.readNbt();
		extra = buffer.readNbt();
	}

	public MessageCreateObjectResponse(QuestObjectBase o, @Nullable CompoundTag e) {
		id = o.id;
		parent = o.getParentID();
		type = o.getObjectType();
		nbt = new CompoundTag();
		o.writeData(nbt);
		extra = e;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeLong(parent);
		QuestObjectType.NAME_MAP.write(buffer, type);
		buffer.writeNbt(nbt);
		buffer.writeNbt(extra);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.createObject(id, parent, type, nbt, extra);
	}
}