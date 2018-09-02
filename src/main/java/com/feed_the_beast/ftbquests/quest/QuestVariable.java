package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

/**
 * @author LatvianModder
 */
public class QuestVariable extends QuestObject
{
	public final QuestFile file;
	public long maxValue;
	public boolean team;
	public short index;

	private String cachedID = "";

	public QuestVariable(QuestFile f, NBTTagCompound nbt)
	{
		file = f;
		title = nbt.getString("title");
		icon = ItemStackSerializer.read(nbt.getCompoundTag("icon"));
		completionCommand = nbt.getString("completion_command");
		maxValue = nbt.getLong("max");
		team = nbt.getBoolean("team");
		index = -1;

		readID(nbt);
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
		nbt.setString("id", id);

		if (!title.isEmpty())
		{
			nbt.setString("title", title);
		}

		if (!icon.isEmpty())
		{
			nbt.setTag("icon", ItemStackSerializer.write(icon));
		}

		if (!completionCommand.isEmpty())
		{
			nbt.setString("completion_command", completionCommand);
		}

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
	public void resetProgress(ITeamData data)
	{
		data.setVariable(this, 0L);
	}

	@Override
	public void completeInstantly(ITeamData data)
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
	public void clearCachedData()
	{
		super.clearCachedData();
		cachedID = "";
	}
}