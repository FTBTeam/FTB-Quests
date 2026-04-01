package dev.ftb.mods.ftbquests.quest.translation;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientConfig;
import dev.ftb.mods.ftbquests.net.SyncTranslationTableMessage;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TranslationManager {
    private static final Pattern LANG_FILE_PAT = Pattern.compile("^\\w+$");

    private final Map<String, TranslationTable> map = new HashMap<>();

    public static final String DEFAULT_FALLBACK_LOCALE = "en_us";

    public TranslationManager() {
    }

    public static void syncTable(ServerPlayer player, String language) {
        ServerQuestFile.ifExists(instance -> instance.getTranslationManager().sendTableToPlayer(player, language));
    }

    private static String getFallbackLocale(QuestObjectBase object) {
        return object.getQuestFile().getFallbackLocale();
    }

    public void loadFromFile(BaseQuestFile file, Path langFolder) {
        map.clear();

        try {
            Files.createDirectories(langFolder);
        } catch (IOException e) {
            FTBQuests.LOGGER.error("can't create lang folder {}: {}", langFolder, e.getMessage());
            return;
        }

        try (Stream<Path> s = Files.list(langFolder)) {
            s.filter(TranslationManager::isValidLangDirectory).forEach(langDir -> {
                String locale = langDir.getFileName().toString();
                map.put(locale, TranslationTable.loadAndCombine(langDir));
            });

            if (!map.containsKey(file.getFallbackLocale())) {
                map.put(file.getFallbackLocale(), new TranslationTable());
            }
            FTBQuests.LOGGER.info("loaded translation tables for {} language(s)", map.size());
        } catch (IOException e) {
            FTBQuests.LOGGER.error("can't scan lang folder {}: {}", langFolder, e.getMessage());
        }
    }

    public void saveToFile(BaseQuestFile file, Path langFolder, boolean force) {
        map.forEach((locale, table) -> {
            Path localeDir = langFolder.resolve(locale);
            try {
                Files.createDirectories(localeDir);
                if (force || table.isSaveNeeded()) {
                    table.splitAndSerialize(file).forEach((path, json) -> {
                        Path fullPath = localeDir.resolve(path);
                        try {
                            Files.createDirectories(fullPath.getParent());
                            Json5Util.tryWrite(fullPath, json);
                        } catch (IOException e) {
                            FTBQuests.LOGGER.error("can't write lang file {}", fullPath);
                        }
                    });
                    table.setSaveNeeded(false);
                }
            } catch (IOException e) {
                FTBQuests.LOGGER.error("can't create lang directory structure {}", langFolder);
            }
        });
    }


    private static boolean isValidLangDirectory(Path p) {
        return Files.isDirectory(p) && LANG_FILE_PAT.matcher(p.getFileName().toString()).matches();
    }

    public Optional<String> getStringTranslation(QuestObjectBase object, String locale, TranslationKey subKey) {
        String key = makeKey(object, subKey);
        TranslationTable table = map.get(locale);
        if (table != null && table.contains(key)) {
            return table.getStringTranslation(key);
        } else {
            return map.computeIfAbsent(getFallbackLocale(object), k -> new TranslationTable()).getStringTranslation(key);
        }
    }

    public Optional<List<String>> getStringListTranslation(QuestObjectBase object, String locale, TranslationKey subKey) {
        String key = makeKey(object, subKey);
        TranslationTable table = map.get(locale);
        if (table != null && table.contains(key)) {
            return table.getStringListTranslation(key);
        } else {
            return map.computeIfAbsent(getFallbackLocale(object), k -> new TranslationTable()).getStringListTranslation(key);
        }
    }

    private Optional<TranslationTable> getTable(QuestObjectBase object, String locale) {
        return Optional.ofNullable(map.get(locale))
                .or(() -> Optional.ofNullable(map.get(getFallbackLocale(object))));
    }

    private boolean hasTranslationForLocale(QuestObjectBase object, String locale, TranslationKey subKey) {
        return map.containsKey(locale) ? getTable(object, locale).map(t -> t.contains(makeKey(object, subKey))).orElse(false) : false;
    }

    public boolean hasMissingTranslation(QuestObjectBase object, TranslationKey key) {
        BaseQuestFile file = object.getQuestFile();
        String locale = file.getLocale();
        return file.canEdit()
                && FTBQuestsClientConfig.HILITE_MISSING.get()
                && !locale.equals(file.getFallbackLocale())
                && hasTranslationForLocale(object, file.getFallbackLocale(), key)
                && !hasTranslationForLocale(object, locale, key);
    }

    public void addTranslation(QuestObjectBase object, String locale, TranslationKey subKey, String message) {
        addTranslation(locale, makeKey(object, subKey), message);
    }

    public void addTranslation(QuestObjectBase object, String locale, TranslationKey subKey, List<String> message) {
        addTranslation(locale, makeKey(object, subKey), message);
    }

    private void addTranslation(String locale, String key, List<String> message) {
        map.computeIfAbsent(locale, k -> new TranslationTable()).put(key, message);
    }

    private void addTranslation(String locale, String key, String message) {
        map.computeIfAbsent(locale, k -> new TranslationTable()).put(key, message);
    }

    public void removeAllTranslations(QuestObjectBase obj) {
        map.values().forEach(table -> {
            for (TranslationKey key : TranslationKey.values()) {
                table.remove(makeKey(obj, key));
            }
            table.setSaveNeeded(true);
        });
    }

    private static String makeKey(QuestObjectBase object, TranslationKey subKey) {
        return String.format("%s.%s.%s", object.getObjectType().getId(), QuestObjectBase.getCodeString(object), subKey.getName());
    }

    public void syncTableFromServer(String locale, TranslationTable table) {
        map.put(locale, table);
    }

    public void sendTranslationsToPlayer(ServerPlayer player) {
        Set<String> toSend = new HashSet<>();
        String fallback = ServerQuestFile.getInstance().getFallbackLocale();

        toSend.add(DEFAULT_FALLBACK_LOCALE);
        toSend.add(fallback);
        toSend.add(player.clientInformation().language());

        toSend.forEach(lang -> sendTableToPlayer(player, lang));
    }

    public void sendTableToPlayer(ServerPlayer player, String locale) {
        if (map.containsKey(locale)) {
            Server2PlayNetworking.send(player, new SyncTranslationTableMessage(locale, map.getOrDefault(locale, new TranslationTable())));
        }
    }

    public void addInitialTranslation(Json5Object extra, String locale, TranslationKey translationKey, String value) {
        extra.addProperty("locale", locale);
        extra.add("translate", Util.make(new Json5Object(),
                o -> o.addProperty(TranslationKey.NAME_MAP.getName(translationKey), value))
        );
    }

    public void processInitialTranslation(Json5Object extra, QuestObjectBase object) {
        if (extra.has("locale") && extra.has("translate")) {
            String locale = Json5Util.getString(extra,"locale").orElse("en_us");
            TranslationTable table = map.computeIfAbsent(locale, k -> new TranslationTable());
            Json5Util.getJson5Object(extra, "translate").ifPresent(translations -> {
                translations.asMap().forEach((keyStr, val) -> {
                    TranslationKey key = TranslationKey.NAME_MAP.getNullable(keyStr);
                    if (key != null) {
                        table.put(makeKey(object, key), val.getAsString());
                    }
                });
            });
        }
    }
}
