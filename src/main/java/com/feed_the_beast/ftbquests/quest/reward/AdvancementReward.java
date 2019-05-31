package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class AdvancementReward extends QuestReward
{
	public String advancement;
	public String criterion;

	public AdvancementReward(Quest quest)
	{
		super(quest);
		advancement = "";
		criterion = "";
	}

	@Override
	public QuestRewardType getType()
	{
		return FTBQuestsRewards.ADVANCEMENT;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setString("advancement", advancement);
		nbt.setString("criterion", criterion);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		advancement = nbt.getString("advancement");
		criterion = nbt.getString("criterion");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeString(advancement);
		data.writeString(criterion);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		advancement = data.readString();
		criterion = data.readString();
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("advancement", () -> advancement, v -> advancement = v, "").setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.advancement"));
		config.addString("criterion", () -> criterion, v -> criterion = v, "");
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		Advancement a = FTBQuests.PROXY.getAdvancement(player.server, advancement);

		if (a != null)
		{
			if (criterion.isEmpty())
			{
				for (String s : a.getCriteria().keySet())
				{
					player.getAdvancements().grantCriterion(a, s);
				}
			}
			else
			{
				player.getAdvancements().grantCriterion(a, criterion);
			}
		}
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		Advancement a = FTBQuests.PROXY.getAdvancement(null, advancement);

		if (a != null && a.getDisplay() != null)
		{
			ITextComponent text = a.getDisplay().getTitle().createCopy();
			text.getStyle().setColor(TextFormatting.YELLOW);
			return new TextComponentTranslation("ftbquests.reward.ftbquests.advancement").appendText(": ").appendSibling(text);
		}

		return super.getAltDisplayName();
	}

	@Override
	public Icon getAltIcon()
	{
		Advancement a = FTBQuests.PROXY.getAdvancement(null, advancement);

		if (a != null && a.getDisplay() != null)
		{
			return ItemIcon.getItemIcon(a.getDisplay().getIcon());
		}

		return super.getAltIcon();
	}
}