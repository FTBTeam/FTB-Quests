package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class CopyChapterImageMessage extends BaseC2SMessage {
    private final ChapterImage img;
    private final long chapterId;

    public CopyChapterImageMessage(ChapterImage toCopy, Chapter chapter, double x, double y) {
        this.img = new ChapterImage(chapter);
        CompoundTag tag = new CompoundTag();
        toCopy.writeData(tag);
        this.img.readData(tag);
        img.x = x;
        img.y = y;
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
        img.chapter.images.add(img);
        img.chapter.file.save();

        new EditObjectResponseMessage(img.chapter).sendToAll(context.getPlayer().getServer());
    }
}
