package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public class CreateObjectResponsePacket extends BaseS2CPacket {
	private final long id;
	private final long parent;
	private final QuestObjectType type;
	private final CompoundTag nbt;
	private final CompoundTag extra;

	public CreateObjectResponsePacket(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		parent = buffer.readLong();
		type = QuestObjectType.NAME_MAP.read(buffer);
		nbt = buffer.readNbt();
		extra = buffer.readNbt();
	}

	public CreateObjectResponsePacket(QuestObjectBase o, @Nullable CompoundTag e) {
		id = o.id;
		parent = o.getParentID();
		type = o.getObjectType();
		nbt = new CompoundTag();
		o.writeData(nbt);
		extra = e;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.CREATE_OBJECT_RESPONSE;
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