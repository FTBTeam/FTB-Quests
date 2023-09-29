package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftbquests.IQuestProxy;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class FTBQClientProxy implements IQuestProxy {
    @Override
    public Collection<LootCrate> getKnownLootCrates() {
        return ClientQuestFile.exists() ? ClientQuestFile.INSTANCE.getRewardTables().stream()
                .map(RewardTable::getLootCrate)
                .filter(Objects::nonNull)
                .toList() :
                List.of();
    }
}