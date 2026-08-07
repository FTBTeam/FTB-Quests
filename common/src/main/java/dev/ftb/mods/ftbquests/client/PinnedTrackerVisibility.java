package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.config.NameMap;

public enum PinnedTrackerVisibility {
    ALL("all"),
    QUESTS_ONLY("quests_only"),
    HIDDEN("hidden");

    public static final NameMap<PinnedTrackerVisibility> NAME_MAP = NameMap.of(ALL, PinnedTrackerVisibility.values())
            .id(p -> p.id)
            .baseNameKey("ftbquests.pinned.visibility")
            .create();

    private final String id;

    PinnedTrackerVisibility(String id) {
        this.id = id;
    }

    public PinnedTrackerVisibility next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
