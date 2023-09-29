package dev.ftb.mods.ftbquests;

import dev.ftb.mods.ftbquests.quest.loot.LootCrate;

import java.util.Collection;

public interface IQuestProxy {
    Collection<LootCrate> getKnownLootCrates();
}
