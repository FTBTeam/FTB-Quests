package com.feed_the_beast.ftbquests.quest.loot;

import com.feed_the_beast.ftbquests.gui.GuiEditRewardTable;
import com.feed_the_beast.ftbquests.gui.GuiRewardTables;
import com.feed_the_beast.ftbquests.gui.quests.GuiQuests;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconAnimation;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.utils.Bits;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

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
		fakeQuest = new Quest(new Chapter(file));
		emptyWeight = 0;
		lootSize = 1;
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
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);

		if (emptyWeight > 0)
		{
			nbt.putInt("empty_weight", emptyWeight);
		}

		nbt.putInt("loot_size", lootSize);

		if (hideTooltip)
		{
			nbt.putBoolean("hide_tooltip", true);
		}

		if (useTitle)
		{
			nbt.putBoolean("use_title", true);
		}

		ListNBT list = new ListNBT();

		for (WeightedReward reward : rewards)
		{
			CompoundNBT nbt1 = new CompoundNBT();
			reward.reward.writeData(nbt1);

			if (reward.reward.getType() != FTBQuestsRewards.ITEM)
			{
				nbt1.putString("type", reward.reward.getType().getTypeForNBT());
			}

			if (reward.weight > 1)
			{
				nbt1.putInt("weight", reward.weight);
			}

			list.add(nbt1);
		}

		nbt.put("rewards", list);

		if (lootCrate != null)
		{
			CompoundNBT nbt1 = new CompoundNBT();
			lootCrate.writeData(nbt1);
			nbt.put("loot_crate", nbt1);
		}
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		emptyWeight = nbt.getInt("empty_weight");
		lootSize = nbt.getInt("loot_size");
		hideTooltip = nbt.getBoolean("hide_tooltip");
		useTitle = nbt.getBoolean("use_title");

		rewards.clear();
		ListNBT list = nbt.getList("rewards", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.size(); i++)
		{
			CompoundNBT nbt1 = list.getCompound(i);
			Reward reward = RewardType.createReward(fakeQuest, nbt1.getString("type"));

			if (reward != null)
			{
				reward.readData(nbt1);
				rewards.add(new WeightedReward(reward, nbt1.getInt("weight")));
			}
		}

		lootCrate = null;

		if (nbt.contains("loot_crate"))
		{
			lootCrate = new LootCrate(this);
			lootCrate.readData(nbt.getCompound("loot_crate"));
		}
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeVarInt(emptyWeight);
		buffer.writeVarInt(lootSize);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, hideTooltip);
		flags = Bits.setFlag(flags, 2, useTitle);
		flags = Bits.setFlag(flags, 4, lootCrate != null);
		buffer.writeVarInt(flags);
		buffer.writeVarInt(rewards.size());

		for (WeightedReward reward : rewards)
		{
			buffer.writeVarInt(RewardType.getRegistry().getID(reward.reward.getType()));
			reward.reward.writeNetData(buffer);
			buffer.writeVarInt(reward.weight);
		}

		if (lootCrate != null)
		{
			lootCrate.writeNetData(buffer);
		}
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		emptyWeight = buffer.readVarInt();
		lootSize = buffer.readVarInt();
		int flags = buffer.readVarInt();
		hideTooltip = Bits.getFlag(flags, 1);
		useTitle = Bits.getFlag(flags, 2);
		boolean hasCrate = Bits.getFlag(flags, 4);
		rewards.clear();
		int s = buffer.readVarInt();

		for (int i = 0; i < s; i++)
		{
			RewardType type = RewardType.getRegistry().getValue(buffer.readVarInt());
			Reward reward = type.provider.create(fakeQuest);
			reward.readNetData(buffer);
			int w = buffer.readVarInt();
			rewards.add(new WeightedReward(reward, w));
		}

		lootCrate = null;

		if (hasCrate)
		{
			lootCrate = new LootCrate(this);
			lootCrate.readNetData(buffer);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("empty_weight", emptyWeight, v -> emptyWeight = v, 0, 0, Integer.MAX_VALUE);
		config.addInt("loot_size", lootSize, v -> lootSize = v, 1, 1, Integer.MAX_VALUE);
		config.addBool("hide_tooltip", hideTooltip, v -> hideTooltip = v, false);
		config.addBool("use_title", useTitle, v -> useTitle = v, false);

		if (lootCrate != null)
		{
			lootCrate.getConfig(config.getGroup("loot_crate").setNameKey("item.ftbquests.lootcrate.name"));
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
	@OnlyIn(Dist.CLIENT)
	public void editedFromGUI()
	{
		GuiQuests gui = ClientUtils.getCurrentGuiAs(GuiQuests.class);

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
	public String getPath()
	{
		return "reward_tables/" + getCodeString(this);
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
			return rewards.get(0).reward.getTitle().getString();
		}

		return I18n.format("ftbquests.reward_table");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void onEditButtonClicked(Runnable gui)
	{
		new GuiEditRewardTable(this, () -> new MessageEditObject(this).sendToServer()).openGui();
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