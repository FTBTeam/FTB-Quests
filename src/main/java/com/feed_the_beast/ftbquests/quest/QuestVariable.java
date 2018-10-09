package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

/**
 * @author LatvianModder
 */
public final class QuestVariable extends QuestObject
{
	public final QuestFile file;
	public long maxValue;
	public boolean team;
	public short index;

	private String cachedID = "";

	public QuestVariable(QuestFile f, NBTTagCompound nbt)
	{
		file = f;
		readCommonData(nbt);
		maxValue = nbt.getLong("max");

		if (maxValue < 1L)
		{
			maxValue = 1L;
		}

		team = nbt.getBoolean("team");
		index = -1;
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
	public String getID()
	{
		if (cachedID.isEmpty())
		{
			cachedID = '#' + id;
		}

		return cachedID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		writeCommonData(nbt);

		nbt.setLong("max", maxValue);

		if (team)
		{
			nbt.setBoolean("team", true);
		}
	}

	@Override
	public Icon getAltIcon()
	{
		return GuiIcons.CONTROLLER;
	}

	@Override
	public long getProgress(ITeamData data)
	{
		return data.getVariable(this);
	}

	@Override
	public long getMaxProgress()
	{
		return maxValue;
	}

	@Override
	public void resetProgress(ITeamData data, boolean dependencies)
	{
		data.setVariable(this, 0L);
	}

	@Override
	public void completeInstantly(ITeamData data, boolean dependencies)
	{
		data.setVariable(this, maxValue);
	}

	@Override
	public int getRelativeProgress(ITeamData data)
	{
		if (maxValue <= 0L)
		{
			return 100;
		}

		long value = data.getVariable(this);

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
		return data.getVariable(this) >= maxValue;
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentString(getID());
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
	public void clearCachedData()
	{
		super.clearCachedData();
		cachedID = "";
	}
}