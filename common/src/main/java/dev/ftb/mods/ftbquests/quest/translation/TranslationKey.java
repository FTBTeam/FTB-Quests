package dev.ftb.mods.ftbquests.quest.translation;

import com.mojang.datafixers.util.Either;
import dev.ftb.mods.ftblibrary.config.NameMap;

import java.util.List;

public enum TranslationKey {
    TITLE("title", false),
    QUEST_SUBTITLE("quest_subtitle", false),
    QUEST_DESC("quest_desc", true),
    CHAPTER_SUBTITLE("chapter_subtitle", true),
    ;

    public static final NameMap<TranslationKey> NAME_MAP = NameMap.of(TITLE, values()).id(v -> v.name).create();

    private final String name;
    private final boolean isListVal;

    TranslationKey(String name, boolean isListVal) {
        this.name = name;
        this.isListVal = isListVal;
    }

    public String getName() {
        return name;
    }

    public boolean isListVal() {
        return isListVal;
    }

    public Either<String, List<String>> validate(Either<String, List<String>> either) {
        either.ifLeft(s -> {
            if (isListVal) throw new IllegalArgumentException("expected a list value!");
        }).ifRight(l -> {
            if (!isListVal) throw new IllegalArgumentException("expected a string value!");
        });
        return either;
    }

    public String getTranslationKey() {
        return "ftbquests.translation_key." + name;
    }
}