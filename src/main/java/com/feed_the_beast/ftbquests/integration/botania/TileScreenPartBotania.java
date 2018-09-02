package com.feed_the_beast.ftbquests.integration.botania;

import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import com.feed_the_beast.ftbquests.tile.TileScreenPart;
import vazkii.botania.api.mana.IManaReceiver;

/**
 * @author LatvianModder
 */
public class TileScreenPartBotania extends TileScreenPart implements IManaReceiver
{
	@Override
	public boolean isFull()
	{
		TileScreenCore screen = getScreen();
		return !(screen instanceof TileScreenCoreBotania) || ((TileScreenCoreBotania) screen).isFull();
	}

	@Override
	public void recieveMana(int mana)
	{
		TileScreenCore screen = getScreen();

		if (screen instanceof TileScreenCoreBotania)
		{
			((TileScreenCoreBotania) screen).recieveMana(mana);
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