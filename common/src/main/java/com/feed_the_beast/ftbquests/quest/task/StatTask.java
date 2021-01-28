package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class StatTask extends Task
{
	public ResourceLocation stat;
	public int value = 1;

	public StatTask(Quest quest)
	{
		super(quest);
		stat = Stats.MOB_KILLS;
	}

	@Override
	public TaskType getType()
	{
		return TaskTypes.STAT;
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
	public void writeData(CompoundTag nbt)
	{
		super.writeData(nbt);
		nbt.putString("stat", stat.toString());
		nbt.putInt("value", value);
	}

	@Override
	public void readData(CompoundTag nbt)
	{
		super.readData(nbt);
		stat = new ResourceLocation(nbt.getString("stat"));
		value = nbt.getInt("value");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer)
	{
		super.writeNetData(buffer);
		buffer.writeResourceLocation(stat);
		buffer.writeVarInt(value);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer)
	{
		super.readNetData(buffer);
		stat = buffer.readResourceLocation();
		value = buffer.readVarInt();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);

		List<ResourceLocation> list = new ArrayList<>();
		Stats.CUSTOM.iterator().forEachRemaining(s -> list.add(s.getValue()));
		config.addEnum("stat", stat, v -> stat = v, NameMap.of(Stats.MOB_KILLS, list).name(v -> new TranslatableComponent("stat." + v.getNamespace() + "." + v.getPath())).create());
		config.addInt("value", value, v -> value = v, 1, 1, Integer.MAX_VALUE);
	}

	@Override
	public MutableComponent getAltTitle()
	{
		return new TranslatableComponent("stat." + stat.getNamespace() + "." + stat.getPath());
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
		public void submitTask(ServerPlayer player, ItemStack item)
		{
			if (isComplete())
			{
				return;
			}

			int set = Math.min(task.value, player.getStats().getValue(Stats.CUSTOM.get(task.stat)));

			if (set > progress)
			{
				setProgress(set);
			}
		}
	}
}