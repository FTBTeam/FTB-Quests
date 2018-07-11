package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class UnknownReward extends QuestReward
{
	private final NBTTagCompound nbt;

	public UnknownReward(Quest parent, int id, NBTTagCompound n)
	{
		super(parent, id);
		nbt = n;
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
	}

	@Override
	public Icon getIcon()
	{
		return GuiIcons.CANCEL;
	}

	@Override
	public void writeData(NBTTagCompound n)
	{
		NBTUtils.copyTags(nbt, n);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return "Unkown | " + nbt;
	}
}