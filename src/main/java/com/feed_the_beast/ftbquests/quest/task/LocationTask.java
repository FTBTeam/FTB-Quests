package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public class LocationTask extends Task
{
	public ResourceLocation dimension;
	public boolean ignoreDimension;
	public int x, y, z;
	public int w, h, d;

	public LocationTask(Quest quest)
	{
		super(quest);
		dimension = World.field_234918_g_.getRegistryName();
		ignoreDimension = false;
		x = 0;
		y = 0;
		z = 0;
		w = 1;
		h = 1;
		d = 1;
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.LOCATION;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putString("dimension", dimension.toString());
		nbt.putBoolean("ignore_dimension", ignoreDimension);
		nbt.putIntArray("position", new int[] {x, y, z});
		nbt.putIntArray("size", new int[] {w, h, d});
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		dimension = new ResourceLocation(nbt.getString("dimension"));
		ignoreDimension = nbt.getBoolean("ignore_dimension");

		int[] pos = nbt.getIntArray("position");

		if (pos.length == 3)
		{
			x = pos[0];
			y = pos[1];
			z = pos[2];
		}

		int[] size = nbt.getIntArray("size");

		if (pos.length == 3)
		{
			w = size[0];
			h = size[1];
			d = size[2];
		}
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeResourceLocation(dimension);
		buffer.writeBoolean(ignoreDimension);
		buffer.writeVarInt(x);
		buffer.writeVarInt(y);
		buffer.writeVarInt(z);
		buffer.writeVarInt(w);
		buffer.writeVarInt(h);
		buffer.writeVarInt(d);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		dimension = buffer.readResourceLocation();
		ignoreDimension = buffer.readBoolean();
		x = buffer.readVarInt();
		y = buffer.readVarInt();
		z = buffer.readVarInt();
		w = buffer.readVarInt();
		h = buffer.readVarInt();
		d = buffer.readVarInt();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("dim", dimension.toString(), v -> dimension = new ResourceLocation(v), "minecraft:overworld");
		config.addBool("ignore_dim", ignoreDimension, v -> ignoreDimension = v, false);
		config.addInt("x", x, v -> x = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		config.addInt("y", y, v -> y = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		config.addInt("z", z, v -> z = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		config.addInt("w", w, v -> w = v, 1, 1, Integer.MAX_VALUE);
		config.addInt("h", h, v -> h = v, 1, 1, Integer.MAX_VALUE);
		config.addInt("d", d, v -> d = v, 1, 1, Integer.MAX_VALUE);
	}

	@Override
	public int autoSubmitOnPlayerTick()
	{
		return 3;
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<LocationTask>
	{
		private Data(LocationTask task, PlayerData data)
		{
			super(task, data);
		}

		@Override
		public String getProgressString()
		{
			return progress > 0 ? "1" : "0";
		}

		@Override
		public boolean canSubmit(ServerPlayerEntity player)
		{
			if (task.ignoreDimension || player.world.func_234923_W_().getRegistryName().equals(task.dimension))
			{
				int y = MathHelper.floor(player.getPosX());

				if (y >= task.y && y < task.y + task.h)
				{
					int x = MathHelper.floor(player.getPosX());

					if (x >= task.x && x < task.x + task.w)
					{
						int z = MathHelper.floor(player.getPosZ());
						return z >= task.z && z < task.z + task.d;
					}
				}
			}

			return false;
		}
	}
}