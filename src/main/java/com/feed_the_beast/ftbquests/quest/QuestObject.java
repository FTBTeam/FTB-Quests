package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
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

	public abstract QuestObjectType getObjectType();

	@Deprecated
	public abstract String getID();

	@Override
	public abstract void writeData(NBTTagCompound nbt);

	@Override
	public abstract void readData(NBTTagCompound nbt);

	@Override
	public final void writeCommonData(NBTTagCompound nbt)
	{
		super.writeCommonData(nbt);

		if (!completionCommand.isEmpty())
		{
			nbt.setString("completion_command", completionCommand);
		}
	}

	@Override
	public final void readCommonData(NBTTagCompound nbt)
	{
		super.readCommonData(nbt);

		if (getObjectType() == QuestObjectType.FILE)
		{
			id = "*";
		}
		else
		{
			id = nbt.getString("id").trim();

			if (id.isEmpty() || getQuestFile().get(getID()) != null)
			{
				id = String.format("%08x", uid);
			}
		}

		completionCommand = nbt.getString("completion_command");
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
					.replace("@id", getID())
					.replace("@idn", getID().replace(':', '_')));
		}
	}

	@Override
	public abstract ITextComponent getAltDisplayName();

	@Override
	public final void getExtraConfig(ConfigGroup config)
	{
		super.getExtraConfig(config);
		config.addString("completion_command", () -> completionCommand, v -> completionCommand = v, "").setDisplayName(new TextComponentTranslation("ftbquests.completion_command")).setOrder((byte) 150);
	}
}