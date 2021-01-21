package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import com.feed_the_beast.ftbquests.net.MessageChangeProgressResponse;
import com.feed_the_beast.ftbquests.net.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.ftbquests.util.FileUtils;
import com.feed_the_beast.ftbquests.util.NBTUtils;
import com.feed_the_beast.ftbquests.util.NetUtils;
import com.feed_the_beast.ftbquests.util.QuestObjectText;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigItemStack;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigString;
import com.feed_the_beast.mods.ftbguilibrary.config.Tristate;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.utils.Bits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public abstract class QuestObjectBase
{
	private static final Pattern TAG_PATTERN = Pattern.compile("^[a-z0-9_]*$");

	public static boolean isNull(@Nullable QuestObjectBase object)
	{
		return object == null || object.invalid;
	}

	public static int getID(@Nullable QuestObjectBase object)
	{
		return isNull(object) ? 0 : object.id;
	}

	public static String getCodeString(int id)
	{
		return Integer.toUnsignedString(id);
	}

	public static String getCodeString(@Nullable QuestObjectBase object)
	{
		return getCodeString(getID(object));
	}

	public int id = 0;
	public boolean invalid = false;
	public String title = "";
	public ItemStack icon = ItemStack.EMPTY;
	private List<String> tags = new ArrayList<>(0);

	private Icon cachedIcon = null;
	private MutableComponent cachedTitle = null;
	private QuestObjectText cachedTextFile = null;
	private Set<String> cachedTags = null;

	public final String getCodeString()
	{
		return getCodeString(id);
	}

	public String toString()
	{
		return getCodeString();
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

	public Set<String> getTags()
	{
		if (tags.isEmpty())
		{
			return Collections.emptySet();
		}
		else if (cachedTags == null)
		{
			cachedTags = new LinkedHashSet<>(tags);
		}

		return cachedTags;
	}

	public boolean hasTag(String tag)
	{
		return !tags.isEmpty() && getTags().contains(tag);
	}

	public void changeProgress(PlayerData data, ChangeProgress type)
	{
	}

	public void forceProgress(PlayerData data, ChangeProgress type, boolean notifications)
	{
		ChangeProgress.sendUpdates = false;
		ChangeProgress.sendNotifications = notifications ? Tristate.TRUE : Tristate.FALSE;
		changeProgress(data, type);
		ChangeProgress.sendUpdates = true;
		ChangeProgress.sendNotifications = Tristate.DEFAULT;
		getQuestFile().clearCachedProgress();

		if (!getQuestFile().getSide().isClient())
		{
			new MessageChangeProgressResponse(data.uuid, id, type, notifications).sendToAll();
		}

		data.save();
	}

	@Nullable
	public Chapter getQuestChapter()
	{
		return null;
	}

	public int getParentID()
	{
		return 1;
	}

	public void writeData(CompoundTag nbt)
	{
		if (!title.isEmpty())
		{
			nbt.putString("title", title);
		}

		NBTUtils.write(nbt, "icon", icon);

		if (!tags.isEmpty())
		{
			ListTag tagList = new ListTag();

			for (String s : tags)
			{
				tagList.add(StringTag.valueOf(s));
			}

			nbt.put("tags", tagList);
		}
	}

	public void readData(CompoundTag nbt)
	{
		title = nbt.getString("title");
		icon = NBTUtils.read(nbt, "icon");

		ListTag tagsList = nbt.getList("tags", Constants.NBT.TAG_STRING);

		tags = new ArrayList<>(tagsList.size());

		for (int i = 0; i < tagsList.size(); i++)
		{
			tags.add(tagsList.getString(i));
		}

		if (nbt.contains("custom_id"))
		{
			tags.add(nbt.getString("custom_id"));
		}
	}

	public void writeNetData(FriendlyByteBuf buffer)
	{
		int flags = 0;
		flags = Bits.setFlag(flags, 1, !title.isEmpty());
		flags = Bits.setFlag(flags, 2, !icon.isEmpty());
		flags = Bits.setFlag(flags, 4, !tags.isEmpty());

		buffer.writeVarInt(flags);

		if (!title.isEmpty())
		{
			buffer.writeUtf(title);
		}

		if (!icon.isEmpty())
		{
			buffer.writeItem(icon);
		}

		if (!tags.isEmpty())
		{
			NetUtils.writeStrings(buffer, tags);
		}
	}

	public void readNetData(FriendlyByteBuf buffer)
	{
		int flags = buffer.readVarInt();
		title = Bits.getFlag(flags, 1) ? buffer.readUtf() : "";
		icon = Bits.getFlag(flags, 2) ? buffer.readItem() : ItemStack.EMPTY;
		tags = new ArrayList<>(0);

		if (Bits.getFlag(flags, 4))
		{
			NetUtils.readStrings(buffer, tags);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		config.addString("title", title, v -> title = v, "").setNameKey("ftbquests.title").setOrder(-127);
		config.add("icon", new ConfigItemStack(false, true), icon, v -> icon = v, ItemStack.EMPTY).setNameKey("ftbquests.icon").setOrder(-126);
		config.addList("tags", tags, new ConfigString(TAG_PATTERN), "").setNameKey("ftbquests.tags").setOrder(-125);
	}

	@OnlyIn(Dist.CLIENT)
	public QuestObjectText loadText()
	{
		if (invalid || id == 0)
		{
			return QuestObjectText.NONE;
		}
		else if (cachedTextFile == null)
		{
			cachedTextFile = QuestObjectText.NONE;
			File file = FMLPaths.CONFIGDIR.get().resolve("ftbquests/quests/text/en_us/" + getCodeString(this) + ".txt").toFile();
			Map<String, String[]> text = new HashMap<>();

			if (file.exists())
			{
				String currentKey = "";
				List<String> currentText = new ArrayList<>();

				for (String s : FileUtils.readFile(file))
				{
					if (s.indexOf('[') == 0 && s.indexOf(']') == s.length() - 1)
					{
						loadTextAdd(text, currentKey, currentText);
						currentKey = s.substring(1, s.length() - 1);
					}
					else
					{
						currentText.add(s);
					}
				}

				loadTextAdd(text, currentKey, currentText);
			}

			String langCode = Minecraft.getInstance().getLanguageManager().getSelected().getCode();

			if (!langCode.equals("en_us"))
			{
				File fileLang = FMLPaths.CONFIGDIR.get().resolve("ftbquests/quests/text/" + langCode + "/" + getCodeString(this) + ".txt").toFile();

				if (fileLang.exists())
				{
					String currentKey = "";
					List<String> currentText = new ArrayList<>();

					for (String s : FileUtils.readFile(fileLang))
					{
						if (s.indexOf('[') == 0 && s.indexOf(']') == s.length() - 1)
						{
							loadTextAdd(text, currentKey, currentText);
							currentKey = s.substring(1, s.length() - 1);
						}
						else
						{
							currentText.add(s);
						}
					}

					loadTextAdd(text, currentKey, currentText);
				}
			}

			cachedTextFile = text.isEmpty() ? QuestObjectText.NONE : new QuestObjectText(text);
		}

		return cachedTextFile;
	}

	private void loadTextAdd(Map<String, String[]> text, String currentKey, List<String> currentText)
	{
		while (!currentText.isEmpty() && currentText.get(0).isEmpty())
		{
			currentText.remove(0);
		}

		while (!currentText.isEmpty() && currentText.get(currentText.size() - 1).isEmpty())
		{
			currentText.remove(currentText.size() - 1);
		}

		if (!currentText.isEmpty())
		{
			text.put(currentKey, currentText.toArray(new String[0]));
			currentText.clear();
		}
	}

	public abstract Icon getAltIcon();

	public abstract MutableComponent getAltTitle();

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

		if (cachedIcon == null || cachedIcon.isEmpty())
		{
			cachedIcon = ThemeProperties.ICON.get(this);
		}

		if (cachedIcon.isEmpty())
		{
			cachedIcon = getAltIcon();
		}

		return cachedIcon;
	}

	@OnlyIn(Dist.CLIENT)
	public final MutableComponent getTitle()
	{
		if (cachedTitle != null)
		{
			return cachedTitle.copy();
		}

		String textTitle = loadText().getString("title");

		if (!textTitle.isEmpty())
		{
			cachedTitle = new TextComponent(textTitle);
			return cachedTitle.copy();
		}

		String key = String.format("quests.%08x.title", id);
		MutableComponent t = FTBQuestsClient.addI18nAndColors(I18n.get(key));

		if (t == TextComponent.EMPTY || key.equals(I18n.get(key)))
		{
			if (!title.isEmpty())
			{
				cachedTitle = FTBQuestsClient.addI18nAndColors(title);
			}
			else
			{
				cachedTitle = getAltTitle();
			}
		}
		else
		{
			cachedTitle = t;
		}

		return cachedTitle.copy();
	}

	public final String getUnformattedTitle()
	{
		return getTitle().getString();
	}

	public void deleteSelf()
	{
		getQuestFile().remove(id);
	}

	public void deleteChildren()
	{
	}

	@OnlyIn(Dist.CLIENT)
	public void editedFromGUI()
	{
		ClientQuestFile.INSTANCE.refreshGui();
	}

	public void onCreated()
	{
	}

	@Nullable
	public String getPath()
	{
		return null;
	}

	public void clearCachedData()
	{
		cachedIcon = null;
		cachedTitle = null;
		cachedTextFile = null;
		cachedTags = null;
	}

	public ConfigGroup createSubGroup(ConfigGroup group)
	{
		return group.getGroup(getObjectType().id);
	}

	@OnlyIn(Dist.CLIENT)
	public void onEditButtonClicked(Runnable gui)
	{
		ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
		getConfig(createSubGroup(group));

		group.savedCallback = accepted -> {
			gui.run();
			if (accepted)
			{
				new MessageEditObject(this).sendToServer();
			}
		};

		new GuiEditConfig(group).openGui();
	}

	public int refreshJEI()
	{
		return 0;
	}
}