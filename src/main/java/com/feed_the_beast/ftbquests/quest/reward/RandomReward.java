package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class RandomReward extends QuestReward
{
	public RewardTable table;

	public RandomReward(QuestObjectBase parent)
	{
		super(parent);
		table = parent.getQuestFile().dummyTable;
	}

	@Override
	public QuestRewardType getType()
	{
		return FTBQuestsRewards.RANDOM;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);

		if (getTable().id != 0 && !getTable().invalid)
		{
			nbt.setInteger("table", getQuestFile().rewardTables.indexOf(getTable()));
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		int index = nbt.hasKey("table") ? nbt.getInteger("table") : -1;

		if (index >= 0 && index < getQuestFile().rewardTables.size())
		{
			table = getQuestFile().rewardTables.get(index);
		}
		else
		{
			table = new RewardTable(getQuestFile());
			NBTTagList list = nbt.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

			for (int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound nbt1 = list.getCompoundTagAt(i);
				QuestReward reward = QuestRewardType.createReward(table.fakeQuest, nbt1.getString("type"));

				if (reward != null)
				{
					reward.readData(nbt1);
					table.rewards.add(new WeightedReward(reward, nbt1.getInteger("weight")));
				}
			}

			table.id = getQuestFile().readID(0);
			table.title = getDisplayName().getUnformattedText() + " " + toString();
			getQuestFile().rewardTables.add(table);
		}
	}

	public RewardTable getTable()
	{
		if (table == null || table.invalid)
		{
			table = getQuestFile().dummyTable;
		}

		return table;
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeInt(getTable().id);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		table = getQuestFile().getRewardTable(data.readInt());
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		Collection<QuestObjectType> set = Collections.singleton(QuestObjectType.REWARD_TABLE);
		config.add("table", new ConfigQuestObject(getQuestFile(), getTable(), set)
		{
			@Override
			public void setObject(@Nullable QuestObjectBase object)
			{
				if (object instanceof RewardTable)
				{
					table = (RewardTable) object;
				}
			}
		}, new ConfigQuestObject(getQuestFile(), getQuestFile().dummyTable, set)).setDisplayName(new TextComponentTranslation("ftbquests.reward_table"));
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
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
				reward.reward.claim(player);
				return;
			}
		}
	}

	@Override
	public Icon getAltIcon()
	{
		return getTable().getIcon();
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return getTable().useTitle ? getTable().getDisplayName() : super.getAltDisplayName();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list)
	{
		getTable().addMouseOverText(list, true, false);
	}

	@Override
	public boolean getExcludeFromClaimAll()
	{
		return false;
	}
}