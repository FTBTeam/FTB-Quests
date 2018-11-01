package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public abstract class QuestObject extends QuestObjectBase
{
	public String id = "";
	public boolean invalid = false;
	public String completionCommand = "";

	public abstract QuestObjectType getObjectType();

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
		if (getObjectType() != QuestObjectType.FILE)
		{
			id = nbt.getString("id").trim();

			if (id.isEmpty() || getQuestFile().get(getID()) != null)
			{
				id = StringUtils.fromUUID(UUID.randomUUID()).substring(0, 8);
			}
		}
		else
		{
			id = "*";
		}

		super.readCommonData(nbt);

		completionCommand = nbt.getString("completion_command");
	}

	public abstract long getProgress(ITeamData data);

	public abstract long getMaxProgress();

	public abstract void resetProgress(ITeamData data, boolean dependencies);

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
			Universe.get().server.commandManager.executeCommand(Universe.get().server, completionCommand
					.replace("@team", data.getTeamID())
					.replace("@id", getID())
					.replace("@idn", getID().replace(':', '_')));
		}
	}

	@Override
	public abstract ITextComponent getAltDisplayName();

	public void deleteSelf()
	{
		getQuestFile().remove(getID());
		invalid = true;
	}

	public void deleteChildren()
	{
	}

	public void onCreated()
	{
	}

	@Override
	public final String toString()
	{
		return getID();
	}

	@Override
	public final int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public final boolean equals(Object o)
	{
		return o == this;
	}

	@Override
	public final void getExtraConfig(ConfigGroup config)
	{
		super.getExtraConfig(config);
		config.addString("completion_command", () -> completionCommand, v -> completionCommand = v, "").setDisplayName(new TextComponentTranslation("ftbquests.completion_command")).setOrder((byte) 150);
	}
}