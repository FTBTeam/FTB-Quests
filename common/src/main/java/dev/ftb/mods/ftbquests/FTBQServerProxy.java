package dev.ftb.mods.ftbquests;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class FTBQServerProxy implements IQuestProxy {
    @Override
    public Collection<LootCrate> getKnownLootCrates() {
        return ServerQuestFile.INSTANCE != null ? ServerQuestFile.INSTANCE.getRewardTables().stream()
                .map(RewardTable::getLootCrate)
                .filter(Objects::nonNull)
                .toList() :
                List.of();
    }
}
