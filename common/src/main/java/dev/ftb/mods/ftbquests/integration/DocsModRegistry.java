package dev.ftb.mods.ftbquests.integration;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public enum DocsModRegistry {
    INSTANCE;

    private static final Map<String, DocsMod> registry = new ConcurrentHashMap<>();

    public void registerDocsMod(String id, DocsMod mod) {
        registry.put(id, mod);
    }

    public Optional<DocsMod> getDocsMod(String id) {
        return Optional.ofNullable(registry.get(id));
    }
}
