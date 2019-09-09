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
import com.feed_the_beast.ftbquests.util.NumberMode;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.controllers.data.PlayerData;

/**
 * @author LatvianModder
 */
public class NPCFactionTask extends Task
{
	public int npcFaction = 0;
	public int requiredPoints = 1;
	public NumberMode mode = NumberMode.GREATER_THAN_OR_EQUAL;

	public NPCFactionTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return CustomNPCsIntegration.FACTION_TASK;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setInteger("npc_faction", npcFaction);
		nbt.setInteger("required_points", requiredPoints);
		nbt.setString("point_mode", mode.getId());
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		npcFaction = nbt.getInteger("npc_faction");
		requiredPoints = nbt.getInteger("required_points");
		mode = NumberMode.NAME_MAP.get(nbt.getString("mode"));
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(npcFaction);
		data.writeVarInt(requiredPoints);
		NumberMode.NAME_MAP.write(data, mode);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		npcFaction = data.readVarInt();
		requiredPoints = data.readVarInt();
		mode = NumberMode.NAME_MAP.read(data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("id", () -> npcFaction, v -> npcFaction = v, 0, 0, Integer.MAX_VALUE);
		config.addInt("required_points", () -> requiredPoints, v -> requiredPoints = v, 1, 0, Integer.MAX_VALUE);
		config.addEnum("mode", () -> mode, v -> mode = v, NumberMode.NAME_MAP);
	}

	@Override
	public TaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<NPCFactionTask>
	{
		private Data(NPCFactionTask task, QuestData data)
		{
			super(task, data);
		}

		@Override
		public boolean canSubmit(EntityPlayerMP player)
		{
			PlayerData data = task.npcFaction > 0 ? PlayerData.get(player) : null;

			if (data != null && data.factionData.factionData.containsKey(task.npcFaction))
			{
				return task.mode.check(data.factionData.getFactionPoints(player, task.npcFaction), task.requiredPoints);
			}

			return false;
		}
	}
}