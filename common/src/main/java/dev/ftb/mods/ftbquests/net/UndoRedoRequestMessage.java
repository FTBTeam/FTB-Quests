package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent by client to request an undo or redo of the last operation.
 *
 * @param isUndo true to request an undo, false to request a redo
 */
public record UndoRedoRequestMessage(boolean isUndo) implements CustomPacketPayload {
    public static final Type<UndoRedoRequestMessage> TYPE = new Type<>(FTBQuestsAPI.id("undo_request"));

    public static final StreamCodec<FriendlyByteBuf, UndoRedoRequestMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, UndoRedoRequestMessage::isUndo,
            UndoRedoRequestMessage::new
    );

    public static UndoRedoRequestMessage undo() {
        return new UndoRedoRequestMessage(true);
    }

    public static UndoRedoRequestMessage redo() {
        return new UndoRedoRequestMessage(false);
    }

    @Override
    public Type<UndoRedoRequestMessage> type() {
        return TYPE;
    }

    public static void handle(UndoRedoRequestMessage message, PacketContext packetContext) {
        if (PermissionsHelper.canPlayerEdit(packetContext)) {
            ServerQuestFile.ifExists(sqf -> {
                if (message.isUndo) {
                    sqf.getHistoryStack().tryUndo(sqf);
                } else {
                    sqf.getHistoryStack().tryRedo(sqf);
                }
            });
        }
    }
}
