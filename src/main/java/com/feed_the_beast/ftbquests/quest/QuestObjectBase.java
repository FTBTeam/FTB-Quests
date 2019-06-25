package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.EnumTristate;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import com.feed_the_beast.ftbquests.gui.editor.ConfigPane;
import com.feed_the_beast.ftbquests.net.edit.MessageChangeProgressResponse;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.latmod.mods.itemfilters.item.ItemMissing;
import javafx.scene.Node;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Optional;

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
	private String cachedTitle = null;

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

	public void forceProgress(ITeamData data, EnumChangeProgress type, boolean notifications)
	{
		EnumChangeProgress.sendUpdates = false;
		EnumChangeProgress.sendNotifications = notifications ? EnumTristate.TRUE : EnumTristate.FALSE;
		changeProgress(data, type);
		EnumChangeProgress.sendUpdates = true;
		EnumChangeProgress.sendNotifications = EnumTristate.DEFAULT;

		if (!getQuestFile().isClient())
		{
			new MessageChangeProgressResponse(data.getTeamUID(), id, type, notifications).sendToAll();
		}
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

	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		config.addString("title", () -> title, v -> title = v, "").setDisplayName(new TextComponentTranslation("ftbquests.title")).setOrder(-127);
		config.add("icon", new ConfigItemStack.SimpleStack(() -> icon, v -> icon = v), new ConfigItemStack(ItemStack.EMPTY)).setDisplayName(new TextComponentTranslation("ftbquests.icon")).setOrder(-126);
	}

	public abstract Icon getAltIcon();

	public abstract String getAltTitle();

	public final Icon getIcon()
	{
		if (cachedIcon != null)
		{
			return cachedIcon;
		}

		if (!icon.isEmpty())
		{
			cachedIcon = ItemIcon.getItemIcon(icon);
		}
		else
		{
			cachedIcon = getAltIcon();
		}

		return cachedIcon;
	}

	public final String getTitle()
	{
		if (cachedTitle != null)
		{
			return cachedTitle;
		}

		String key = String.format("quests.%08x.title", id);
		String t = FTBQuestsClient.addI18nAndColors(I18n.format(key));

		if (t.isEmpty() || key.equals(t))
		{
			if (!title.isEmpty())
			{
				cachedTitle = FTBQuestsClient.addI18nAndColors(title);
			}
			else
			{
				cachedTitle = getAltTitle().trim();
			}
		}
		else
		{
			cachedTitle = t;
		}

		return cachedTitle;
	}

	public final String getUnformattedTitle()
	{
		return StringUtils.unformatted(getTitle());
	}

	public final String getYellowDisplayName()
	{
		return TextFormatting.YELLOW + getTitle();
	}

	public void deleteSelf()
	{
		getQuestFile().remove(id);
	}

	public void deleteChildren()
	{
	}

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
		cachedTitle = null;
	}

	public ConfigGroup createSubGroup(ConfigGroup group)
	{
		return group.getGroup(getObjectType().getID());
	}

	public void onEditButtonClicked()
	{
		new MessageEditObject(id).sendToServer();
	}

	public int refreshJEI()
	{
		return 0;
	}

	public Optional<Node> createTabContent()
	{
		return Optional.of(new ConfigPane(this));
	}
}