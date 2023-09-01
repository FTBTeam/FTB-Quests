package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;

public class CopyChapterImageMessage extends BaseC2SMessage {
    private final ChapterImage img;
    private final long chapterId;

    public CopyChapterImageMessage(ChapterImage toCopy, Chapter chapter, double newX, double newY) {
        img = toCopy.copy(chapter, newX, newY);
        this.chapterId = chapter.id;
    }

    public CopyChapterImageMessage(FriendlyByteBuf buf) {
        chapterId = buf.readLong();
        img = new ChapterImage(ServerQuestFile.INSTANCE.getChapter(chapterId));
        img.readNetData(buf);
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.COPY_CHAPTER_IMAGE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(chapterId);
        img.writeNetData(buf);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        Chapter chapter = img.getChapter();
        chapter.addImage(img);
        chapter.file.markDirty();
        new EditObjectResponseMessage(chapter).sendToAll(context.getPlayer().getServer());
    }
}
