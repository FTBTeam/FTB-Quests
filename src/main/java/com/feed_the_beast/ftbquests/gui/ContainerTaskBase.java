package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.ContainerBase;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class ContainerTaskBase extends ContainerBase
{
	public final QuestTaskData data;

	public ContainerTaskBase(EntityPlayer player, QuestTaskData d)
	{
		super(player);
		data = d;
		addTaskSlots();
		addPlayerSlots(8, 84);
	}

	public void addTaskSlots()
	{
	}

	@Override
	public int getNonPlayerSlots()
	{
		return 0;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return !data.task.isInvalid();
	}

	public Icon getEmptySlotIcon(int slot)
	{
		return Icon.EMPTY;
	}

	@SideOnly(Side.CLIENT)
	public String getEmptySlotText(int slot)
	{
		return "";
	}
}