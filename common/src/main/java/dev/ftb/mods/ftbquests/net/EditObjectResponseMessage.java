package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class EditObjectResponseMessage extends BaseS2CMessage {
	private final long id;
	private final CompoundTag nbt;

	EditObjectResponseMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		nbt = buffer.readNbt();
	}

	public EditObjectResponseMessage(QuestObjectBase o) {
		id = o.id;
		nbt = new CompoundTag();
		o.writeData(nbt);
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.EDIT_OBJECT_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeNbt(nbt);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.editObject(id, nbt);
	}
}