package com.feed_the_beast.ftbquests.quest.loot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

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

	public void writeData(CompoundNBT nbt)
	{
		nbt.putInt("passive", passive);
		nbt.putInt("monster", monster);
		nbt.putInt("boss", boss);
	}

	public void readData(CompoundNBT nbt)
	{
		passive = nbt.getInt("passive");
		monster = nbt.getInt("monster");
		boss = nbt.getInt("boss");
	}

	public void writeNetData(PacketBuffer data)
	{
		data.writeVarInt(passive);
		data.writeVarInt(monster);
		data.writeVarInt(boss);
	}

	public void readNetData(PacketBuffer data)
	{
		passive = data.readVarInt();
		monster = data.readVarInt();
		boss = data.readVarInt();
	}
}