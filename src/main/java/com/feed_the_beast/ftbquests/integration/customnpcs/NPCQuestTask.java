package com.feed_the_beast.ftbquests.integration.customnpcs;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.task.BooleanTaskData;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.controllers.PlayerQuestController;

/**
 * @author LatvianModder
 */
public class NPCQuestTask extends Task
{
	public int npcQuestID = 0;

	public NPCQuestTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return CustomNPCsIntegration.QUEST_TASK;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setInteger("npc_quest", npcQuestID);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		npcQuestID = nbt.getInteger("npc_quest");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(npcQuestID);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		npcQuestID = data.readVarInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("id", () -> npcQuestID, v -> npcQuestID = v, 0, 0, Integer.MAX_VALUE);
	}

	@Override
	public int autoSubmitOnPlayerTick()
	{
		return 20;
	}

	@Override
	public TaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<NPCQuestTask>
	{
		private Data(NPCQuestTask task, QuestData data)
		{
			super(task, data);
		}

		@Override
		public boolean canSubmit(EntityPlayerMP player)
		{
			return task.npcQuestID > 0 && PlayerQuestController.isQuestActive(player, task.npcQuestID);
		}
	}
}