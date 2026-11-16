package dev.ftb.mods.ftbquests.util;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FTBQCodecs {
    public static final StreamCodec<ByteBuf, List<Long>> LONG_LIST_STREAM_CODEC
            = ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list());

    public static final StreamCodec<ByteBuf, Map<Long, Boolean>> LONG_BOOL_MAP_STREAM_CODEC
            = ByteBufCodecs.map(HashMap::new, ByteBufCodecs.VAR_LONG, ByteBufCodecs.BOOL);

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
