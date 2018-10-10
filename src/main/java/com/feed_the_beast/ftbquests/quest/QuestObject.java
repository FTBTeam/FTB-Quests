package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.item.ItemMissing;
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

	public final void writeCommonData(NBTTagCompound nbt)
	{
		if (!title.isEmpty())
		{
			nbt.setString("title", title);
		}

		if (!icon.isEmpty())
		{
			nbt.setTag("icon", ItemMissing.write(icon, false));
		}

		if (!completionCommand.isEmpty())
		{
			nbt.setString("completion_command", completionCommand);
		}
	}

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

		title = nbt.getString("title");
		icon = ItemMissing.read(nbt.getTag("icon"));
		completionCommand = nbt.getString("completion_command");
	}

	public abstract Icon getAltIcon();

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
		config.addString("title", () -> title, v -> title = v, "").setDisplayName(new TextComponentTranslation("ftbquests.title")).setOrder((byte) -127);
		config.add("icon", new ConfigItemStack.SimpleStack(() -> icon, v -> icon = v), new ConfigItemStack(ItemStack.EMPTY)).setDisplayName(new TextComponentTranslation("ftbquests.icon")).setOrder((byte) -126);
		config.addString("completion_command", () -> completionCommand, v -> completionCommand = v, "").setDisplayName(new TextComponentTranslation("ftbquests.completion_command")).setOrder((byte) 150);
	}

	public void clearCachedData()
	{
		cachedIcon = null;
	}
}