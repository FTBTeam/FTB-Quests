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
import noppes.npcs.controllers.data.PlayerData;

/**
 * @author LatvianModder
 */
public class NPCDialogTask extends Task
{
	public int npcDialog = 0;

	public NPCDialogTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return CustomNPCsIntegration.DIALOG_TASK;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setInteger("npc_dialog", npcDialog);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		npcDialog = nbt.getInteger("npc_dialog");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(npcDialog);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		npcDialog = data.readVarInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("id", () -> npcDialog, v -> npcDialog = v, 0, 0, Integer.MAX_VALUE);
	}

	@Override
	public TaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<NPCDialogTask>
	{
		private Data(NPCDialogTask task, QuestData data)
		{
			super(task, data);
		}

		@Override
		public boolean canSubmit(EntityPlayerMP player)
		{
			PlayerData data = task.npcDialog > 0 ? PlayerData.get(player) : null;
			return data != null && data.dialogData.dialogsRead.contains(task.npcDialog);
		}
	}
}