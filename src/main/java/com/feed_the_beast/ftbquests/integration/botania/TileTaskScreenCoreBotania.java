package com.feed_the_beast.ftbquests.integration.botania;

import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import vazkii.botania.api.mana.IManaReceiver;

/**
 * @author LatvianModder
 */
public class TileTaskScreenCoreBotania extends TileTaskScreenCore implements IManaReceiver
{
	@Override
	public boolean isFull()
	{
		TaskData d = getTaskData();

		if (d instanceof IManaReceiver && d.task.quest.canStartTasks(d.data))
		{
			return ((IManaReceiver) d).isFull();
		}

		return true;
	}

	@Override
	public void recieveMana(int mana)
	{
		TaskData d = getTaskData();

		if (d instanceof IManaReceiver && d.task.quest.canStartTasks(d.data))
		{
			((IManaReceiver) d).recieveMana(mana);
		}
	}

	@Override
	public boolean canRecieveManaFromBursts()
	{
		return !isFull();
	}

	@Override
	public int getCurrentMana()
	{
		return 0;
	}
}