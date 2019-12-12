package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public abstract class EnergyTask extends Task implements ISingleLongValueTask
{
	public long value = 1000L;
	public long maxInput = 0L;

	public EnergyTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public long getMaxProgress()
	{
		return value;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putLong("value", value);

		if (maxInput > 0L)
		{
			nbt.putLong("max_input", maxInput);
		}
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		value = nbt.getLong("value");

		if (value < 1L)
		{
			value = 1L;
		}

		maxInput = nbt.getLong("max_input");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeVarLong(value);
		buffer.writeVarLong(maxInput);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		value = buffer.readVarLong();
		maxInput = buffer.readVarLong();
	}

	@Override
	public void setValue(long v)
	{
		value = v;
	}

	@Override
	public String getAltTitle()
	{
		return StringUtils.formatDouble(value, true);
	}

	@Override
	public boolean consumesResources()
	{
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addLong("value", value, v -> value = v, 1000L, 1L, Long.MAX_VALUE);
		config.addLong("max_input", maxInput, v -> maxInput = v, 0L, 0L, Integer.MAX_VALUE).setNameKey("ftbquests.task.max_input");
	}
}