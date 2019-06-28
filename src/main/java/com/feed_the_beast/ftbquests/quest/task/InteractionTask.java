package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.util.RayMatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class InteractionTask extends Task
{
	public final RayMatcher matcher;

	public InteractionTask(Quest quest)
	{
		super(quest);
		matcher = new RayMatcher();
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.INTERACTION;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		matcher.writeData(nbt);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		matcher.readData(nbt);
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		matcher.writeNetData(data);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		matcher.readNetData(data);
	}

	@Override
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.addEnum("type", () -> matcher.type, v -> matcher.type = v, RayMatcher.Type.NAME_MAP);
		config.addString("match", () -> matcher.match, v -> matcher.match = v, "");
		config.addString("properties", matcher::getPropertyString, matcher::setPropertyString, "");
	}

	@Override
	public void onButtonClicked(boolean canClick)
	{
	}

	@Override
	public TaskData createData(QuestData data)
	{
		return new BooleanTaskData<>(this, data);
	}
}