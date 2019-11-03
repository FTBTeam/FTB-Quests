package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

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
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);

		if (getTable() != null)
		{
			nbt.setInteger("table", getQuestFile().rewardTables.indexOf(getTable()));
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		int index = nbt.hasKey("table") ? nbt.getInteger("table") : -1;

		QuestFile file = getQuestFile();

		if (index >= 0 && index < file.rewardTables.size())
		{
			table = file.rewardTables.get(index);
		}
		else
		{
			table = new RewardTable(file);
			NBTTagList list = nbt.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

			for (int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound nbt1 = list.getCompoundTagAt(i);
				Reward reward = RewardType.createReward(table.fakeQuest, nbt1.getString("type"));

				if (reward != null)
				{
					reward.readData(nbt1);
					table.rewards.add(new WeightedReward(reward, nbt1.getInteger("weight")));
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
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeInt(getTable() == null ? 0 : getTable().id);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		table = getQuestFile().getRewardTable(data.readInt());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.add("table", new ConfigQuestObject(getQuestFile(), getID(getTable()), QuestObjectType.REWARD_TABLE)
		{
			@Override
			public void setObject(int object)
			{
				table = file.getRewardTable(object);
			}
		}, new ConfigQuestObject(getQuestFile(), 0, QuestObjectType.REWARD_TABLE)).setDisplayName(new TextComponentTranslation("ftbquests.reward_table"));
	}

	@Override
	public void claim(EntityPlayerMP player, boolean notify)
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
	public String getAltTitle()
	{
		return getTable() == null ? super.getAltTitle() : getTable().useTitle ? getTable().getTitle() : super.getAltTitle();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list)
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
}