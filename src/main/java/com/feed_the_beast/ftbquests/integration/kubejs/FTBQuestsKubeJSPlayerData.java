package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageSyncEditingMode;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.task.Task;
import dev.latvian.kubejs.documentation.DocClass;
import dev.latvian.kubejs.documentation.DocMethod;
import dev.latvian.kubejs.documentation.Param;
import dev.latvian.kubejs.player.PlayerDataJS;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;

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
	@Nullable
	public QuestFile getFile()
	{
		return FTBQuests.PROXY.getQuestFile(playerData.getOverworld().world);
	}

	@DocMethod
	@Nullable
	public QuestData getData()
	{
		QuestFile file = getFile();
		EntityPlayer player = playerData.getPlayerEntity();
		return file == null || player == null ? null : file.getData(player);
	}

	@DocMethod(params = {@Param("id"), @Param("progress")})
	public void addProgress(Object id, long progress)
	{
		QuestData data = getData();

		if (data == null)
		{
			return;
		}

		Task task = data.getFile().getTask(data.getFile().getID(String.valueOf(id)));

		if (task != null)
		{
			data.getTaskData(task).addProgress(progress);
		}
	}

	@DocMethod(params = @Param("id"))
	public boolean isCompleted(Object id)
	{
		QuestData data = getData();

		if (data == null)
		{
			return false;
		}

		QuestObject object = data.getFile().get(data.getFile().getID(id));
		return object != null && object.isComplete(data);
	}

	@DocMethod(params = @Param("id"))
	public boolean isStarted(Object id)
	{
		QuestData data = getData();

		if (data == null)
		{
			return false;
		}

		QuestObject object = data.getFile().get(data.getFile().getID(id));
		return object != null && object.isStarted(data);
	}

	@DocMethod(params = @Param("id"))
	public boolean canStartQuest(Object id)
	{
		QuestData data = getData();

		if (data == null)
		{
			return false;
		}

		Quest quest = data.getFile().getQuest(data.getFile().getID(id));
		return quest != null && quest.canStartTasks(data);
	}

	@DocMethod(params = @Param("id"))
	public int getProgress(Object id)
	{
		QuestData data = getData();

		if (data == null)
		{
			return 0;
		}

		QuestObject object = data.getFile().get(data.getFile().getID(id));
		return object != null ? object.getRelativeProgress(data) : 0;
	}
}