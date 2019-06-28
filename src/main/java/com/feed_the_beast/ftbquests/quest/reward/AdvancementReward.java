package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class AdvancementReward extends Reward
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
	public RewardType getType()
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
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("advancement", () -> advancement, v -> advancement = v, "").setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.advancement"));
		config.addString("criterion", () -> criterion, v -> criterion = v, "");
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		Advancement a = player.server.getAdvancementManager().getAdvancement(new ResourceLocation(advancement));

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
	public String getAltTitle()
	{
		Advancement a = Minecraft.getMinecraft().player.connection.getAdvancementManager().getAdvancementList().getAdvancement(new ResourceLocation(advancement));

		if (a != null && a.getDisplay() != null)
		{
			return I18n.format("ftbquests.reward.ftbquests.advancement") + ": " + TextFormatting.YELLOW + a.getDisplay().getTitle().getFormattedText();
		}

		return super.getAltTitle();
	}

	@Override
	public Icon getAltIcon()
	{
		Advancement a = Minecraft.getMinecraft().player.connection.getAdvancementManager().getAdvancementList().getAdvancement(new ResourceLocation(advancement));

		if (a != null && a.getDisplay() != null)
		{
			return ItemIcon.getItemIcon(a.getDisplay().getIcon());
		}

		return super.getAltIcon();
	}
}