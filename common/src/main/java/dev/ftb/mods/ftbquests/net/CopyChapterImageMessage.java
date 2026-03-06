package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

import java.util.Objects;

public record CopyChapterImageMessage(long id, long chapterId, double qx, double qy) implements CustomPacketPayload {
    public static final Type<CopyChapterImageMessage> TYPE = new Type<>(FTBQuestsAPI.id("copy_chapter_image_message"));

    public static final StreamCodec<FriendlyByteBuf, CopyChapterImageMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, CopyChapterImageMessage::id,
            ByteBufCodecs.VAR_LONG, CopyChapterImageMessage::chapterId,
            ByteBufCodecs.DOUBLE, CopyChapterImageMessage::qx,
            ByteBufCodecs.DOUBLE, CopyChapterImageMessage::qy,
            CopyChapterImageMessage::new
    );

    @Override
    public Type<CopyChapterImageMessage> type() {
        return TYPE;
    }

    public static void handle(CopyChapterImageMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            BaseQuestFile file = ServerQuestFile.getInstance();
            if (file.getBase(message.id) instanceof ChapterImage img && file.get(message.chapterId) instanceof Chapter chapter) {
                ChapterImage newImage = Objects.requireNonNull(QuestObjectBase.copy(img, () -> new ChapterImage(file.newID(), chapter)));
                newImage.setPosition(message.qx, message.qy);
                newImage.onCreated();
                MinecraftServer server = Objects.requireNonNull(context.getPlayer().level().getServer());
                NetworkHelper.sendToAll(server, CreateObjectResponseMessage.create(newImage, null));

                ServerQuestFile.getInstance().refreshIDMap();
                ServerQuestFile.getInstance().clearCachedData();
                ServerQuestFile.getInstance().markDirty();
            }
        });
    }
}
