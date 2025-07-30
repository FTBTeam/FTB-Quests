package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.config.NameMap;
import net.minecraft.network.chat.Component;

public enum AutoPinTarget {
    QUEST_BOOK("file"),
    CHAPTER("chapter");

    public static final NameMap<AutoPinTarget> NAME_MAP = NameMap.of(CHAPTER, AutoPinTarget.values())
            .id(AutoPinTarget::getId)
            .name(n -> Component.translatable("ftbquests." + n.id))
            .create();

    private final String id;

    AutoPinTarget(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
