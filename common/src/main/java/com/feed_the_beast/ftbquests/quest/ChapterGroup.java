package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.item.CustomIconItem;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.util.NBTUtils;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconAnimation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ChapterGroup {
	public final QuestFile file;
	public final String id;
	public final List<Chapter> chapters;
	private String title;
	private ItemStack icon;

	public ChapterGroup(QuestFile f, String i) {
		file = f;
		id = i;
		chapters = new ArrayList<>();
		title = "Unnamed Group";
		icon = ItemStack.EMPTY;
	}

	public static ChapterGroup readNet(QuestFile f, FriendlyByteBuf buffer) {
		String id = buffer.readUtf(Short.MAX_VALUE);

		if (id.isEmpty()) {
			return f.defaultChapterGroup;
		}

		ChapterGroup g = new ChapterGroup(f, id);
		g.title = buffer.readUtf(Short.MAX_VALUE);
		g.icon = FTBQuestsNetHandler.readItemType(buffer);
		return g;
	}

	public void writeNet(FriendlyByteBuf buffer) {
		buffer.writeUtf(id, Short.MAX_VALUE);

		if (id.isEmpty()) {
			return;
		}

		buffer.writeUtf(title, Short.MAX_VALUE);
		FTBQuestsNetHandler.writeItemType(buffer, icon);
	}

	public void read(CompoundTag tag) {
		title = tag.getString("title");
		icon = NBTUtils.read(tag, "icon");
	}

	public void write(CompoundTag tag) {
		tag.putString("title", title);
		NBTUtils.write(tag, "icon", icon);
	}

	public String getTitle() {
		return title;
	}

	public ItemStack getIconItem() {
		return icon;
	}

	@Override
	public String toString() {
		return id;
	}

	public int getIndex() {
		return file.chapterGroups.indexOf(this);
	}

	public boolean isDefaultGroup() {
		return this == file.defaultChapterGroup;
	}

	public Icon getIcon() {
		if (!getIconItem().isEmpty()) {
			return CustomIconItem.getIcon(getIconItem());
		}

		List<Icon> list = new ArrayList<>();

		for (Chapter chapter : chapters) {
			list.add(chapter.getIcon());
		}

		return IconAnimation.fromList(list, false);
	}

	public boolean isVisible(PlayerData data) {
		for (Chapter chapter : chapters) {
			if (chapter.isVisible(data)) {
				return true;
			}
		}

		return false;
	}

	public void changeProgress(PlayerData data, ChangeProgress type) {
		for (Chapter chapter : chapters) {
			chapter.changeProgress(data, type);
		}
	}

	public List<Chapter> getVisibleChapters(PlayerData data) {
		if (file.canEdit()) {
			return chapters;
		}

		List<Chapter> list = new ArrayList<>();

		for (Chapter chapter : chapters) {
			if (!chapter.quests.isEmpty() && chapter.isVisible(data)) {
				list.add(chapter);
			}
		}

		return list;
	}
}
