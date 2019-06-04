package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.latmod.mods.itemfilters.item.ItemMissing;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.File;

/**
 * @author LatvianModder
 */
public abstract class QuestObjectBase
{
	public int id = 0;
	public boolean invalid = false;
	public String title = "";
	public ItemStack icon = ItemStack.EMPTY;

	private Icon cachedIcon = null;
	private ITextComponent cachedDisplayName = null;

	public final String toString()
	{
		return String.format("#%08x", id);
	}

	public final String getCodeString()
	{
		return String.format("%08x", id);
	}

	public final boolean equals(Object object)
	{
		return object == this;
	}

	public final int hashCode()
	{
		return id;
	}

	public abstract QuestObjectType getObjectType();

	public abstract QuestFile getQuestFile();

	public void changeProgress(ITeamData data, EnumChangeProgress type)
	{
	}

	@Nullable
	public QuestChapter getQuestChapter()
	{
		return null;
	}

	public int getParentID()
	{
		return 0;
	}

	public void writeData(NBTTagCompound nbt)
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

	public void readData(NBTTagCompound nbt)
	{
		title = nbt.getString("title");
		icon = ItemMissing.read(nbt.getTag("icon"));
	}

	public void writeNetData(DataOut data)
	{
		data.writeString(title);
		data.writeItemStack(icon);
	}

	public void readNetData(DataIn data)
	{
		title = data.readString();
		icon = data.readItemStack();
	}

	public void getConfig(ConfigGroup config)
	{
		config.addString("title", () -> title, v -> title = v, "").setDisplayName(new TextComponentTranslation("ftbquests.title")).setOrder(-127);
		config.add("icon", new ConfigItemStack.SimpleStack(() -> icon, v -> icon = v), new ConfigItemStack(ItemStack.EMPTY)).setDisplayName(new TextComponentTranslation("ftbquests.icon")).setOrder(-126);
	}

	public abstract Icon getAltIcon();

	public abstract ITextComponent getAltDisplayName();

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
		if (cachedDisplayName != null)
		{
			return cachedDisplayName;
		}

		if (!title.isEmpty())
		{
			cachedDisplayName = title.startsWith("{") && title.endsWith("}") ? new TextComponentTranslation(title.substring(1, title.length() - 1)) : new TextComponentString(title);
		}
		else
		{
			cachedDisplayName = getAltDisplayName();
		}

		return cachedDisplayName;
	}

	public final ITextComponent getYellowDisplayName()
	{
		ITextComponent component = getDisplayName().createCopy();
		component.getStyle().setColor(TextFormatting.YELLOW);
		return component;
	}

	public String getI18NKey()
	{
		return String.format("%s.%08x.title", getObjectType().getID(), id);
	}

	@SideOnly(Side.CLIENT)
	public final String getDisplayNameString(boolean formatted)
	{
		String key = getI18NKey();

		if (I18n.hasKey(key))
		{
		}

		return formatted ? getDisplayName().getFormattedText() : getDisplayName().getUnformattedText();
	}

	public void deleteSelf()
	{
		getQuestFile().remove(id);
	}

	public void deleteChildren()
	{
	}

	@SideOnly(Side.CLIENT)
	public void editedFromGUI()
	{
		ClientQuestFile.INSTANCE.refreshGui();
	}

	public void onCreated()
	{
	}

	@Nullable
	public File getFile(File folder)
	{
		return null;
	}

	public void clearCachedData()
	{
		cachedIcon = null;
		cachedDisplayName = null;
	}

	public ConfigGroup createSubGroup(ConfigGroup group)
	{
		return group.getGroup(getObjectType().getID());
	}

	@SideOnly(Side.CLIENT)
	public void onEditButtonClicked()
	{
		new MessageEditObject(id).sendToServer();
	}

	public int refreshJEI()
	{
		return 0;
	}
}