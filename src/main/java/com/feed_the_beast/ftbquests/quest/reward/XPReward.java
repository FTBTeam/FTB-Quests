package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class XPReward extends QuestReward
{
	private int xp;

	public XPReward(Quest quest)
	{
		super(quest);
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setInteger("xp", xp);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		xp = nbt.getInteger("xp");

		if (xp < 1)
		{
			xp = 1;
		}
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		config.addInt("xp", () -> xp, v -> xp = v, 1, 1, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.xp"));
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		player.addExperience(xp);
	}

	@Override
	public Icon getAltIcon()
	{
		return Icon.getIcon("minecraft:items/experience_bottle");
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		ITextComponent text = new TextComponentString("+" + xp);
		text.getStyle().setColor(TextFormatting.GREEN);
		return new TextComponentTranslation("ftbquests.reward.ftbquests.xp.text", text);
	}
}