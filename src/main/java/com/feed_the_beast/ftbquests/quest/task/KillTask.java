package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class KillTask extends QuestTask
{
	public EntityList.EntityEggInfo entity = EntityList.ENTITY_EGGS.get(new ResourceLocation("minecraft:zombie"));
	public int value = 100;

	public static StatBase get(String id)
	{
		for (StatBase base : StatList.BASIC_STATS)
		{
			if (base.statId.equals(id))
			{
				return base;
			}
		}

		return StatList.MOB_KILLS;
	}

	public KillTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestTaskType getType()
	{
		return FTBQuestsTasks.KILL;
	}

	@Override
	public long getMaxProgress()
	{
		return value;
	}

	@Override
	public String getMaxProgressString()
	{
		return Integer.toString(value);
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setString("entity", entity.spawnedID.toString());
		nbt.setInteger("value", value);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		entity = EntityList.ENTITY_EGGS.get(new ResourceLocation(nbt.getString("entity")));

		if (entity == null)
		{
			entity = EntityList.ENTITY_EGGS.get(new ResourceLocation("minecraft:zombie"));
		}

		value = nbt.getInteger("value");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeString(entity.spawnedID.toString());
		data.writeVarInt(value);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		entity = EntityList.ENTITY_EGGS.get(new ResourceLocation(data.readString()));

		if (entity == null)
		{
			entity = EntityList.ENTITY_EGGS.get(new ResourceLocation("minecraft:zombie"));
		}

		value = data.readVarInt();
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addEnum("entity", () -> entity, v -> entity = v, NameMap.create(EntityList.ENTITY_EGGS.get(new ResourceLocation("minecraft:zombie")), NameMap.ObjectProperties.withName((sender, s) -> s.killEntityStat.getStatName()), EntityList.ENTITY_EGGS.values().toArray(new EntityList.EntityEggInfo[0])));
		config.addInt("value", () -> value, v -> value = v, 1, 1, Integer.MAX_VALUE);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return StringUtils.color(entity.killEntityStat.getStatName(), null);
	}

	@Override
	public QuestTaskData createData(ITeamData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<KillTask>
	{
		private Data(KillTask task, ITeamData data)
		{
			super(task, data);
		}

		@Override
		public String getProgressString()
		{
			return Integer.toString((int) progress);
		}

		@Override
		public boolean submitTask(EntityPlayerMP player, Collection<ItemStack> itemsToCheck, boolean simulate)
		{
			if (progress >= task.value)
			{
				return false;
			}

			int set = Math.min(task.value, player.getStatFile().readStat(task.entity.killEntityStat));

			if (set > progress)
			{
				if (!simulate)
				{
					progress = set;
					sync();
				}

				return true;
			}

			return false;
		}
	}
}