package com.feed_the_beast.ftbquests.quest.loot;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.Bits;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.gui.GuiEditRewardTable;
import com.feed_the_beast.ftbquests.gui.GuiRewardTables;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObjectDirect;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;

import java.io.File;
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
	public boolean hideTooltip;
	public boolean useTitle;
	public LootCrate lootCrate;

	public RewardTable(QuestFile f)
	{
		file = f;
		rewards = new ArrayList<>();
		fakeQuest = new Quest(new QuestChapter(file));
		emptyWeight = 0;
		lootSize = 27;
		hideTooltip = false;
		useTitle = false;
		lootCrate = null;
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

		if (emptyWeight > 0)
		{
			nbt.setInteger("empty_weight", emptyWeight);
		}

		nbt.setInteger("loot_size", lootSize);

		if (hideTooltip)
		{
			nbt.setBoolean("hide_tooltip", true);
		}

		if (useTitle)
		{
			nbt.setBoolean("use_title", true);
		}

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

		if (lootCrate != null)
		{
			NBTTagCompound nbt1 = new NBTTagCompound();
			lootCrate.writeData(nbt1);
			nbt.setTag("loot_crate", nbt1);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		emptyWeight = nbt.getInteger("empty_weight");
		lootSize = nbt.getInteger("loot_size");
		hideTooltip = nbt.getBoolean("hide_tooltip");
		useTitle = nbt.getBoolean("use_title");

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

		lootCrate = null;

		if (nbt.hasKey("loot_crate"))
		{
			lootCrate = new LootCrate(this);
			lootCrate.readData(nbt.getCompoundTag("loot_crate"));
		}
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(emptyWeight);
		data.writeVarInt(lootSize);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, hideTooltip);
		flags = Bits.setFlag(flags, 2, useTitle);
		flags = Bits.setFlag(flags, 4, lootCrate != null);
		data.writeVarInt(flags);
		data.writeVarInt(rewards.size());

		for (WeightedReward reward : rewards)
		{
			data.writeVarInt(QuestRewardType.getRegistry().getID(reward.reward.getType()));
			reward.reward.writeNetData(data);
			data.writeVarInt(reward.weight);
		}

		if (lootCrate != null)
		{
			lootCrate.writeNetData(data);
		}
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		emptyWeight = data.readVarInt();
		lootSize = data.readVarInt();
		int flags = data.readVarInt();
		hideTooltip = Bits.getFlag(flags, 1);
		useTitle = Bits.getFlag(flags, 2);
		boolean hasCrate = Bits.getFlag(flags, 4);
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

		lootCrate = null;

		if (hasCrate)
		{
			lootCrate = new LootCrate(this);
			lootCrate.readNetData(data);
		}
	}

	@Override
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.addInt("empty_weight", () -> emptyWeight, v -> emptyWeight = v, 0, 0, Integer.MAX_VALUE);
		config.addInt("loot_size", () -> lootSize, v -> lootSize = v, 27, 1, Integer.MAX_VALUE);
		config.addBool("hide_tooltip", () -> hideTooltip, v -> hideTooltip = v, false);
		config.addBool("use_title", () -> useTitle, v -> useTitle = v, false);

		if (lootCrate != null)
		{
			ConfigGroup lc = config.getGroup("loot_crate");
			lc.setDisplayName(new TextComponentTranslation("item.ftbquests.lootcrate.name"));
			lootCrate.getConfig(lc);
		}
	}

	@Override
	public void clearCachedData()
	{
		super.clearCachedData();

		for (WeightedReward reward : rewards)
		{
			reward.reward.clearCachedData();
		}
	}

	@Override
	public void deleteSelf()
	{
		file.rewardTables.remove(this);
		super.deleteSelf();
	}

	@Override
	public void editedFromGUI()
	{
		GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

		if (gui != null && gui.getViewedQuest() != null)
		{
			gui.viewQuestPanel.refreshWidgets();
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
	public File getFile(File folder)
	{
		return new File(folder, "reward_tables/" + getCodeString(this) + ".nbt");
	}

	@Override
	public Icon getAltIcon()
	{
		if (lootCrate != null)
		{
			return ItemIcon.getItemIcon(lootCrate.createStack());
		}

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
	public String getAltTitle()
	{
		if (rewards.size() == 1)
		{
			return rewards.get(0).reward.getTitle();
		}

		return I18n.format("ftbquests.reward_table");
	}

	@Override
	public void onEditButtonClicked()
	{
		new GuiEditRewardTable(this, () -> new MessageEditObjectDirect(this).sendToServer()).openGui();
	}

	public void addMouseOverText(List<String> list, boolean includeWeight, boolean includeEmpty)
	{
		if (hideTooltip)
		{
			return;
		}

		int totalWeight = getTotalWeight(includeEmpty);

		if (includeWeight && includeEmpty && emptyWeight > 0)
		{
			list.add(TextFormatting.GRAY + "- " + I18n.format("ftbquests.reward_table.nothing") + TextFormatting.DARK_GRAY + " [" + WeightedReward.chanceString(emptyWeight, totalWeight) + "]");
		}

		List<WeightedReward> rewards1;

		if (rewards.size() > 1)
		{
			rewards1 = new ArrayList<>(rewards);
			rewards1.sort(null);
		}
		else
		{
			rewards1 = rewards;
		}

		for (int i = 0; i < rewards1.size(); i++)
		{
			if (i == 10)
			{
				list.add(TextFormatting.GRAY + "- " + I18n.format("ftbquests.reward_table.and_more", rewards1.size() - 10));
				return;
			}

			WeightedReward r = rewards1.get(i);

			if (includeWeight)
			{
				list.add(TextFormatting.GRAY + "- " + r.reward.getTitle() + TextFormatting.DARK_GRAY + " [" + WeightedReward.chanceString(r.weight, totalWeight) + "]");
			}
			else
			{
				list.add(TextFormatting.GRAY + "- " + r.reward.getTitle());
			}
		}
	}

	@Override
	public int refreshJEI()
	{
		return FTBQuestsJEIHelper.LOOTCRATES;
	}
}