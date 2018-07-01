package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.ContainerBase;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.entity.player.EntityPlayer;

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
}