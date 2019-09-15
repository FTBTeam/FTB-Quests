package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageSyncEditingMode;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.task.Task;
import dev.latvian.kubejs.documentation.DisplayName;
import dev.latvian.kubejs.documentation.Info;
import dev.latvian.kubejs.documentation.P;
import dev.latvian.kubejs.player.PlayerDataJS;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
@DisplayName("FTB Quests Player Data")
public class FTBQuestsKubeJSPlayerData
{
	private final PlayerDataJS playerData;

	public FTBQuestsKubeJSPlayerData(PlayerDataJS p)
	{
		playerData = p;
	}

	@Info("Returns true if player is in editing mode")
	public boolean getCanEdit()
	{
		EntityPlayer p = playerData.getPlayerEntity();
		return p != null && FTBQuests.canEdit(p);
	}

	@Info("Sets editing mode for player")
	public void setCanEdit(@P("canEdit") boolean canEdit)
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

	@Nullable
	public QuestFile getFile()
	{
		return FTBQuests.PROXY.getQuestFile(playerData.getOverworld().world);
	}

	@Nullable
	public QuestData getData()
	{
		QuestFile file = getFile();
		EntityPlayer player = playerData.getPlayerEntity();
		return file == null || player == null ? null : file.getData(player);
	}

	public void addProgress(@P("id") Object id, @P("progress") long progress)
	{
		QuestData data = getData();

		if (data == null)
		{
			return;
		}

		Task task = data.getFile().getTask(data.getFile().getID(id));

		if (task != null)
		{
			data.getTaskData(task).addProgress(progress);
		}
	}

	public void complete(@P("id") Object id)
	{
		QuestData data = getData();

		if (data == null)
		{
			return;
		}

		QuestObject object = data.getFile().get(data.getFile().getID(id));

		if (object != null)
		{
			object.forceProgress(data, ChangeProgress.COMPLETE, false);
		}
	}

	public void reset(@P("id") Object id)
	{
		QuestData data = getData();

		if (data == null)
		{
			return;
		}

		QuestObject object = data.getFile().get(data.getFile().getID(id));

		if (object != null)
		{
			object.forceProgress(data, ChangeProgress.RESET, false);
		}
	}

	public boolean isCompleted(@P("id") Object id)
	{
		QuestData data = getData();

		if (data == null)
		{
			return false;
		}

		QuestObject object = data.getFile().get(data.getFile().getID(id));
		return object != null && object.isComplete(data);
	}

	public boolean isStarted(@P("id") Object id)
	{
		QuestData data = getData();

		if (data == null)
		{
			return false;
		}

		QuestObject object = data.getFile().get(data.getFile().getID(id));
		return object != null && object.isStarted(data);
	}

	public boolean canStartQuest(@P("id") Object id)
	{
		QuestData data = getData();

		if (data == null)
		{
			return false;
		}

		Quest quest = data.getFile().getQuest(data.getFile().getID(id));
		return quest != null && quest.canStartTasks(data);
	}

	public int getProgress(@P("id") Object id)
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