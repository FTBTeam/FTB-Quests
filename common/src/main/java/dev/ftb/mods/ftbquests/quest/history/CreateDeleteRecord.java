package dev.ftb.mods.ftbquests.quest.history;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record CreateDeleteRecord(long id, long parent, QuestObjectType questObjectType, CompoundTag nbt, CompoundTag extra) {
    public static final StreamCodec<FriendlyByteBuf, CreateDeleteRecord> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, CreateDeleteRecord::id,
            ByteBufCodecs.VAR_LONG, CreateDeleteRecord::parent,
            QuestObjectType.STREAM_CODEC, CreateDeleteRecord::questObjectType,
            ByteBufCodecs.COMPOUND_TAG, CreateDeleteRecord::nbt,
            ByteBufCodecs.COMPOUND_TAG, CreateDeleteRecord::extra,
            CreateDeleteRecord::new
    );

    public static CreateDeleteRecord ofQuestObject(QuestObjectBase qo) {
        return new CreateDeleteRecord(
                qo.getId(),
                qo.getParentID(),
                qo.getObjectType(),
                Util.make(new CompoundTag(), nbt1 -> qo.writeData(nbt1, qo.getQuestFile().holderLookup())),
                qo.makeExtraCreationData()
        );
    }

    public static List<CreateDeleteRecord> fromIds(ServerQuestFile sqf, List<Long> ids) {
        return ids.stream()
                .map(sqf::getBase)
                .filter(Objects::nonNull)
                .map(CreateDeleteRecord::ofQuestObject)
                .toList();
    }

    public static List<CreateDeleteRecord> ofQuestObjects(QuestObjectBase... qo) {
        return Arrays.stream(qo).map(CreateDeleteRecord::ofQuestObject).toList();
    }

    public CreateDeleteRecord withNewID(ServerQuestFile file) {
        return new CreateDeleteRecord(file.newID(), parent, questObjectType, nbt, extra);
    }
}
