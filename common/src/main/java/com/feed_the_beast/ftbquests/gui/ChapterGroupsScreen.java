package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.quests.QuestScreen;
import com.feed_the_beast.ftbquests.net.MessageCreateObject;
import com.feed_the_beast.ftbquests.quest.ChapterGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigString;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.GuiButtonListBase;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ChapterGroupsScreen extends GuiButtonListBase implements QuestObjectUpdateListener {
	private class ChapterGroupButton extends SimpleTextButton {
		private final ChapterGroup chaperGroup;

		public ChapterGroupButton(Panel panel, ChapterGroup t) {
			super(panel, t.getTitle(), Icon.EMPTY);
			chaperGroup = t;
			setHeight(14);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			QuestScreen.addObjectMenuItems(contextMenu, ChapterGroupsScreen.this, chaperGroup);
			getGui().openContextMenu(contextMenu);
		}
	}

	public ChapterGroupsScreen() {
		setTitle(new TranslatableComponent("ftbquests.chapter_groups"));
		setHasSearchBox(true);
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel) {
		SimpleTextButton button = new SimpleTextButton(panel, new TranslatableComponent("gui.add"), GuiIcons.ADD) {
			@Override
			public void onClicked(MouseButton button) {
				playClickSound();
				ConfigString c = new ConfigString();
				GuiEditConfigFromString.open(c, "", "", accepted -> {
					if (accepted) {
						ChapterGroup group = new ChapterGroup(ClientQuestFile.INSTANCE);
						group.title = c.value;
						new MessageCreateObject(group, null).sendToServer();
					}

					openGui();
				});
			}
		};

		button.setHeight(14);
		panel.add(button);

		for (ChapterGroup group : ClientQuestFile.INSTANCE.chapterGroups) {
			if (!group.isDefaultGroup()) {
				panel.add(new ChapterGroupButton(panel, group));
			}
		}
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	@Override
	public void onQuestObjectUpdate(Object o) {
		refreshWidgets();
	}
}