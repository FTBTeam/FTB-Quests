package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftbguilibrary.utils.ClientUtils;
import dev.ftb.mods.ftbguilibrary.widget.BaseScreen;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.jei.FTBQuestsJEIHelper;
import dev.ftb.mods.ftbquests.net.MessageDeleteObject;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import me.shedaniel.architectury.utils.Env;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

import java.util.Objects;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ClientQuestFile extends QuestFile {
	public static ClientQuestFile INSTANCE;

	public static boolean exists() {
		return INSTANCE != null && !INSTANCE.invalid;
	}

	public TeamData self;
	public QuestScreen questScreen;
	public BaseScreen questGui;

	@Override
	public void load(UUID s) {
		if (INSTANCE != null) {
			INSTANCE.deleteChildren();
			INSTANCE.deleteSelf();
		}

		self = Objects.requireNonNull(getData(s));
		self.name = Minecraft.getInstance().getUser().getName();
		INSTANCE = this;

		refreshGui();
		FTBQuestsJEIHelper.refresh(this);
	}

	@Override
	public boolean canEdit() {
		return self.getCanEdit();
	}

	@Override
	public void refreshGui() {
		clearCachedData();

		boolean hasPrev = false;
		boolean guiOpen = false;
		int zoom = 0;
		double scrollX = 0, scrollY = 0;
		long selectedChapter = 0L;
		long[] selectedQuests = new long[0];
		boolean chaptersExpanded = false;

		if (questScreen != null) {
			hasPrev = true;
			zoom = questScreen.zoom;
			scrollX = questScreen.questPanel.centerQuestX;
			scrollY = questScreen.questPanel.centerQuestY;
			selectedChapter = questScreen.selectedChapter == null ? 0L : questScreen.selectedChapter.id;
			selectedQuests = new long[questScreen.selectedObjects.size()];
			int i = 0;

			for (Movable m : questScreen.selectedObjects) {
				if (m instanceof Quest) {
					selectedQuests[i] = ((Quest) m).id;
				}

				i++;
			}

			if (ClientUtils.getCurrentGuiAs(QuestScreen.class) != null) {
				guiOpen = true;
			}

			chaptersExpanded = questScreen.chapterPanel.expanded;
		}

		questScreen = new QuestScreen(this);
		questGui = questScreen;

		if (hasPrev) {
			questScreen.zoom = zoom;
			questScreen.selectChapter(getChapter(selectedChapter));

			for (long i : selectedQuests) {
				Quest q = getQuest(i);

				if (q != null) {
					questScreen.selectedObjects.add(q);
				}
			}

			if (guiOpen) {
				questScreen.openGui();
			}
		}

		questScreen.refreshWidgets();

		if (hasPrev) {
			questScreen.questPanel.scrollTo(scrollX, scrollY);
		}

		questScreen.chapterPanel.setExpanded(chaptersExpanded);
	}

	public void openQuestGui() {
		if (disableGui && !self.getCanEdit()) {
			Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("item.ftbquests.book.disabled"), true);
		} else if (exists()) {
			questGui.openGui();
		}
	}

	@Override
	public Env getSide() {
		return Env.CLIENT;
	}

	@Override
	public void deleteObject(long id) {
		new MessageDeleteObject(id).sendToServer();
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();
		QuestTheme.instance.clearCache();
	}

	@Override
	public TeamData getData(Entity player) {
		return player == Minecraft.getInstance().player ? self : super.getData(player);
	}
}