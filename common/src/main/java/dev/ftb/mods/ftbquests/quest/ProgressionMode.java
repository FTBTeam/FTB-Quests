package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.util.NameMap;

public enum ProgressionMode {
    DEFAULT("default"),
    LINEAR("linear"),
    FLEXIBLE("flexible");

    public static final NameMap<ProgressionMode> NAME_MAP = NameMap.of(DEFAULT, values())
            .baseNameKey("ftbquests.file.progression_mode").create();
    public static final NameMap<ProgressionMode> NAME_MAP_NO_DEFAULT = NameMap.of(LINEAR, values())
            .baseNameKey("ftbquests.file.progression_mode").create();

    private final String id;

    ProgressionMode(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
