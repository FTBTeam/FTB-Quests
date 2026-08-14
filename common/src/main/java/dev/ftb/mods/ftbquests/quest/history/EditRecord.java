package dev.ftb.mods.ftbquests.quest.history;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public record EditRecord(long id, QuestObjectType questObjectType, Component title, CompoundTag nbt) {
    public static final StreamCodec<RegistryFriendlyByteBuf, EditRecord> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, EditRecord::id,
            NetworkHelper.enumStreamCodec(QuestObjectType.class), EditRecord::questObjectType,
            ComponentSerialization.STREAM_CODEC, EditRecord::title,
            ByteBufCodecs.COMPOUND_TAG, EditRecord::nbt,
            EditRecord::new
    );

    public static EditRecord ofQuestObject(QuestObjectBase qo) {
        return new EditRecord(qo.id, qo.getObjectType(), qo.getTitle(), Util.make(new CompoundTag(), nbt1 -> qo.writeData(nbt1, qo.getQuestFile().holderLookup())));
    }

    @Override
    public boolean equals(Object o) {
        // we do not consider the title part of the equality check
        // title here is for display purposes only; the actual quest object title is handled by UpdateTranslation
        if (o == null || getClass() != o.getClass()) return false;
        EditRecord that = (EditRecord) o;
        return id == that.id && Objects.equals(nbt, that.nbt) && questObjectType == that.questObjectType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, questObjectType, nbt);
    }

    public EditRecord sanitizeTitle() {
        return new EditRecord(id, questObjectType, QuestBookEditEvent.sanitizeComponent(title), nbt);
    }
}
