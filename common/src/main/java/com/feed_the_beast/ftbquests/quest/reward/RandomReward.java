package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import com.feed_the_beast.ftbquests.util.OrderedCompoundTag;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
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
public class RandomReward extends Reward
{
	public RewardTable table;

	public RandomReward(Quest parent)
	{
		super(parent);
		table = null;
	}

	@Override
	public RewardType getType()
	{
		return RewardTypes.RANDOM;
	}

	@Override
	public void writeData(CompoundTag nbt)
	{
		super.writeData(nbt);

		if (getTable() != null)
		{
			nbt.putInt("table_id", getTable().id);
		}

		if (table.id == -1)
		{
			OrderedCompoundTag tag = new OrderedCompoundTag();
			table.writeData(tag);
			nbt.put("table_data", tag);
		}
	}

	@Override
	public void readData(CompoundTag nbt)
	{
		super.readData(nbt);
		QuestFile file = getQuestFile();

		int id = nbt.getInt("table_id");

		if (id != 0)
		{
			table = file.getRewardTable(id);
		}
		else
		{
			int index = nbt.contains("table") ? nbt.getInt("table") : -1;

			if (index >= 0 && index < file.rewardTables.size())
			{
				table = file.rewardTables.get(index);
			}
		}

		if (table == null && nbt.contains("table_data"))
		{
			table = new RewardTable(file);
			table.readData(nbt.getCompound("table_data"));
			table.id = -1;
			table.title = "Internal";
		}
	}

	@Nullable
	public RewardTable getTable()
	{
		if (table != null && table.invalid)
		{
			table = null;
		}

		return table;
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer)
	{
		super.writeNetData(buffer);

		RewardTable table = getTable();
		buffer.writeInt(table == null ? 0 : table.id);

		if (table != null && table.id == -1)
		{
			table.writeNetData(buffer);
		}
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer)
	{
		super.readNetData(buffer);
		QuestFile file = getQuestFile();

		int t = buffer.readInt();

		if (t == -1)
		{
			table = new RewardTable(file);
			table.readNetData(buffer);
			table.id = -1;
			table.title = "Internal";
		}
		else
		{
			table = file.getRewardTable(t);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.add("table", new ConfigQuestObject<>(QuestObjectType.REWARD_TABLE), table, v -> table = v, getTable()).setNameKey("ftbquests.reward_table");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify)
	{
		if (getTable() == null)
		{
			return;
		}

		int totalWeight = getTable().getTotalWeight(false);

		if (totalWeight <= 0)
		{
			return;
		}

		int number = player.level.random.nextInt(totalWeight) + 1;
		int currentWeight = 0;

		for (WeightedReward reward : getTable().rewards)
		{
			currentWeight += reward.weight;

			if (currentWeight >= number)
			{
				reward.reward.claim(player, notify);
				return;
			}
		}
	}

	@Override
	public Icon getAltIcon()
	{
		return getTable() == null ? super.getAltIcon() : getTable().getIcon();
	}

	@Override
	public Component getAltTitle()
	{
		return getTable() == null ? super.getAltTitle() : getTable().useTitle ? getTable().getTitle() : super.getAltTitle();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void addMouseOverText(TooltipList list)
	{
		if (getTable() != null)
		{
			getTable().addMouseOverText(list, true, false);
		}
	}

	@Override
	public boolean getExcludeFromClaimAll()
	{
		return false;
	}

	@Override
	@Nullable
	public Object getIngredient()
	{
		return getTable() != null && getTable().lootCrate != null ? getTable().lootCrate.createStack() : null;
	}

	@Override
	public boolean automatedClaimPre(BlockEntity tileEntity, List<ItemStack> items, Random random, UUID playerId, @Nullable ServerPlayer player)
	{
		return false;
	}

	@Override
	public void automatedClaimPost(BlockEntity tileEntity, UUID playerId, @Nullable ServerPlayer player)
	{
	}
}