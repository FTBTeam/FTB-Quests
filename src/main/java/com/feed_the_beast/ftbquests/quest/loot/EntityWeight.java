package com.feed_the_beast.ftbquests.quest.loot;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class EntityWeight
{
	public int passive = 0;
	public int monster = 0;
	public int boss = 0;

	public int getWeight(Entity entity)
	{
		if (!entity.isNonBoss())
		{
			return boss;
		}
		else if (entity instanceof IMob)
		{
			return monster;
		}

		return passive;
	}

	public void writeData(NBTTagCompound nbt)
	{
		nbt.setInteger("passive", passive);
		nbt.setInteger("monster", monster);
		nbt.setInteger("boss", boss);
	}

	public void readData(NBTTagCompound nbt)
	{
		passive = nbt.getInteger("passive");

		if (passive == 0)
		{
			passive = nbt.getInteger("#passive");
		}

		monster = nbt.getInteger("monster");

		if (monster == 0)
		{
			monster = nbt.getInteger("#monster");
		}

		boss = nbt.getInteger("boss");

		if (boss == 0)
		{
			boss = nbt.getInteger("#boss");
		}
	}

	public void writeNetData(DataOut data)
	{
		data.writeVarInt(passive);
		data.writeVarInt(monster);
		data.writeVarInt(boss);
	}

	public void readNetData(DataIn data)
	{
		passive = data.readVarInt();
		monster = data.readVarInt();
		boss = data.readVarInt();
	}
}