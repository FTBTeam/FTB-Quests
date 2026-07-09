package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.history.ChangeType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/**
 * Received on: CLIENT
 * <p>
 * Sent by server when a change has been made; includes descriptive info about what just happened so the client
 * can show a change log and update tooltips for the Undo & Redo buttons.
 *
 * @param changeType is this a new operation, an undo, or a redo?
 * @param desc description of the message, to add to change log
 * @param undoDesc description of the change to be undone, to add to button tooltip
 * @param redoDesc description of the change to be redone, to add to button tooltip
 */
public record SendChangeDescPacket(ChangeType changeType, List<Component> desc, List<Component> undoDesc, List<Component> redoDesc) implements CustomPacketPayload {
    public static final Type<SendChangeDescPacket> TYPE = new Type<>(FTBQuestsAPI.rl("send_change_desc"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SendChangeDescPacket> STREAM_CODEC = StreamCodec.composite(
            NetworkHelper.enumStreamCodec(ChangeType.class), SendChangeDescPacket::changeType,
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()), SendChangeDescPacket::desc,
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()), SendChangeDescPacket::undoDesc,
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()), SendChangeDescPacket::redoDesc,
            SendChangeDescPacket::new
    );

    @Override
    public Type<SendChangeDescPacket> type() {
        return TYPE;
    }

    public static void handle(SendChangeDescPacket message, NetworkManager.PacketContext context) {
        if (ClientQuestFile.exists()) {
            ClientQuestFile.INSTANCE.getChangelog().addEntry(message);
        }
    }

}
