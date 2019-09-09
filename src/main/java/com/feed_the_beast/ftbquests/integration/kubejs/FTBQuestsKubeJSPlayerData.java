package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageSyncEditingMode;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.task.Task;
import dev.latvian.kubejs.documentation.DocClass;
import dev.latvian.kubejs.documentation.DocMethod;
import dev.latvian.kubejs.documentation.Param;
import dev.latvian.kubejs.player.PlayerDataJS;
import net.minecraft.entity.player.EntityPlayer;
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
	public boolean getCanEdit()
	{
		EntityPlayer p = playerData.getPlayerEntity();
		return p != null && FTBQuests.canEdit(p);
	}

	@DocMethod(value = "Sets editing mode for player", params = @Param("canEdit"))
	public void setCanEdit(boolean canEdit)
	{
		EntityPlayer p = playerData.getPlayerEntity();

		if (p != null)
		{
			NBTUtils.getPersistedData(p, canEdit).setBoolean("ftbquests_editing_mode", canEdit);

			if (p instanceof EntityPlayerMP)
			{
				new MessageSyncEditingMode(canEdit).sendTo((EntityPlayerMP) p);
			}
		}
	}

	@DocMethod
	public QuestFile getFile()
	{
		return FTBQuests.PROXY.getQuestFile(playerData.getOverworld().world);
	}

	@DocMethod
	public QuestData getData()
	{
		return getFile().getData(playerData.getPlayerEntity());
	}

	@DocMethod(params = {@Param("id"), @Param("progress")})
	public void addProgress(Object id, long progress)
	{
		QuestFile file = getFile();
		Task task = file.getTask(file.getID(String.valueOf(id)));

		if (task != null)
		{
			file.getData(playerData.getPlayerEntity()).getTaskData(task).addProgress(progress);
		}
	}
}