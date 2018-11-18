package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author LatvianModder
 */
public abstract class QuestObject extends QuestObjectBase
{
	public String id = "";
	public String completionCommand = "";

	@Deprecated
	public abstract String getID();

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);

		if (!completionCommand.isEmpty())
		{
			nbt.setString("completion_command", completionCommand);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		completionCommand = nbt.getString("completion_command");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		//data.writeString(completionCommand);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		//completionCommand = data.readString();
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("completion_command", () -> completionCommand, v -> completionCommand = v, "").setDisplayName(new TextComponentTranslation("ftbquests.completion_command")).setOrder(150);
	}

	public abstract long getProgress(ITeamData data);

	public abstract long getMaxProgress();

	public abstract void completeInstantly(ITeamData data, boolean dependencies);

	public abstract int getRelativeProgress(ITeamData data);

	public static int fixRelativeProgress(int progress, int max)
	{
		if (max <= 0 || progress >= max * 100)
		{
			return 100;
		}
		else if (progress <= 0)
		{
			return 0;
		}

		return (int) (progress / (double) max);
	}

	public abstract boolean isComplete(ITeamData data);

	public void onCompleted(ITeamData data)
	{
		if (!completionCommand.isEmpty() && !getQuestFile().isClient())
		{
			ServerQuestFile.INSTANCE.universe.server.commandManager.executeCommand(ServerQuestFile.INSTANCE.universe.server, completionCommand
					.replace("@team", data.getTeamID())
					.replace("@teamuid", String.format("%04X", data.getTeamUID()))
					.replace("@id", toString())
			);
		}
	}

	@Override
	public abstract ITextComponent getAltDisplayName();
}