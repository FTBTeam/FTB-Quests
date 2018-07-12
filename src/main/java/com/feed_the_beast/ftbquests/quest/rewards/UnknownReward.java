package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class UnknownReward extends QuestReward
{
	public static final String ID = "unknown";
	private final NBTTagCompound nbt;
	private String hover;

	public UnknownReward(Quest parent, int id, NBTTagCompound n)
	{
		super(parent, id);
		nbt = n;
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
		NBTUtils.copyTags(nbt, n);
	}

	@Override
	public Icon getIcon()
	{
		return GuiIcons.CANCEL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return I18n.format("ftbquests.gui.reward.unknown");
	}

	public String getHover()
	{
		if (hover == null)
		{
			hover = NBTUtils.getColoredNBTString(nbt);
		}

		return hover;
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
	}
}