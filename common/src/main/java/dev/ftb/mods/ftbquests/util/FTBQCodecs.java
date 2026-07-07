package dev.ftb.mods.ftbquests.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
    public static final StreamCodec<ByteBuf, List<Long>> LONG_LIST_STREAM_CODEC
            = ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list());

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
