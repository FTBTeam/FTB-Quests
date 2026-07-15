package dev.ftb.mods.ftbquests.quest.preset;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.util.Lazy;
import dev.ftb.mods.ftbquests.FTBQuests;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

public class VisualPresets {
    public static final VisualPresets EMPTY = new VisualPresets(Map.of());

    private static final Codec<Map<String, VisualPresetRecord>> CODEC = Codec.unboundedMap(Codec.STRING, VisualPresetRecord.CODEC);

    public static final StreamCodec<FriendlyByteBuf, VisualPresets> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, VisualPresetRecord.STREAM_CODEC), p -> p.presetMap,
            VisualPresets::new
    );

    private final Map<String, VisualPresetRecord> presetMap;
    private final Lazy<NameMap<String>> nameMap = Lazy.of(this::buildNameMap);

    public VisualPresets(Map<String, VisualPresetRecord> presetMap) {
        this.presetMap = presetMap;
    }

    private NameMap<String> buildNameMap() {
        List<String> values = new ArrayList<>(presetMap.keySet());
        values.addFirst("");
        return NameMap.of("", values).name(this::getDisplayName).create();
    }

    private Component getDisplayName(String name) {
        return name.isEmpty() ?
                Component.translatable("ftbquests.null").withStyle(ChatFormatting.ITALIC) :
                Component.literal(name);
    }

    public static VisualPresets makeDefaults() {
        Map<String, VisualPresetRecord> map = Util.make(new HashMap<>(), m -> {
            m.put("normal", new VisualPresetRecord("square", 1.0));
            m.put("info", new VisualPresetRecord("gear", 1.0));
            m.put("goal", new VisualPresetRecord("hexagon", 2.0));
        });

        return new VisualPresets(map);
    }

    public Optional<VisualPreset> get(String name) {
        return Optional.ofNullable(presetMap.get(name));
    }

    public Tag serialize() {
        return CODEC.encodeStart(NbtOps.INSTANCE, presetMap)
                .resultOrPartial(e -> FTBQuests.LOGGER.error("can't serialize presets! {}", e))
                .orElseGet(CompoundTag::new);
    }

    public static VisualPresets deserialize(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(e -> FTBQuests.LOGGER.error("can't deserialize presets! {}", e))
                .map(VisualPresets::new)
                .orElse(EMPTY);
    }

    public Map<String, VisualPresetRecord> allPresets() {
        return Collections.unmodifiableMap(presetMap);
    }

    public NameMap<String> nameMap() {
        return nameMap.get();
    }
}
