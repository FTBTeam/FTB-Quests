package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class LocationTask extends Task
{
	public int dimension = 0;
	public boolean ignoreDimension = false;
	public int x = 0;
	public int y = 0;
	public int z = 0;
	public int w = 1;
	public int h = 1;
	public int d = 1;

	public LocationTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.LOCATION;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setIntArray("location", new int[] {dimension, x, y, z, w, h, d});

		if (ignoreDimension)
		{
			nbt.setBoolean("ignore_dim", true);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		int[] ai = nbt.getIntArray("location");

		if (ai.length != 7)
		{
			ai = new int[7];
		}

		dimension = ai[0];
		x = ai[1];
		y = ai[2];
		z = ai[3];
		w = ai[4];
		h = ai[5];
		d = ai[6];
		ignoreDimension = nbt.getBoolean("ignore_dim");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(dimension);
		data.writeBoolean(ignoreDimension);
		data.writeVarInt(x);
		data.writeVarInt(y);
		data.writeVarInt(z);
		data.writeVarInt(w);
		data.writeVarInt(h);
		data.writeVarInt(d);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		dimension = data.readVarInt();
		ignoreDimension = data.readBoolean();
		x = data.readVarInt();
		y = data.readVarInt();
		z = data.readVarInt();
		w = data.readVarInt();
		h = data.readVarInt();
		d = data.readVarInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("dim", () -> dimension, v -> dimension = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		config.addBool("ignore_dim", () -> ignoreDimension, v -> ignoreDimension = v, false);
		config.addInt("x", () -> x, v -> x = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		config.addInt("y", () -> y, v -> y = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		config.addInt("z", () -> z, v -> z = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		config.addInt("w", () -> w, v -> w = v, 1, 1, Integer.MAX_VALUE);
		config.addInt("h", () -> h, v -> h = v, 1, 1, Integer.MAX_VALUE);
		config.addInt("d", () -> d, v -> d = v, 1, 1, Integer.MAX_VALUE);
	}

	@Override
	public boolean autoSubmitOnPlayerTick()
	{
		return true;
	}

	@Override
	public TaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<LocationTask>
	{
		private Data(LocationTask task, QuestData data)
		{
			super(task, data);
		}

		@Override
		public String getProgressString()
		{
			return progress > 0 ? "1" : "0";
		}

		@Override
		public boolean canSubmit(EntityPlayerMP player)
		{
			if (task.ignoreDimension || task.dimension == player.dimension)
			{
				int y = MathHelper.floor(player.posY);

				if (y >= task.y && y < task.y + task.h)
				{
					int x = MathHelper.floor(player.posX);

					if (x >= task.x && x < task.x + task.w)
					{
						int z = MathHelper.floor(player.posZ);
						return z >= task.z && z < task.z + task.d;
					}
				}
			}

			return false;
		}
	}
}