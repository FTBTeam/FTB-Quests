package com.feed_the_beast.ftbquests.quest.loot;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/**
 * @author LatvianModder
 */
public class EntityWeight
{
	public int passive = 0;
	public int monster = 0;
	public int boss = 0;
	public Object2IntOpenHashMap<ResourceLocation> custom = new Object2IntOpenHashMap<>();

	public EntityWeight()
	{
		custom.defaultReturnValue(-1);
	}

	public int getWeight(Entity entity)
	{
		int i = custom.getInt(EntityList.getKey(entity));

		if (i >= 0)
		{
			return i;
		}
		else if (!entity.isNonBoss())
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
		nbt.setInteger("#passive", passive);
		nbt.setInteger("#monster", monster);
		nbt.setInteger("#boss", boss);

		for (Object2IntOpenHashMap.Entry<ResourceLocation> entry : custom.object2IntEntrySet())
		{
			nbt.setInteger(entry.getKey().toString(), entry.getIntValue());
		}
	}

	public void readData(NBTTagCompound nbt)
	{
		passive = nbt.getInteger("#passive");
		monster = nbt.getInteger("#monster");
		boss = nbt.getInteger("#boss");
		custom.clear();

		for (String s : nbt.getKeySet())
		{
			if (!s.startsWith("#"))
			{
				custom.put(new ResourceLocation(s), nbt.getInteger(s));
			}
		}
	}

	public void writeNetData(DataOut data)
	{
		data.writeVarInt(passive);
		data.writeVarInt(monster);
		data.writeVarInt(boss);
		data.writeVarInt(custom.size());

		for (Object2IntOpenHashMap.Entry<ResourceLocation> entry : custom.object2IntEntrySet())
		{
			data.writeResourceLocation(entry.getKey());
			data.writeVarInt(entry.getIntValue());
		}
	}

	public void readNetData(DataIn data)
	{
		passive = data.readVarInt();
		monster = data.readVarInt();
		boss = data.readVarInt();
		custom.clear();

		int s = data.readVarInt();

		for (int i = 0; i < s; i++)
		{
			ResourceLocation id = data.readResourceLocation();
			int v = data.readVarInt();
			custom.put(id, v);
		}
	}
}