package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.gui.GuiEditRandomReward;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObjectDirect;
import com.feed_the_beast.ftbquests.quest.Quest;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class RandomReward extends QuestReward
{
	public static class WeightedReward
	{
		public final QuestReward reward;
		public int weight;

		public WeightedReward(QuestReward r, int w)
		{
			reward = r;
			weight = Math.max(w, 1);
		}
	}

	public final List<WeightedReward> rewards;

	public RandomReward(Quest quest)
	{
		super(quest);
		rewards = new ObjectArrayList<>();
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
		NBTTagList list = new NBTTagList();

		for (WeightedReward reward : rewards)
		{
			NBTTagCompound nbt1 = new NBTTagCompound();
			reward.reward.writeData(nbt1);

			if (reward.reward.getType() != FTBQuestsRewards.ITEM)
			{
				nbt1.setString("type", reward.reward.getType().getTypeForNBT());
			}

			if (reward.weight > 1)
			{
				nbt1.setInteger("weight", reward.weight);
			}

			list.appendTag(nbt1);
		}

		nbt.setTag("rewards", list);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		rewards.clear();
		NBTTagList list = nbt.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			QuestReward reward = QuestRewardType.createReward(quest, nbt1.getString("type"));

			if (reward != null)
			{
				reward.readData(nbt1);
				rewards.add(new WeightedReward(reward, nbt1.getInteger("weight")));
			}
		}
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(rewards.size());

		for (WeightedReward reward : rewards)
		{
			data.writeVarInt(QuestRewardType.getRegistry().getID(reward.reward.getType()));
			reward.reward.writeNetData(data);
			data.writeVarInt(reward.weight);
		}
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		rewards.clear();
		int s = data.readVarInt();

		for (int i = 0; i < s; i++)
		{
			QuestRewardType type = QuestRewardType.getRegistry().getValue(data.readVarInt());
			QuestReward reward = type.provider.create(quest);
			reward.readNetData(data);
			int w = data.readVarInt();
			rewards.add(new WeightedReward(reward, w));
		}
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		int totalWeight = 0;

		for (WeightedReward reward : rewards)
		{
			totalWeight += reward.weight;
		}

		int number = player.world.rand.nextInt(totalWeight) + 1;
		int currentWeight = 0;

		for (WeightedReward reward : rewards)
		{
			currentWeight += reward.weight;

			if (currentWeight >= number)
			{
				reward.reward.claim(player);
				break;
			}
		}
	}

	@Override
	public Icon getAltIcon()
	{
		if (rewards.isEmpty())
		{
			return super.getAltIcon();
		}

		List<Icon> icons = new ArrayList<>();

		for (WeightedReward reward : rewards)
		{
			icons.add(reward.reward.getIcon());
		}

		return IconAnimation.fromList(icons, false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list)
	{
		int totalWeight = 0;

		for (WeightedReward reward : rewards)
		{
			totalWeight += reward.weight;
		}

		for (WeightedReward reward : rewards)
		{
			list.add(TextFormatting.GRAY + "- " + reward.reward.getDisplayName().getFormattedText() + TextFormatting.DARK_GRAY + " [" + (reward.weight * 100 / totalWeight) + "%]");
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onEditButtonClicked()
	{
		new GuiEditRandomReward(this, () -> new MessageEditObjectDirect(this).sendToServer()).openGui();
	}
}