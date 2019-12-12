package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public class ObservationTask extends Task
{
	@FunctionalInterface
	public interface Check
	{
		boolean check(PlayerEntity player, RayTraceResult lookingAt);
	}

	public final Check matcher;
	public long ticks;

	public ObservationTask(Quest quest)
	{
		super(quest);
		matcher = (player, ray) -> false;
		ticks = 0L;
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.OBSERVATION;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putLong("ticks", ticks);
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		ticks = nbt.getLong("ticks");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeVarLong(ticks);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		ticks = buffer.readVarLong();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addLong("ticks", ticks, v -> ticks = v, 0L, 0L, 1200L);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void onButtonClicked(Button button, boolean canClick)
	{
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new BooleanTaskData<>(this, data);
	}
}