package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class CreateObjectMessage extends BaseC2SMessage {
	private final long parent;
	private final QuestObjectType type;
	private final boolean openScreen;
	private final CompoundTag nbt;
	private final CompoundTag extra;

	CreateObjectMessage(FriendlyByteBuf buffer) {
		parent = buffer.readLong();
		type = QuestObjectType.NAME_MAP.read(buffer);
		openScreen = buffer.readBoolean();
		nbt = buffer.readNbt();
		extra = buffer.readNbt();
	}

	public CreateObjectMessage(QuestObjectBase o, @Nullable CompoundTag e, boolean openScreen) {
		parent = o.getParentID();
		type = o.getObjectType();
		this.openScreen = openScreen;
		nbt = new CompoundTag();
		o.writeData(nbt);
		extra = e;
	}

	public CreateObjectMessage(QuestObjectBase o, @Nullable CompoundTag e) {
		this(o, e, true);
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.CREATE_OBJECT;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(parent);
		QuestObjectType.NAME_MAP.write(buffer, type);
		buffer.writeBoolean(openScreen);
		buffer.writeNbt(nbt);
		buffer.writeNbt(extra);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context) && context.getPlayer() instanceof ServerPlayer sp) {
			QuestObjectBase object = ServerQuestFile.INSTANCE.create(ServerQuestFile.INSTANCE.newID(), type, parent, extra == null ? new CompoundTag() : extra);
			object.readData(nbt);
			object.onCreated();
			object.getQuestFile().refreshIDMap();
			object.getQuestFile().clearCachedData();
			object.getQuestFile().markDirty();

			object.getQuestFile().getTranslationManager().processInitialTranslation(extra, object);

			new CreateObjectResponseMessage(object, extra, openScreen ? sp.getUUID() : Util.NIL_UUID).sendToAll(sp.getServer());
		}
	}
}