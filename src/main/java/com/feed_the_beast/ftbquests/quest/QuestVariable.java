package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.File;

/**
 * @author LatvianModder
 */
public final class QuestVariable extends QuestObject
{
	public final QuestFile file;
	public long maxValue;
	public boolean team;

	public QuestVariable(QuestFile f)
	{
		file = f;
		maxValue = 1L;
		team = false;
	}

	@Override
	public QuestFile getQuestFile()
	{
		return file;
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.VARIABLE;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setLong("max", maxValue);

		if (team)
		{
			nbt.setBoolean("team", true);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		maxValue = nbt.getLong("max");

		if (maxValue < 1L)
		{
			maxValue = 1L;
		}

		team = nbt.getBoolean("team");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarLong(maxValue);
		data.writeBoolean(team);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		maxValue = data.readVarLong();
		team = data.readBoolean();
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addLong("max_value", () -> maxValue, v -> maxValue = v, 1L, 1L, Long.MAX_VALUE);
		config.addBool("team", () -> team, v -> team = v, false);
	}

	@Override
	public Icon getAltIcon()
	{
		return GuiIcons.CONTROLLER;
	}

	@Override
	public long getProgress(ITeamData data)
	{
		return data.getVariable(id);
	}

	@Override
	public long getMaxProgress()
	{
		return maxValue;
	}

	@Override
	public void changeProgress(ITeamData data, EnumChangeProgress type)
	{
		if (type.reset)
		{
			data.setVariable(id, 0L);
		}
		else if (type.complete)
		{
			data.setVariable(id, maxValue);
		}
	}

	@Override
	public int getRelativeProgress(ITeamData data)
	{
		if (maxValue <= 0L)
		{
			return 100;
		}

		long value = data.getVariable(id);

		if (value <= 0L)
		{
			return 0;
		}
		else if (value >= maxValue)
		{
			return 100;
		}

		return (int) (value * 100L / maxValue);
	}

	@Override
	public boolean isComplete(ITeamData data)
	{
		return data.getVariable(id) >= maxValue;
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentTranslation("ftbquests.variable");
	}

	@Override
	public void deleteSelf()
	{
		super.deleteSelf();
		file.variables.remove(this);
	}

	@Override
	public void onCreated()
	{
		file.variables.add(this);
	}

	@Override
	public File getFile(File folder)
	{
		return new File(folder, "variables/" + getCodeString() + ".nbt");
	}
}