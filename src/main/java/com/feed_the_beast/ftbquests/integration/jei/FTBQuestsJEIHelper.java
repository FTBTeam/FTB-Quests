package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import net.minecraftforge.fml.common.Loader;

/**
 * @author LatvianModder
 */
public class FTBQuestsJEIHelper
{
	public static void refresh(QuestObjectBase object)
	{
		if (object.refreshJEI() && Loader.isModLoaded("jei"))
		{
			reload0();
		}
	}

	private static void reload0()
	{
		FTBQuestsJEIIntegration.refresh();
	}
}