package dev.ftb.mods.ftbquests.util;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;
import java.util.function.Function;

public class FTBQCodecs {
    public static final Codec<Map<QuestKey,Long>> CLAIMED_REWARDS_CODEC
            = Codec.unboundedMap(QuestKey.CODEC, Codec.LONG).xmap(Object2LongOpenHashMap::new, Function.identity());
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
}
