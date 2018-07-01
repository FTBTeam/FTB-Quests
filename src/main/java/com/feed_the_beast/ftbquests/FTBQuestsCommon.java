package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

import javax.annotation.Nullable;

public class FTBQuestsCommon
{
	public static final String PERM_EDIT = "admin_panel.ftbquests.edit";
	public static final String PERM_RESET_PROGRESS = "admin_panel.ftbquests.reset_progress";

	public void preInit()
	{
		FTBQuestsConfig.sync();
		FTBQuestsNetHandler.init();
	}

	public void postInit()
	{
		PermissionAPI.registerNode(PERM_EDIT, DefaultPermissionLevel.OP, "Permission for editing quests");
		PermissionAPI.registerNode(PERM_RESET_PROGRESS, DefaultPermissionLevel.OP, "Permission for resetting quest progress");
	}

	@Nullable
	public IProgressData getOwner(String owner, boolean clientSide)
	{
		if (clientSide)
		{
			return null;
		}

		ForgeTeam team = Universe.get().getTeam(owner);
		return team.isValid() ? FTBQuestsTeamData.get(team) : null;
	}
}