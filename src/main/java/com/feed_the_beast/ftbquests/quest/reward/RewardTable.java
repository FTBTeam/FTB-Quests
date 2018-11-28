package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.gui.GuiEditRewardTable;
import com.feed_the_beast.ftbquests.gui.GuiRewardTables;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObjectDirect;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class RewardTable extends QuestObjectBase
{
	public final QuestFile file;
	public final List<WeightedReward> rewards;
	public final Quest fakeQuest;
	public int emptyWeight;
	public int lootSize;

	public RewardTable(QuestFile f)
	{
		file = f;
		rewards = new ArrayList<>();
		fakeQuest = new Quest(new QuestChapter(file));
		emptyWeight = 0;
		lootSize = 27;
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.REWARD_TABLE;
	}

	@Override
	public QuestFile getQuestFile()
	{
		return file;
	}

	public int getTotalWeight(boolean includeEmpty)
	{
		int w = includeEmpty ? emptyWeight : 0;

		for (WeightedReward r : rewards)
		{
			w += r.weight;
		}

		return w;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setInteger("empty_weight", emptyWeight);
		nbt.setInteger("loot_size", lootSize);

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
		emptyWeight = nbt.getInteger("empty_weight");
		lootSize = nbt.getInteger("loot_size");

		rewards.clear();
		NBTTagList list = nbt.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			QuestReward reward = QuestRewardType.createReward(fakeQuest, nbt1.getString("type"));

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
		data.writeVarInt(emptyWeight);
		data.writeVarInt(lootSize);
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
		emptyWeight = data.readVarInt();
		lootSize = data.readVarInt();
		rewards.clear();
		int s = data.readVarInt();

		for (int i = 0; i < s; i++)
		{
			QuestRewardType type = QuestRewardType.getRegistry().getValue(data.readVarInt());
			QuestReward reward = type.provider.create(fakeQuest);
			reward.readNetData(data);
			int w = data.readVarInt();
			rewards.add(new WeightedReward(reward, w));
		}
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("empty_weight", () -> emptyWeight, v -> emptyWeight = v, 0, 0, Integer.MAX_VALUE);
		config.addInt("loot_size", () -> lootSize, v -> lootSize = v, 27, 1, Integer.MAX_VALUE);
	}

	@Override
	public void resetProgress(ITeamData data, boolean dependencies)
	{
	}

	@Override
	public void deleteSelf()
	{
		file.rewardTables.remove(this);
		super.deleteSelf();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void editedFromGUI()
	{
		GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

		if (gui != null && gui.selectedQuest != null)
		{
			gui.questRight.refreshWidgets();
		}
		else
		{
			GuiRewardTables gui1 = ClientUtils.getCurrentGuiAs(GuiRewardTables.class);

			if (gui1 != null)
			{
				gui1.refreshWidgets();
			}
		}
	}

	@Override
	public void onCreated()
	{
		file.rewardTables.add(this);
	}

	@Override
	public Icon getAltIcon()
	{
		if (rewards.isEmpty())
		{
			return GuiIcons.DICE;
		}

		List<Icon> icons = new ArrayList<>();

		for (WeightedReward reward : rewards)
		{
			icons.add(reward.reward.getIcon());
		}

		return IconAnimation.fromList(icons, false);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentTranslation("ftbquests.reward_table");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onEditButtonClicked()
	{
		new GuiEditRewardTable(this, () -> new MessageEditObjectDirect(this).sendToServer()).openGui();
	}

	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list, boolean includeWeight, boolean includeEmpty)
	{
		int totalWeight = getTotalWeight(includeEmpty);

		if (includeWeight && includeEmpty && emptyWeight > 0)
		{
			list.add(TextFormatting.GRAY + "- " + I18n.format("ftbquests.reward_table.nothing") + TextFormatting.DARK_GRAY + " [" + WeightedReward.chanceString(emptyWeight, totalWeight) + "]");
		}

		for (WeightedReward r : rewards)
		{
			if (includeWeight)
			{
				list.add(TextFormatting.GRAY + "- " + r.reward.getDisplayName().getFormattedText() + TextFormatting.DARK_GRAY + " [" + WeightedReward.chanceString(r.weight, totalWeight) + "]");
			}
			else
			{
				list.add(TextFormatting.GRAY + "- " + r.reward.getDisplayName().getFormattedText());
			}
		}
	}
}