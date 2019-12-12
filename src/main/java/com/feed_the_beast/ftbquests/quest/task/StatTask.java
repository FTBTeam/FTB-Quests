package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class StatTask extends Task
{
	public Stat<ResourceLocation> stat;
	public int value = 1;

	public StatTask(Quest quest)
	{
		super(quest);
		stat = Stats.CUSTOM.get(Stats.MOB_KILLS);
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.STAT;
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
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putString("stat", stat.toString());
		nbt.putInt("value", value);
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		stat = Stats.CUSTOM.get(new ResourceLocation(nbt.getString("stat")));
		value = nbt.getInt("value");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeResourceLocation(stat.getValue());
		buffer.writeVarInt(value);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		stat = Stats.CUSTOM.get(buffer.readResourceLocation());
		value = buffer.readVarInt();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);

		List<Stat<ResourceLocation>> list = new ArrayList<>();
		Stats.CUSTOM.iterator().forEachRemaining(list::add);
		config.addEnum("stat", stat, v -> stat = v, NameMap.of(Stats.CUSTOM.get(Stats.MOB_KILLS), list).name(v -> new TranslationTextComponent("stat." + v.getValue().getNamespace() + "." + v.getValue().getPath())).create());
		config.addInt("value", value, v -> value = v, 1, 1, Integer.MAX_VALUE);
	}

	@Override
	public String getAltTitle()
	{
		return stat.getName();
	}

	@Override
	public boolean consumesResources()
	{
		return true;
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

	public static class Data extends TaskData<StatTask>
	{
		private Data(StatTask task, PlayerData data)
		{
			super(task, data);
		}

		@Override
		public String getProgressString()
		{
			return Integer.toString((int) progress);
		}

		@Override
		public void submitTask(ServerPlayerEntity player, ItemStack item)
		{
			if (isComplete())
			{
				return;
			}

			int set = Math.min(task.value, player.getStats().getValue(task.stat));

			if (set > progress)
			{
				setProgress(set);
			}
		}
	}
}