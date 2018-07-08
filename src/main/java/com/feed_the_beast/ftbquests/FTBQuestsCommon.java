package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;

import javax.annotation.Nullable;

public class FTBQuestsCommon
{
	@Nullable
	public IProgressData getOwner(String owner, boolean clientSide)
	{
		if (clientSide || !Universe.loaded())
		{
			return null;
		}

		ForgeTeam team = Universe.get().getTeam(owner);
		return team.isValid() ? FTBQuestsTeamData.get(team) : null;
	}
}