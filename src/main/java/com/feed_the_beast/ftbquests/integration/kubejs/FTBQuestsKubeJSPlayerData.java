package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageSyncEditingMode;
import dev.latvian.kubejs.documentation.DocClass;
import dev.latvian.kubejs.documentation.DocMethod;
import dev.latvian.kubejs.documentation.Param;
import dev.latvian.kubejs.player.PlayerDataJS;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
@DocClass(displayName = "FTB Quests Player Data")
public class FTBQuestsKubeJSPlayerData
{
	private final PlayerDataJS playerData;

	public FTBQuestsKubeJSPlayerData(PlayerDataJS p)
	{
		playerData = p;
	}

	@DocMethod("Returns true if player is in editing mode")
	public boolean canEdit()
	{
		EntityPlayerMP p = playerData.getPlayerEntity();
		return p != null && FTBQuests.canEdit(p);
	}

	@DocMethod(value = "Sets editing mode for player", params = @Param("canEdit"))
	public void setCanEdit(boolean canEdit)
	{
		EntityPlayerMP p = playerData.getPlayerEntity();

		if (p != null)
		{
			NBTUtils.getPersistedData(p, canEdit).setBoolean("ftbquests_editing_mode", canEdit);
			new MessageSyncEditingMode(canEdit).sendTo(p);
		}
	}
}