package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.client.ConfigIconItemStack;
import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import com.feed_the_beast.ftbquests.item.CustomIconItem;
import com.feed_the_beast.ftbquests.net.MessageChangeProgressResponse;
import com.feed_the_beast.ftbquests.net.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.ftbquests.util.NBTUtils;
import com.feed_the_beast.ftbquests.util.NetUtils;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigString;
import com.feed_the_beast.mods.ftbguilibrary.config.Tristate;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.Bits;
import me.shedaniel.architectury.utils.NbtType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public abstract class QuestObjectBase {
	private static final Pattern TAG_PATTERN = Pattern.compile("^[a-z0-9_]*$");

	public static boolean isNull(@Nullable QuestObjectBase object) {
		return object == null || object.invalid;
	}

	public static long getID(@Nullable QuestObjectBase object) {
		return isNull(object) ? 0L : object.id;
	}

	public static String getCodeString(long id) {
		return id == 0L ? "-" : String.format("%016X", id);
	}

	public static String getCodeString(@Nullable QuestObjectBase object) {
		return getCodeString(getID(object));
	}

	public static long parseCodeString(String id) {
		if (id.isEmpty() || id.equals("-")) {
			return 0L;
		}

		try {
			return Long.parseLong(id.charAt(0) == '#' ? id.substring(1) : id, 16);
		} catch (Exception ex) {
			return 0L;
		}
	}

	public static Optional<String> titleToID(String s) {
		s = s.replace(' ', '_').replaceAll("\\W", "").toLowerCase().trim();

		while (s.startsWith("_")) {
			s = s.substring(1);
		}

		while (s.endsWith("_")) {
			s = s.substring(0, s.length() - 1);
		}

		return s.isEmpty() ? Optional.empty() : Optional.of(s);
	}

	public long id = 0L;
	public boolean invalid = false;
	public String title = "";
	public ItemStack icon = ItemStack.EMPTY;
	private List<String> tags = new ArrayList<>(0);

	private Icon cachedIcon = null;
	private Component cachedTitle = null;
	private Set<String> cachedTags = null;

	public final String getCodeString() {
		return getCodeString(id);
	}

	public String toString() {
		return getCodeString();
	}

	public final boolean equals(Object object) {
		return object == this;
	}

	public final int hashCode() {
		return Long.hashCode(id);
	}

	public abstract QuestObjectType getObjectType();

	public abstract QuestFile getQuestFile();

	public Set<String> getTags() {
		if (tags.isEmpty()) {
			return Collections.emptySet();
		} else if (cachedTags == null) {
			cachedTags = new LinkedHashSet<>(tags);
		}

		return cachedTags;
	}

	public boolean hasTag(String tag) {
		return !tags.isEmpty() && getTags().contains(tag);
	}

	public void changeProgress(PlayerData data, ChangeProgress type) {
	}

	public void forceProgress(PlayerData data, ChangeProgress type, boolean notifications) {
		ChangeProgress.sendUpdates = false;
		ChangeProgress.sendNotifications = notifications ? Tristate.TRUE : Tristate.FALSE;
		changeProgress(data, type);
		ChangeProgress.sendUpdates = true;
		ChangeProgress.sendNotifications = Tristate.DEFAULT;
		getQuestFile().clearCachedProgress();

		if (getQuestFile().isServerSide()) {
			new MessageChangeProgressResponse(data.uuid, id, type, notifications).sendToAll();
		}

		data.save();
	}

	@Nullable
	public Chapter getQuestChapter() {
		return null;
	}

	public long getParentID() {
		return 1L;
	}

	public void writeData(CompoundTag nbt) {
		if (!title.isEmpty()) {
			nbt.putString("title", title);
		}

		NBTUtils.write(nbt, "icon", icon);

		if (!tags.isEmpty()) {
			ListTag tagList = new ListTag();

			for (String s : tags) {
				tagList.add(StringTag.valueOf(s));
			}

			nbt.put("tags", tagList);
		}
	}

	public void readData(CompoundTag nbt) {
		title = nbt.getString("title");
		icon = NBTUtils.read(nbt, "icon");

		ListTag tagsList = nbt.getList("tags", NbtType.STRING);

		tags = new ArrayList<>(tagsList.size());

		for (int i = 0; i < tagsList.size(); i++) {
			tags.add(tagsList.getString(i));
		}

		if (nbt.contains("custom_id")) {
			tags.add(nbt.getString("custom_id"));
		}
	}

	public void writeNetData(FriendlyByteBuf buffer) {
		int flags = 0;
		flags = Bits.setFlag(flags, 1, !title.isEmpty());
		flags = Bits.setFlag(flags, 2, !icon.isEmpty());
		flags = Bits.setFlag(flags, 4, !tags.isEmpty());

		buffer.writeVarInt(flags);

		if (!title.isEmpty()) {
			buffer.writeUtf(title, Short.MAX_VALUE);
		}

		if (!icon.isEmpty()) {
			buffer.writeItem(icon);
		}

		if (!tags.isEmpty()) {
			NetUtils.writeStrings(buffer, tags);
		}
	}

	public void readNetData(FriendlyByteBuf buffer) {
		int flags = buffer.readVarInt();
		title = Bits.getFlag(flags, 1) ? buffer.readUtf(Short.MAX_VALUE) : "";
		icon = Bits.getFlag(flags, 2) ? buffer.readItem() : ItemStack.EMPTY;
		tags = new ArrayList<>(0);

		if (Bits.getFlag(flags, 4)) {
			NetUtils.readStrings(buffer, tags);
		}
	}

	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		config.addString("title", title, v -> title = v, "").setNameKey("ftbquests.title").setOrder(-127);
		config.add("icon", new ConfigIconItemStack(), icon, v -> icon = v, ItemStack.EMPTY).setNameKey("ftbquests.icon").setOrder(-126);
		config.addList("tags", tags, new ConfigString(TAG_PATTERN), "").setNameKey("ftbquests.tags").setOrder(-125);
	}

	@Environment(EnvType.CLIENT)
	public abstract Component getAltTitle();

	@Environment(EnvType.CLIENT)
	public abstract Icon getAltIcon();

	@Environment(EnvType.CLIENT)
	public final Component getTitle() {
		if (cachedTitle != null) {
			return cachedTitle.copy();
		}

		String key = String.format("quests.%s.title", getCodeString());
		String s = title.isEmpty() ? I18n.exists(key) ? I18n.get(key) : "" : title;

		if (!s.isEmpty()) {
			cachedTitle = FTBQuestsClient.parse(s);
		} else {
			cachedTitle = getAltTitle();
		}

		return cachedTitle.copy();
	}

	@Environment(EnvType.CLIENT)
	public final MutableComponent getMutableTitle() {
		return new TextComponent("").append(getTitle());
	}

	@Environment(EnvType.CLIENT)
	public final Icon getIcon() {
		if (cachedIcon != null) {
			return cachedIcon;
		}

		if (!icon.isEmpty()) {
			cachedIcon = CustomIconItem.getIcon(icon);
		}

		if (cachedIcon == null || cachedIcon.isEmpty()) {
			cachedIcon = ThemeProperties.ICON.get(this);
		}

		if (cachedIcon.isEmpty()) {
			cachedIcon = getAltIcon();
		}

		return cachedIcon;
	}

	public void deleteSelf() {
		getQuestFile().remove(id);
	}

	public void deleteChildren() {
	}

	@Environment(EnvType.CLIENT)
	public void editedFromGUI() {
		ClientQuestFile.INSTANCE.refreshGui();
	}

	public void onCreated() {
	}

	@Nullable
	public String getPath() {
		return null;
	}

	public void clearCachedData() {
		cachedIcon = null;
		cachedTitle = null;
		cachedTags = null;
	}

	public ConfigGroup createSubGroup(ConfigGroup group) {
		return group.getGroup(getObjectType().id);
	}

	@Environment(EnvType.CLIENT)
	public void onEditButtonClicked(Runnable gui) {
		ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
		getConfig(createSubGroup(group));

		group.savedCallback = accepted -> {
			gui.run();
			if (accepted) {
				new MessageEditObject(this).sendToServer();
			}
		};

		new GuiEditConfig(group).openGui();
	}

	public int refreshJEI() {
		return 0;
	}
}