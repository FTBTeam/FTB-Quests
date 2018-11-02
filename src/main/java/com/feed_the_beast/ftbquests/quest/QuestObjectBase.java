package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.item.ItemMissing;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public abstract class QuestObjectBase
{
	public static final Pattern ID_PATTERN = Pattern.compile("^[a-z0-9_]{1,32}$");

	public int uid = 0;
	public boolean invalid = false;
	private String title = "";
	private ItemStack icon = ItemStack.EMPTY;

	private Icon cachedIcon = null;

	public final String toString()
	{
		return String.format("#%08x", uid);
	}

	public final boolean equals(Object object)
	{
		return object == this;
	}

	public final int hashCode()
	{
		return uid;
	}

	public abstract QuestFile getQuestFile();

	public abstract void writeData(NBTTagCompound nbt);

	public abstract void readData(NBTTagCompound nbt);

	public abstract void getConfig(ConfigGroup config);

	public abstract void resetProgress(ITeamData data, boolean dependencies);

	public abstract Icon getAltIcon();

	public abstract ITextComponent getAltDisplayName();

	public void getExtraConfig(ConfigGroup config)
	{
		config.addString("title", () -> title, v -> title = v, "").setDisplayName(new TextComponentTranslation("ftbquests.title")).setOrder((byte) -127);
		config.add("icon", new ConfigItemStack.SimpleStack(() -> icon, v -> icon = v), new ConfigItemStack(ItemStack.EMPTY)).setDisplayName(new TextComponentTranslation("ftbquests.icon")).setOrder((byte) -126);
	}

	public void writeCommonData(NBTTagCompound nbt)
	{
		if (!title.isEmpty())
		{
			nbt.setString("title", title);
		}

		if (!icon.isEmpty())
		{
			nbt.setTag("icon", ItemMissing.write(icon, false));
		}
	}

	public void readCommonData(NBTTagCompound nbt)
	{
		if (this instanceof QuestFile)
		{
			uid = 0;
		}
		else
		{
			uid = getQuestFile().readID(nbt.getInteger("uid"));
		}

		title = nbt.getString("title");
		icon = ItemMissing.read(nbt.getTag("icon"));
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

	public final ITextComponent getDisplayName()
	{
		if (!title.isEmpty())
		{
			return new TextComponentString(title.equals("-") ? "" : title);
		}

		return getAltDisplayName();
	}

	@Nullable
	public QuestChapter getQuestChapter()
	{
		return null;
	}

	public void deleteSelf()
	{
		getQuestFile().remove(uid);
	}

	public void deleteChildren()
	{
	}

	public void onCreated()
	{
	}

	public void clearCachedData()
	{
		cachedIcon = null;
	}
}