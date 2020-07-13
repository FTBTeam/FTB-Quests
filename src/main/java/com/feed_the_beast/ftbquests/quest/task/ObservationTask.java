package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigTimer;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.math.Ticks;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.util.RayMatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class ObservationTask extends Task
{
	public final RayMatcher matcher;
	public Ticks timer;

	public ObservationTask(Quest quest)
	{
		super(quest);
		matcher = new RayMatcher();
		timer = Ticks.NO_TICKS;
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.OBSERVATION;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		matcher.writeData(nbt);
		nbt.setLong("timer", timer.ticks());
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		matcher.readData(nbt);
		timer = Ticks.get(nbt.getLong("timer"));
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		matcher.writeNetData(data);
		data.writeVarLong(timer.ticks());
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		matcher.readNetData(data);
		timer = Ticks.get(data.readVarLong());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addEnum("type", () -> matcher.type, v -> matcher.type = v, RayMatcher.Type.NAME_MAP);
		config.addString("match", () -> matcher.match, v -> matcher.match = v, "");
		config.addString("properties", matcher::getPropertyString, matcher::setPropertyString, "");
		config.add("timer", new ConfigTimer(timer)
		{
			@Override
			public Ticks getTimer()
			{
				return timer;
			}

			@Override
			public void setTimer(Ticks v)
			{
				timer = v;
			}
		}, new ConfigTimer(Ticks.NO_TICKS));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onButtonClicked(boolean canClick)
	{
		if (ClientQuestFile.INSTANCE.canEdit())
		{
			Minecraft mc = Minecraft.getMinecraft();
			RayMatcher.Data data = RayMatcher.Data.get(mc.world, mc.objectMouseOver);
			mc.player.sendMessage(new TextComponentString(data.toString()));
		}
	}

	@Override
	public TaskData createData(QuestData data)
	{
		return new BooleanTaskData<>(this, data);
	}
}