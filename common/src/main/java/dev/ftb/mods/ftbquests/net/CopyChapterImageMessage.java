package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.CreateOrDeleteRecord;
import dev.ftb.mods.ftbquests.quest.history.events.CreateQuestObjects;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

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

    public static void handle(CopyChapterImageMessage message, PacketContext context) {
        if (NetUtils.canEdit(context)) {
            ServerQuestFile sqf = ServerQuestFile.getInstance();
            if (sqf.getBase(message.id) instanceof ChapterImage img && sqf.get(message.chapterId) instanceof Chapter chapter) {
                ChapterImage newImage = Objects.requireNonNull(QuestObjectBase.copy(img, () -> new ChapterImage(sqf.newID(), chapter)));
                newImage.setPosition(message.qx, message.qy);
                sqf.getHistoryStack().addAndApply(sqf, new CreateQuestObjects(CreateOrDeleteRecord.ofQuestObject(newImage), null));
            }
        }
    }
}
