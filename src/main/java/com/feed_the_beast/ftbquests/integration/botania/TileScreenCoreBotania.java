package com.feed_the_beast.ftbquests.integration.botania;

import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import vazkii.botania.api.mana.IManaReceiver;

/**
 * @author LatvianModder
 */
public class TileScreenCoreBotania extends TileScreenCore implements IManaReceiver
{
	@Override
	public boolean isFull()
	{
		QuestTaskData d = getTaskData();

		if (d instanceof IManaReceiver && d.task.quest.canStartTasks(d.teamData))
		{
			return ((IManaReceiver) d).isFull();
		}

		return true;
	}

	@Override
	public void recieveMana(int mana)
	{
		QuestTaskData d = getTaskData();

		if (d instanceof IManaReceiver && d.task.quest.canStartTasks(d.teamData))
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