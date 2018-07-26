package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNBT;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class UnknownReward extends QuestReward
{
	public static final String ID = "unknown";
	private ConfigNBT nbt;
	private String hover;

	public UnknownReward(Quest parent, NBTTagCompound n)
	{
		super(parent, n);
		nbt = new ConfigNBT(n);
	}

	@Override
	public boolean isInvalid()
	{
		return true;
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound n)
	{
		NBTUtils.copyTags(nbt.getNBT(), n);
	}

	@Override
	public Icon getIcon()
	{
		return GuiIcons.BARRIER;
	}

	public String getHover()
	{
		if (hover == null)
		{
			hover = NBTUtils.getColoredNBTString(nbt.getNBT());
		}

		return hover;
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("nbt", nbt, new ConfigNBT(new NBTTagCompound()));
	}
}