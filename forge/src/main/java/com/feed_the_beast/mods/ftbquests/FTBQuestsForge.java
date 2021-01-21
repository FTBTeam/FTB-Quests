package com.feed_the_beast.mods.ftbquests;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.mods.ftbquests.integration.gamestages.GameStagesIntegration;
import me.shedaniel.architectury.platform.Platform;
import net.minecraftforge.fml.common.Mod;

@Mod(FTBQuests.MOD_ID)
public class FTBQuestsForge {
    public FTBQuestsForge() {
        new FTBQuests();

	    if (Platform.isModLoaded("gamestages"))
	    {
		    new GameStagesIntegration().init();
	    }
    }
}
