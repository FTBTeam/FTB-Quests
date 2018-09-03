package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public abstract class QuestObject
{
	public static final Pattern ID_PATTERN = Pattern.compile("^[a-z0-9_]{1,32}$");

	public String id = "";
	public boolean invalid = false;
	public String title = "";
	public ItemStack icon = ItemStack.EMPTY;
	private Icon cachedIcon = null;
	public String completionCommand = "";

	public abstract QuestFile getQuestFile();

	public abstract QuestObjectType getObjectType();

	public abstract String getID();

	public abstract void writeData(NBTTagCompound nbt);

	public abstract Icon getAltIcon();

	public abstract long getProgress(ITeamData data);

	public abstract long getMaxProgress();

	public abstract void resetProgress(ITeamData data);

	public abstract void completeInstantly(ITeamData data);

	public abstract int getRelativeProgress(ITeamData data);

	public static int fixRelativeProgress(int progress, int max)
	{
		if (max <= 0)
		{
			return 100;
		}
		else if (progress <= 0)
		{
			return 0;
		}

		if (progress >= max * 100)
		{
			return 100;
		}

		return (int) (progress / (double) max);
	}

	public abstract boolean isComplete(ITeamData data);

	public void onCompleted(ITeamData data)
	{
		new ObjectCompletedEvent(data, this).post();

		if (!completionCommand.isEmpty())
		{
			Universe.get().server.commandManager.executeCommand(Universe.get().server, completionCommand
					.replace("@team", data.getTeamID())
					.replace("@id", getID())
					.replace("@idn", getID().replace(':', '_')));
		}
	}

	public final Icon getIcon()
	{
		if (cachedIcon == null)
		{
			if (!icon.isEmpty())
			{
				cachedIcon = ItemIcon.getItemIcon(icon);
			}
			else
			{
				cachedIcon = getAltIcon();
			}
		}

		return cachedIcon;
	}

	public abstract ITextComponent getAltDisplayName();

	public final ITextComponent getDisplayName()
	{
		if (!title.isEmpty())
		{
			return new TextComponentString(title.equals("-") ? "" : title);
		}

		return getAltDisplayName();
	}

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

	public void getConfig(ConfigGroup config)
	{
	}

	public void getExtraConfig(ConfigGroup config)
	{
		config.add("title", new ConfigString(title)
		{
			@Override
			public String getString()
			{
				return title;
			}

			@Override
			public void setString(String v)
			{
				title = v;
			}
		}, new ConfigString("")).setDisplayName(new TextComponentTranslation("ftbquests.title")).setOrder((byte) -127);

		config.add("icon", new ConfigItemStack(icon, true)
		{
			@Override
			public ItemStack getStack()
			{
				return icon;
			}

			@Override
			public void setStack(ItemStack v)
			{
				icon = v;
			}
		}, new ConfigItemStack(ItemStack.EMPTY)).setDisplayName(new TextComponentTranslation("ftbquests.icon")).setOrder((byte) -126);

		config.add("completion_command", new ConfigString(completionCommand)
		{
			@Override
			public String getString()
			{
				return completionCommand;
			}

			@Override
			public void setString(String v)
			{
				completionCommand = v;
			}
		}, new ConfigString("")).setDisplayName(new TextComponentTranslation("ftbquests.completion_command")).setOrder((byte) 150);
	}

	public void readID(NBTTagCompound nbt)
	{
		id = nbt.getString("id").trim();

		if (id.isEmpty() || getQuestFile().get(getID()) != null)
		{
			id = StringUtils.fromUUID(UUID.randomUUID()).substring(0, 8);
		}
	}

	public void clearCachedData()
	{
		cachedIcon = null;
	}
}