package com.feed_the_beast.ftbquests.integration.botania;

import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenPart;
import vazkii.botania.api.mana.IManaReceiver;

/**
 * @author LatvianModder
 */
public class TileTaskScreenPartBotania extends TileTaskScreenPart implements IManaReceiver
{
	@Override
	public boolean isFull()
	{
		TileTaskScreenCore screen = getScreen();
		return !(screen instanceof TileTaskScreenCoreBotania) || ((TileTaskScreenCoreBotania) screen).isFull();
	}

	@Override
	public void recieveMana(int mana)
	{
		TileTaskScreenCore screen = getScreen();

		if (screen instanceof TileTaskScreenCoreBotania)
		{
			((TileTaskScreenCoreBotania) screen).recieveMana(mana);
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