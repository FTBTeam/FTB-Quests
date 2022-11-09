package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author desht
 */
public class ForceSaveMessage extends BaseC2SMessage {
	public ForceSaveMessage() {
	}

	ForceSaveMessage(FriendlyByteBuf buffer) {
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.FORCE_SAVE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		ServerPlayer player = (ServerPlayer) context.getPlayer();
		if (player.hasPermissions(2)) {
			ServerQuestFile.INSTANCE.save();
			ServerQuestFile.INSTANCE.saveNow();
			player.displayClientMessage(Component.translatable("ftbquests.gui.saved_on_server"), false);
		}
	}
}
