package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class EditObjectResponsePacket extends BaseS2CPacket {
	private final long id;
	private final CompoundTag nbt;

	EditObjectResponsePacket(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		nbt = buffer.readNbt();
	}

	public EditObjectResponsePacket(QuestObjectBase o) {
		id = o.id;
		nbt = new CompoundTag();
		o.writeData(nbt);
	}

	@Override
	public PacketID getId() {
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