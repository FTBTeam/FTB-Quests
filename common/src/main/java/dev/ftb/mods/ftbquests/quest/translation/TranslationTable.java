package dev.ftb.mods.ftbquests.quest.translation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import com.mojang.datafixers.util.Either;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.common.collect.Maps;

public class TranslationTable {
    public static final StreamCodec<FriendlyByteBuf, TranslationTable> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    Maps::newHashMapWithExpectedSize,
                    ByteBufCodecs.STRING_UTF8,
                    ByteBufCodecs.either(ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()))
            ), t -> t.map,
            TranslationTable::new
    );

    private final Map<String, Either<String, List<String>>> map;
    private boolean saveNeeded;

    public TranslationTable() {
        this.map = new HashMap<>();
    }

    public static TranslationTable fromNBT(CompoundTag tag) {
        Map<String, Either<String, List<String>>> map = new HashMap<>();
        tag.keySet().forEach(k -> {
            switch (tag.get(k)) {
                case StringTag str -> map.put(k, Either.left(str.asString().orElseThrow()));
                case ListTag list -> map.put(k, Either.right(list.stream().map(e -> e.asString().orElseThrow()).toList()));
                case null, default -> { }
            }
        });
        return new TranslationTable(map);
    }

    private TranslationTable(Map<String, Either<String, List<String>>> map) {
        this.map = map;
    }

    boolean isSaveNeeded() {
        return saveNeeded;
    }

    void setSaveNeeded(boolean saveNeeded) {
        this.saveNeeded = saveNeeded;
    }

    public Optional<String> getStringTranslation(String key) {
        return Optional.ofNullable(map.get(key)).flatMap(e -> e.left());
    }

    public Optional<List<String>> getStringListTranslation(String key) {
        return Optional.ofNullable(map.get(key)).flatMap(e -> e.right());
    }

    public void put(String key, String message) {
        map.put(key, Either.left(message));
        setSaveNeeded(true);
    }

    public void put(String key, List<String> message) {
        map.put(key, Either.right(message));
        setSaveNeeded(true);
    }

    public void remove(String key) {
        if (map.remove(key) != null) {
            setSaveNeeded(true);
        }
    }

    public CompoundTag saveToNBT() {
        return Util.make(new CompoundTag(), tag ->
                map.forEach((key, val) -> val
                        .ifLeft(str -> {
                            if (!str.isEmpty()) tag.putString(key, str);
                        })
                        .ifRight(list -> {
                            if (!list.isEmpty()) tag.put(key, listOfStr(list));
                        })
                ));
    }

    static ListTag listOfStr(List<String> l) {
        return Util.make(new ListTag(), t -> l.forEach(s -> t.add(StringTag.valueOf(s))));
    }

    public int size() {
        return map.size();
    }

    public boolean contains(String key) {
        return map.containsKey(key);
    }
}
