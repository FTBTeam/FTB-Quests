package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public abstract class QuestObject
{
	public String id = "";
	public boolean invalid = false;

	public abstract QuestFile getQuestFile();

	public abstract QuestObjectType getObjectType();

	public abstract String getID();

	public abstract void writeData(NBTTagCompound nbt);

	public abstract Icon getIcon();

	public abstract ITextComponent getDisplayName();

	public void deleteSelf()
	{
		getQuestFile().remove(getID());
		invalid = true;
	}

	public void deleteChildren()
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

	public void getConfig(ConfigGroup config)
	{
	}

	public void readID(NBTTagCompound nbt)
	{
		id = nbt.getString("id");

		if (id.isEmpty())
		{
			id = StringUtils.getId(getDisplayName().getUnformattedText(), StringUtils.FLAG_ID_DEFAULTS);
		}

		if (id.length() > 32)
		{
			id = id.substring(0, 32);
		}

		if (id.isEmpty() || getQuestFile().get(getID()) != null)
		{
			id = StringUtils.fromUUID(UUID.randomUUID());
		}
	}

	public void clearCachedData()
	{
	}
}