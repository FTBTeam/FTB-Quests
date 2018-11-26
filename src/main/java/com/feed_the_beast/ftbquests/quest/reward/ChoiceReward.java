package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.gui.GuiEditChoiceReward;
import com.feed_the_beast.ftbquests.gui.GuiSelectChoiceReward;
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
public class ChoiceReward extends QuestReward
{
	public final List<QuestReward> rewards;

	public ChoiceReward(Quest quest)
	{
		super(quest);
		rewards = new ObjectArrayList<>();
	}

	@Override
	public QuestRewardType getType()
	{
		return FTBQuestsRewards.CHOICE;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		NBTTagList list = new NBTTagList();

		for (QuestReward reward : rewards)
		{
			NBTTagCompound nbt1 = new NBTTagCompound();
			reward.writeData(nbt1);

			if (reward.getType() != FTBQuestsRewards.ITEM)
			{
				nbt1.setString("type", reward.getType().getTypeForNBT());
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
				rewards.add(reward);
			}
		}
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(rewards.size());

		for (QuestReward reward : rewards)
		{
			data.writeVarInt(QuestRewardType.getRegistry().getID(reward.getType()));
			reward.writeNetData(data);
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
			rewards.add(reward);
		}
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
	}

	@Override
	public Icon getAltIcon()
	{
		if (rewards.isEmpty())
		{
			return super.getIcon();
		}

		List<Icon> icons = new ArrayList<>();

		for (QuestReward reward : rewards)
		{
			icons.add(reward.getIcon());
		}

		return IconAnimation.fromList(icons, false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list)
	{
		for (QuestReward reward : rewards)
		{
			list.add(TextFormatting.GRAY + "- " + reward.getDisplayName().getFormattedText());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onButtonClicked()
	{
		new GuiSelectChoiceReward(this).openGui();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onEditButtonClicked()
	{
		new GuiEditChoiceReward(this, () -> new MessageEditObjectDirect(this).sendToServer()).openGui();
	}
}