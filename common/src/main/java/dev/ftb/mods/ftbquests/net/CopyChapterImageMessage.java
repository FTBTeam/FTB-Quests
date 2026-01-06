package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CopyChapterImageMessage(ChapterImage img) implements CustomPacketPayload {
    public static final Type<CopyChapterImageMessage> TYPE = new Type<>(FTBQuestsAPI.id("copy_chapter_image_message"));

    public static final StreamCodec<FriendlyByteBuf, CopyChapterImageMessage> STREAM_CODEC = StreamCodec.composite(
            ChapterImage.STREAM_CODEC, CopyChapterImageMessage::img,
            CopyChapterImageMessage::new
    );

    public CopyChapterImageMessage(ChapterImage toCopy, Chapter chapter, double newX, double newY) {
        this(toCopy.copy(chapter, newX, newY));
    }

    @Override
    public Type<CopyChapterImageMessage> type() {
        return TYPE;
    }

    public static void handle(CopyChapterImageMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            Chapter chapter = message.img.getChapter();
            chapter.addImage(message.img);
            chapter.file.markDirty();
            NetworkHelper.sendToAll(context.getPlayer().level().getServer(), new EditObjectResponseMessage(chapter));
        });
    }

}
