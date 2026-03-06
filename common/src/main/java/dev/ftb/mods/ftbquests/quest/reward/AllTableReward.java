package dev.ftb.mods.ftbquests.quest.reward;

import net.minecraft.server.level.ServerPlayer;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;

public class AllTableReward extends LootReward {
    public AllTableReward(long id, Quest parent) {
        super(id, parent);
    }

    @Override
    public RewardType getType() {
        return RewardTypes.ALL_TABLE;
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        RewardTable table = getTable();

        if (table != null) {
            for (WeightedReward wr : table.getWeightedRewards()) {
                wr.getReward().claim(player, notify);
            }
        }
    }
}
