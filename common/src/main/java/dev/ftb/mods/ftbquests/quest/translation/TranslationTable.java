package dev.ftb.mods.ftbquests.quest.translation;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TranslationTable {
    private final Map<String, Either<String, List<String>>> map;
    private boolean saveNeeded;

    public TranslationTable(FriendlyByteBuf buffer) {
        this.map = new HashMap<>();

        buffer.readMap(Maps::newHashMapWithExpectedSize, buf -> buf.readUtf(), buf -> buf.readEither(buf1 -> buf1.readUtf(), buf1 -> buf1.readList(buf2 -> buf2.readUtf())));
    }

    public TranslationTable() {
        this.map = new HashMap<>();
    }

    public static TranslationTable fromNBT(CompoundTag tag) {
        Map<String, Either<String, List<String>>> map = new HashMap<>();
        tag.getAllKeys().forEach(k -> {
            Tag i = tag.get(k);
            if (i instanceof StringTag str) {
                map.put(k, Either.left(str.getAsString()));
            } else if (i instanceof ListTag list) {
                map.put(k, Either.right(list.stream().map(Tag::getAsString).toList()));
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