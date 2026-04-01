package dev.ftb.mods.ftbquests.quest.translation;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import de.marhali.json5.Json5Array;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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

    private TranslationTable(Map<String, Either<String, List<String>>> map) {
        this.map = map;
    }

    static TranslationTable loadAndCombine(Path langDir) {
        Json5Object combinedJson = new Json5Object();

        try (Stream<Path> stream = Files.walk(langDir)) {
            stream.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(BaseQuestFile.FILE_SUFFIX))
                    .forEach(p -> {
                        try {
                            Json5Util.tryRead(p).asMap().forEach(combinedJson::add);
                        } catch (IOException ex) {
                            FTBQuests.LOGGER.error("can't read lang file {}: {}", p, ex.getMessage());
                        }
                    });
        } catch (IOException ex) {
            FTBQuests.LOGGER.error("can't walk lang file tree at {}: {}", langDir, ex.getMessage());
        }

        return fromJson(combinedJson);
    }

    public Map<Path, Json5Object> splitAndSerialize(BaseQuestFile file) {
        Map<Path,Json5Object> splitTables = new HashMap<>();

        map.forEach((key, val) -> {
            String[] parts = key.split("\\.");
            QuestObject qo = file.get(BaseQuestFile.parseCodeString(parts[1]));
            if (qo != null) {
                Path path = getPathForQuestObject(qo);
                if (path != null) {
                    var json = splitTables.computeIfAbsent(path, _ -> new Json5Object());
                    val.ifLeft(str -> {
                        if (!str.isEmpty()) json.addProperty(key, str);
                    }).ifRight(list -> {
                        if (!list.isEmpty()) json.add(key, listOfStr(list));
                    });
                }
            }
        });

        return splitTables;
    }

    private static TranslationTable fromJson(Json5Object json) {
        Map<String, Either<String, List<String>>> map = new HashMap<>();

        json.asMap().forEach((k, v) -> {
            if (v.isJson5Primitive()) {
                map.put(k, Either.left(v.getAsString()));
            } else if (v.isJson5Array()) {
                map.put(k, Either.right(v.getAsJson5Array().asList().stream().map(Json5Element::getAsString).toList()));
            }
        });

        return new TranslationTable(map);
    }

    private static @Nullable Path getPathForQuestObject(QuestObject qo) {
        return switch (qo.getObjectType()) {
            case QUEST, TASK, QUEST_LINK -> qo.getQuestChapter() != null ?
                    Path.of("chapters").resolve(qo.getQuestChapter().getFilename() + BaseQuestFile.FILE_SUFFIX) :
                    null;
            default -> Path.of(qo.getObjectType().getId() + BaseQuestFile.FILE_SUFFIX);
        };
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

    static Json5Array listOfStr(List<String> l) {
        return Util.make(new Json5Array(), t -> l.forEach(t::add));
    }

    public int size() {
        return map.size();
    }

    public boolean contains(String key) {
        return map.containsKey(key);
    }
}
