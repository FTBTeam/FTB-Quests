package dev.ftb.mods.ftbquests.quest.history;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EditRecord(long id, CompoundTag nbt) {
    public static final StreamCodec<FriendlyByteBuf, EditRecord> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, EditRecord::id,
            ByteBufCodecs.COMPOUND_TAG, EditRecord::nbt,
            EditRecord::new
    );

    public static EditRecord ofQuestObject(QuestObjectBase qo) {
        return new EditRecord(qo.id, Util.make(new CompoundTag(), nbt1 -> qo.writeData(nbt1, qo.getQuestFile().holderLookup())));
    }
}
