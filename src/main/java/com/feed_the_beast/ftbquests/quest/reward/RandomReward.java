package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

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
		return FTBQuestsRewards.RANDOM;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);

		if (getTable() != null)
		{
			nbt.putInt("table", getQuestFile().rewardTables.indexOf(getTable()));
		}
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		int index = nbt.contains("table") ? nbt.getInt("table") : -1;

		QuestFile file = getQuestFile();

		if (index >= 0 && index < file.rewardTables.size())
		{
			table = file.rewardTables.get(index);
		}
		else
		{
			table = new RewardTable(file);
			ListNBT list = nbt.getList("rewards", Constants.NBT.TAG_COMPOUND);

			for (int i = 0; i < list.size(); i++)
			{
				CompoundNBT nbt1 = list.getCompound(i);
				Reward reward = RewardType.createReward(table.fakeQuest, nbt1.getString("type"));

				if (reward != null)
				{
					reward.readData(nbt1);
					table.rewards.add(new WeightedReward(reward, nbt1.getInt("weight")));
				}
			}

			table.id = file.newID();
			table.title = getUnformattedTitle() + " " + toString();
			file.rewardTables.add(table);
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
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeInt(getTable() == null ? 0 : getTable().id);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		table = getQuestFile().getRewardTable(buffer.readInt());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.add("table", new ConfigQuestObject<>(QuestObjectType.REWARD_TABLE), table, v -> table = v, getTable()).setNameKey("ftbquests.reward_table");
	}

	@Override
	public void claim(ServerPlayerEntity player, boolean notify)
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

		int number = player.world.rand.nextInt(totalWeight) + 1;
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
	public IFormattableTextComponent getAltTitle()
	{
		return getTable() == null ? super.getAltTitle() : getTable().useTitle ? getTable().getTitle() : super.getAltTitle();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
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
	public boolean automatedClaimPre(TileEntity tileEntity, List<ItemStack> items, Random random, UUID playerId, @Nullable ServerPlayerEntity player)
	{
		return false;
	}

	@Override
	public void automatedClaimPost(TileEntity tileEntity, UUID playerId, @Nullable ServerPlayerEntity player)
	{
	}
}