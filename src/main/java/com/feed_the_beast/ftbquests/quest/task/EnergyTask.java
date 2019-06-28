package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigLong;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

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
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setLong("value", value);

		if (maxInput > 0L)
		{
			nbt.setLong("max_input", maxInput);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
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
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarLong(value);
		data.writeVarLong(maxInput);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		value = data.readVarLong();
		maxInput = data.readVarLong();
	}

	@Override
	public ConfigLong getDefaultValue()
	{
		return new ConfigLong(value, 1L, Long.MAX_VALUE);
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
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.addLong("value", () -> value, v -> value = v, 1000L, 1L, Long.MAX_VALUE);
		config.addLong("max_input", () -> maxInput, v -> maxInput = v, 0L, 0L, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.task.max_input"));
	}
}