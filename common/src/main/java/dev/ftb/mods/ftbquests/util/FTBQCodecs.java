package dev.ftb.mods.ftbquests.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5NetPacker;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FTBQCodecs {
    public static final Codec<Map<QuestKey,Long>> CLAIMED_REWARDS_CODEC
            = Codec.unboundedMap(QuestKey.STRING_CODEC, Codec.LONG).xmap(Object2LongOpenHashMap::new, Function.identity());
    public static final StreamCodec<FriendlyByteBuf,Map<QuestKey,Long>> CLAIMED_REWARDS_STREAM_CODEC
            = ByteBufCodecs.map(Object2LongOpenHashMap::new, QuestKey.STREAM_CODEC, ByteBufCodecs.LONG);

    public static final Codec<Map<Long,Long>> LONG_LONG_MAP_CODEC
            = Codec.unboundedMap(Codec.LONG, Codec.LONG).xmap(Long2LongOpenHashMap::new, Function.identity());
    public static final StreamCodec<FriendlyByteBuf, Map<Long,Long>> LONG_LONG_MAP_STREAM_CODEC
            = ByteBufCodecs.map(Long2LongOpenHashMap::new, ByteBufCodecs.LONG, ByteBufCodecs.LONG);

    public static final Codec<Map<Long,Integer>> LONG_INT_MAP_CODEC
            = Codec.unboundedMap(Codec.LONG, Codec.INT).xmap(Long2IntOpenHashMap::new, Function.identity());

    public static final Codec<LongSet> LONG_SET_CODEC
            = Codec.LONG.listOf().xmap(LongOpenHashSet::new, LongArrayList::new);

    public static final StreamCodec<FriendlyByteBuf, LongSet> LONG_SET_STREAM_CODEC = StreamCodec.of(
            (output, value) -> output.writeCollection(value, FriendlyByteBuf::writeLong),
            input -> input.readCollection(LongOpenHashSet::new, FriendlyByteBuf::readLong)
    );

    public static final StreamCodec<ByteBuf, List<Long>> LONG_LIST_STREAM_CODEC
            = ByteBufCodecs.LONG.apply(ByteBufCodecs.list());

    public static final Codec<Long> QUEST_ID_CODEC = Codec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(QuestObjectBase.parseCodeString(s));
        } catch (NumberFormatException e) {
            return DataResult.error(e::getMessage);
        }
    }, QuestObjectBase::getCodeString);

    // TODO move to Json5NetPacker?
    public static final StreamCodec<ByteBuf, Json5Object> JSON5_OBJECT_STREAM_CODEC = StreamCodec.of(
            Json5NetPacker::pack,
            Json5NetPacker::unpack
    ).map(Json5Element::getAsJson5Object, Function.identity());

    public static <T, U, B extends ByteBuf> StreamCodec<B, Pair<T, U>> pair(StreamCodec<? super B, T> first, StreamCodec<? super B, U> second) {
        return StreamCodec.of(
                (output, value) -> {
                    first.encode(output, value.getFirst());
                    second.encode(output, value.getSecond());
                },
                input -> Pair.of(first.decode(input), second.decode(input))
        );
    }
}
