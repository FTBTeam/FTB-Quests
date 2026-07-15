package dev.ftb.mods.ftbquests.quest.preset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record VisualPresetRecord(String shapeName, double size) implements VisualPreset {
    public static final Codec<VisualPresetRecord> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        Codec.STRING.fieldOf("shape").forGetter(VisualPresetRecord::shapeName),
        Codec.DOUBLE.fieldOf("size").forGetter(VisualPresetRecord::size)
    ).apply(builder, VisualPresetRecord::new));

    public static final StreamCodec<FriendlyByteBuf, VisualPresetRecord> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, VisualPresetRecord::shapeName,
            ByteBufCodecs.DOUBLE, VisualPresetRecord::size,
            VisualPresetRecord::new
    );
}
