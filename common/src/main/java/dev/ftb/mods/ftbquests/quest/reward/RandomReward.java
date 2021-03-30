package dev.ftb.mods.ftbquests.quest.reward;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import dev.ftb.mods.ftbquests.util.OrderedCompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class RandomReward extends Reward {
	public RewardTable table;

	public RandomReward(Quest parent) {
		super(parent);
		table = null;
	}

	@Override
	public RewardType getType() {
		return RewardTypes.RANDOM;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);

		if (getTable() != null) {
			nbt.putLong("table_id", table.id);

			if (table.id == -1L) {
				OrderedCompoundTag tag = new OrderedCompoundTag();
				table.writeData(tag);
				nbt.put("table_data", tag);
			}
		}
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		table = null;
		QuestFile file = getQuestFile();

		long id = nbt.getLong("table_id");

		if (id != 0L) {
			table = file.getRewardTable(id);
		} else {
			int index = nbt.contains("table") ? nbt.getInt("table") : -1;

			if (index >= 0 && index < file.rewardTables.size()) {
				table = file.rewardTables.get(index);
			}
		}

		if (table == null && nbt.contains("table_data")) {
			table = new RewardTable(file);
			table.readData(nbt.getCompound("table_data"));
			table.id = -1L;
			table.title = "Internal";
		}
	}

	@Nullable
	public RewardTable getTable() {
		if (table != null && table.invalid) {
			table = null;
		}

		return table;
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);

		table = getTable();
		buffer.writeLong(table == null ? 0L : table.id);

		if (table != null && table.id == -1L) {
			table.writeNetData(buffer);
		}
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		QuestFile file = getQuestFile();

		long t = buffer.readLong();

		if (t == -1L) {
			table = new RewardTable(file);
			table.readNetData(buffer);
			table.id = -1L;
			table.title = "Internal";
		} else {
			table = file.getRewardTable(t);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.add("table", new ConfigQuestObject<>(QuestObjectType.REWARD_TABLE), table, v -> table = v, getTable()).setNameKey("ftbquests.reward_table");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		if (getTable() == null) {
			return;
		}

		int totalWeight = getTable().getTotalWeight(false);

		if (totalWeight <= 0) {
			return;
		}

		int number = player.level.random.nextInt(totalWeight) + 1;
		int currentWeight = 0;

		for (WeightedReward reward : getTable().rewards) {
			currentWeight += reward.weight;

			if (currentWeight >= number) {
				reward.reward.claim(player, notify);
				return;
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Component getAltTitle() {
		return getTable() == null ? super.getAltTitle() : getTable().useTitle ? getTable().getTitle() : super.getAltTitle();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		return getTable() == null ? super.getAltIcon() : getTable().getIcon();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void addMouseOverText(TooltipList list) {
		if (getTable() != null) {
			getTable().addMouseOverText(list, true, false);
		}
	}

	@Override
	public boolean getExcludeFromClaimAll() {
		return false;
	}

	@Override
	@Nullable
	@Environment(EnvType.CLIENT)
	public Object getIngredient() {
		return getTable() != null && getTable().lootCrate != null ? getTable().lootCrate.createStack() : null;
	}

	@Override
	public boolean automatedClaimPre(BlockEntity tileEntity, List<ItemStack> items, Random random, UUID playerId, @Nullable ServerPlayer player) {
		return false;
	}

	@Override
	public void automatedClaimPost(BlockEntity tileEntity, UUID playerId, @Nullable ServerPlayer player) {
	}
}