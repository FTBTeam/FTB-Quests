package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.gui.FTBQuestsGuiHandler;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public class FTBQuestsCommon
{
	public static final String PERM_RESET_PROGRESS = "admin_panel.ftbquests.reset_progress";

	public void preInit()
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(FTBQuests.MOD, new FTBQuestsGuiHandler());
		FTBQuestsConfig.sync();
		FTBQuestsNetHandler.init();
	}

	public void postInit()
	{
		PermissionAPI.registerNode(PERM_RESET_PROGRESS, DefaultPermissionLevel.OP, "Permission for resetting quest progress");
	}
}