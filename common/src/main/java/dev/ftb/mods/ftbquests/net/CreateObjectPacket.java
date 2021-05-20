package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseC2SPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class CreateObjectPacket extends BaseC2SPacket {
	private final long parent;
	private final QuestObjectType type;
	private final CompoundTag nbt;
	private final CompoundTag extra;

	CreateObjectPacket(FriendlyByteBuf buffer) {
		parent = buffer.readLong();
		type = QuestObjectType.NAME_MAP.read(buffer);
		nbt = buffer.readNbt();
		extra = buffer.readNbt();
	}

	public CreateObjectPacket(QuestObjectBase o, @Nullable CompoundTag e) {
		parent = o.getParentID();
		type = o.getObjectType();
		nbt = new CompoundTag();
		o.writeData(nbt);
		extra = e;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.CREATE_OBJECT;
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

			new CreateObjectResponsePacket(object, extra).sendToAll(context.getPlayer().getServer());
		}
	}
}