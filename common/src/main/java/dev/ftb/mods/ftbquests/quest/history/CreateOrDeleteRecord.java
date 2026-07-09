package dev.ftb.mods.ftbquests.quest.history;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record CreateOrDeleteRecord(long id, long parent, QuestObjectType questObjectType, Component title, CompoundTag nbt, CompoundTag extra) {
    public static final StreamCodec<RegistryFriendlyByteBuf, CreateOrDeleteRecord> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, CreateOrDeleteRecord::id,
            ByteBufCodecs.VAR_LONG, CreateOrDeleteRecord::parent,
            QuestObjectType.STREAM_CODEC, CreateOrDeleteRecord::questObjectType,
            ComponentSerialization.STREAM_CODEC, CreateOrDeleteRecord::title,
            ByteBufCodecs.COMPOUND_TAG, CreateOrDeleteRecord::nbt,
            ByteBufCodecs.COMPOUND_TAG, CreateOrDeleteRecord::extra,
            CreateOrDeleteRecord::new
    );

    public static CreateOrDeleteRecord ofQuestObject(QuestObjectBase qo) {
        return new CreateOrDeleteRecord(
                qo.getId(),
                qo.getParentID(),
                qo.getObjectType(),
                qo.getTitle(),
                Util.make(new CompoundTag(), nbt1 -> qo.writeData(nbt1, qo.getQuestFile().holderLookup())),
                qo.makeExtraCreationData()
        );
    }

    public static List<CreateOrDeleteRecord> fromIds(ServerQuestFile sqf, List<Long> ids) {
        return ids.stream()
                .map(sqf::getBase)
                .filter(Objects::nonNull)
                .map(CreateOrDeleteRecord::ofQuestObject)
                .toList();
    }

    public static List<CreateOrDeleteRecord> ofQuestObjects(QuestObjectBase... qo) {
        return Arrays.stream(qo).map(CreateOrDeleteRecord::ofQuestObject).toList();
    }

    public CreateOrDeleteRecord withNewID(ServerQuestFile file) {
        return new CreateOrDeleteRecord(file.newID(), parent, questObjectType, title, nbt, extra);
    }
}
