package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.util.ClientUtils;
import dev.ftb.mods.ftbquests.gui.CustomToast;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.jei.FTBQuestsJEIHelper;
import dev.ftb.mods.ftbquests.net.DeleteObjectPacket;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import dev.ftb.mods.ftbteams.data.ClientTeamManager;
import me.shedaniel.architectury.utils.Env;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

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
	public void load() {
		if (INSTANCE != null) {
			INSTANCE.deleteChildren();
			INSTANCE.deleteSelf();
		}

		self = new TeamData(Util.NIL_UUID);
		self.file = this;
		self.name = "Loading...";
		self.setLocked(true);
		INSTANCE = this;

		refreshGui();
		FTBQuestsJEIHelper.refresh(this);
	}

	@Override
	public boolean canEdit() {
		return self.getCanEdit();
	}

	@Override
	public void setSelf(TeamData s) {
		self = s;
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
		if (exists()) {
			if (disableGui && !canEdit()) {
				Minecraft.getInstance().getToasts().addToast(new CustomToast(new TranslatableComponent("item.ftbquests.book.disabled"), Icons.BARRIER, TextComponent.EMPTY));
			} else if (self.isLocked()) {
				Minecraft.getInstance().getToasts().addToast(new CustomToast(lockMessage.isEmpty() ? new TextComponent("Quests locked!") : FTBQuestsClient.parse(lockMessage), Icons.BARRIER, TextComponent.EMPTY));
			} else {
				questGui.openGui();
			}
		}
	}

	@Override
	public Env getSide() {
		return Env.CLIENT;
	}

	@Override
	public void deleteObject(long id) {
		new DeleteObjectPacket(id).sendToServer();
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();
		QuestTheme.instance.clearCache();
	}

	@Override
	public TeamData getData(Entity player) {
		return player == Minecraft.getInstance().player ? self : getData(ClientTeamManager.INSTANCE.playerTeamMap.get(player.getUUID()));
	}
}