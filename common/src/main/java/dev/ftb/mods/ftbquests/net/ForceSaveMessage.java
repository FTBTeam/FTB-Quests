package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class ForceSaveMessage implements CustomPacketPayload {
	public static final Type<ForceSaveMessage> TYPE = new Type<>(FTBQuestsAPI.id("force_save_message"));

	public static final ForceSaveMessage INSTANCE = new ForceSaveMessage();

	public static final StreamCodec<FriendlyByteBuf, ForceSaveMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<ForceSaveMessage> type() {
		return TYPE;
	}

	public static void handle(ForceSaveMessage ignoredMessage, PacketContext context) {
        if (context.player() instanceof ServerPlayer player && PermissionsHelper.hasEditorPermission(player)) {
            ServerQuestFile.ifExists(sqf -> {
                sqf.markDirty();
                sqf.saveNow();
                player.sendSystemMessage(Component.translatable("ftbquests.gui.saved_on_server"));
            });
        }
	}
}
