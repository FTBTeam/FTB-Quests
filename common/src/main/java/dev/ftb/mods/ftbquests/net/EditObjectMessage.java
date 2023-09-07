package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class EditObjectMessage extends BaseC2SMessage {
	private final long id;
	private final CompoundTag nbt;

	EditObjectMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		nbt = buffer.readNbt();
	}

	public EditObjectMessage(QuestObjectBase o) {
		id = o.id;
		nbt = new CompoundTag();
		o.writeData(nbt);
		FTBQuests.getRecipeModHelper().refreshRecipes(o);
		ClientQuestFile.INSTANCE.clearCachedData();
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.EDIT_OBJECT;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeNbt(nbt);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context)) {
			QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(id);

			if (object != null) {
				object.readData(nbt);
				object.editedFromGUIOnServer();
				ServerQuestFile.INSTANCE.clearCachedData();
				ServerQuestFile.INSTANCE.markDirty();
				new EditObjectResponseMessage(object).sendToAll(context.getPlayer().getServer());
			}
		}
	}
}