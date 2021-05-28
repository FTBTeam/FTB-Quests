package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.gui.RewardNotificationsScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class LootReward extends RandomReward {
	public LootReward(Quest quest) {
		super(quest);
	}

	@Override
	public RewardType getType() {
		return RewardTypes.LOOT;
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		RewardTable table = getTable();

		if (table == null) {
			return;
		}

		for (WeightedReward reward : table.rewards) {
			if (reward.weight == 0) {
				reward.reward.claim(player, notify);
			}
		}

		int totalWeight = table.getTotalWeight(true);

		if (totalWeight <= 0) {
			return;
		}

		for (int i = 0; i < table.lootSize; i++) {
			int number = player.level.random.nextInt(totalWeight) + 1;
			int currentWeight = table.emptyWeight;

			if (currentWeight < number) {
				for (WeightedReward reward : table.rewards) {
					currentWeight += reward.weight;

					if (currentWeight >= number) {
						reward.reward.claim(player, notify);
						break;
					}
				}
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void addMouseOverText(TooltipList list) {
		if (getTable() != null) {
			getTable().addMouseOverText(list, true, true);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
		if (canClick) {
			new RewardNotificationsScreen().openGui();
		}

		super.onButtonClicked(button, canClick);
	}

	@Override
	public boolean getExcludeFromClaimAll() {
		return true;
	}

	@Override
	public boolean automatedClaimPre(BlockEntity tileEntity, List<ItemStack> items, Random random, UUID playerId, @Nullable ServerPlayer player) {
		return false;
	}

	@Override
	public void automatedClaimPost(BlockEntity tileEntity, UUID playerId, @Nullable ServerPlayer player) {
	}
}