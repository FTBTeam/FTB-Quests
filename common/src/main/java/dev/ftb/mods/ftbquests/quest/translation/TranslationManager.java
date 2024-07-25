package dev.ftb.mods.ftbquests.quest.translation;

import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientConfig;
import dev.ftb.mods.ftbquests.net.SyncTranslationTableMessage;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.FTBQuestsServerPlayer;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TranslationManager {
    private static final Pattern LANG_FILE_PAT = Pattern.compile("^\\w+\\.snbt$");

    private final Map<String, TranslationTable> map = new HashMap<>();

    private final String fallbackLocale = "en_us";

    public TranslationManager() {
        map.put(fallbackLocale, new TranslationTable());
    }

    public static void syncTable(ServerPlayer player, String language) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file != null && file.isValid()) {
            file.getTranslationManager().sendTableToPlayer(player, language);
        }
    }

    public void loadFromNBT(Path langFolder) {
        map.clear();

        if (!Files.exists(langFolder)) {
            // the first run, hopefully...
            try {
                Files.createDirectory(langFolder);
            } catch (IOException e) {
                FTBQuests.LOGGER.error("can't create lang folder {}: {}", langFolder, e.getMessage());
                return;
            }
        }

        try (Stream<Path> s = Files.list(langFolder)) {
            s.filter(TranslationManager::isValidLangFile).forEach(path -> {
                CompoundTag langNBT = SNBT.read(path);
                if (langNBT != null) {
                    String locale = (path.getFileName().toString().split("\\.", 2))[0].toLowerCase(Locale.ROOT);
                    map.put(locale, TranslationTable.fromNBT(langNBT));
                } else {
                    FTBQuests.LOGGER.error("can't read lang file {}", path);
                }
            });
            if (!map.containsKey(fallbackLocale)) {
                map.put(fallbackLocale, new TranslationTable());
            }
            FTBQuests.LOGGER.info("loaded translation tables for {} language(s)", map.size());
        } catch (IOException e) {
            FTBQuests.LOGGER.error("can't scan lang folder {}: {}", langFolder, e.getMessage());
        }
    }

    public void saveToNBT(Path langFolder) {
        map.forEach((locale, table) -> {
            if (table.isSaveNeeded()) {
                boolean prevSort = SNBT.setShouldSortKeysOnWrite(true);
                Path savePath = langFolder.resolve(locale + ".snbt");
                if (!SNBT.write(savePath, table.saveToNBT())) {
                    FTBQuests.LOGGER.error("can't write lang file {}", savePath);
                }
                table.setSaveNeeded(false);
                SNBT.setShouldSortKeysOnWrite(prevSort);
            }
        });
    }

    private static boolean isValidLangFile(Path p) {
        return LANG_FILE_PAT.matcher(p.getFileName().toString()).matches();
    }

    public Optional<String> getStringTranslation(QuestObjectBase object, String locale, TranslationKey subKey) {
        String key = makeKey(object, subKey);
        TranslationTable table = map.get(locale);
        if (table != null && table.contains(key)) {
            return table.getStringTranslation(key);
        } else {
            return map.get(fallbackLocale).getStringTranslation(key);
        }
    }

    public Optional<List<String>> getStringListTranslation(QuestObjectBase object, String locale, TranslationKey subKey) {
        String key = makeKey(object, subKey);
        TranslationTable table = map.get(locale);
        if (table != null && table.contains(key)) {
            return table.getStringListTranslation(key);
        } else {
            return map.get(fallbackLocale).getStringListTranslation(key);
        }
    }

    private Optional<TranslationTable> getTable(String locale) {
        return Optional.ofNullable(map.get(locale))
                .or(() -> Optional.ofNullable(map.get(fallbackLocale)));
    }

    private boolean hasTranslationForLocale(QuestObjectBase object, String locale, TranslationKey subKey) {
        return map.containsKey(locale) ? getTable(locale).map(t -> t.contains(makeKey(object, subKey))).orElse(false) : false;
    }

    public boolean hasMissingTranslation(QuestObjectBase object, TranslationKey key) {
        String locale = object.getQuestFile().getLocale();
        return object.getQuestFile().canEdit()
                && FTBQuestsClientConfig.HILITE_MISSING.get()
                && !locale.equals("en_us")
                && hasTranslationForLocale(object, "en_us", key)
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

    private static @NotNull String makeKey(QuestObjectBase object, TranslationKey subKey) {
        return String.format("%s.%s.%s", object.getObjectType().getId(), QuestObjectBase.getCodeString(object), subKey.getName());
    }

    public void syncTableFromServer(String locale, TranslationTable table) {
        map.put(locale, table);
    }

    public void sendTranslationsToPlayer(ServerPlayer player) {
        // make sure player always has the fallback en_us translations
        sendTableToPlayer(player, "en_us");

        String lang = ((FTBQuestsServerPlayer) player).ftbquests$language();
        if (!lang.equals("en_us")) {
            sendTableToPlayer(player, lang);
        }
    }

    public void sendTableToPlayer(ServerPlayer player, String locale) {
        if (map.containsKey(locale)) {
            new SyncTranslationTableMessage(locale, map.getOrDefault(locale, new TranslationTable())).sendTo(player);
        }
    }

    public void addInitialTranslation(CompoundTag extra, String locale, TranslationKey translationKey, String value) {
        extra.putString("locale", locale);
        extra.put("translate", Util.make(new CompoundTag(), t -> t.putString(TranslationKey.NAME_MAP.getName(translationKey), value)));
    }

    public void processInitialTranslation(CompoundTag extra, QuestObjectBase object) {
        if (extra.contains("locale") && extra.contains("translate")) {
            String locale = extra.getString("locale");
            TranslationTable table = map.computeIfAbsent(locale, k -> new TranslationTable());
            CompoundTag tag = extra.getCompound("translate");
            for (String keyStr : tag.getAllKeys()) {
                TranslationKey key = TranslationKey.NAME_MAP.getNullable(keyStr);
                if (key != null) {
                    table.put(makeKey(object, key), tag.getString(keyStr));
                }
            }
        }
    }
}