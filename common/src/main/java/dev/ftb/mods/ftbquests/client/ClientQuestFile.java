package dev.ftb.mods.ftbquests.client;

import dev.architectury.utils.Env;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.util.ClientUtils;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.gui.CustomToast;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.FTBQuestsJEIHelper;
import dev.ftb.mods.ftbquests.net.DeleteObjectMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.StructureTask;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import dev.ftb.mods.ftbquests.util.TextUtils;
import dev.ftb.mods.ftbteams.data.ClientTeamManager;
import dev.ftb.mods.ftbteams.data.KnownClientPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Objects;

/**
 * @author LatvianModder
 */
public class ClientQuestFile extends QuestFile {
	private static final List<String> MISSING_DATA_ERR = List.of(
			"Unable to open Quest GUI: no quest book data received from server!",
			"- Check that FTB Quests and FTB Teams are installed on the server",
			"  and that no server-side errors were logged when you connected."
	);

	public static ClientQuestFile INSTANCE;

	public static boolean exists() {
		return INSTANCE != null && !INSTANCE.invalid;
	}

	public TeamData self;
	public QuestScreen questScreen;
	private QuestScreen.PersistedData persistedData;

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
		return self.getCanEdit(Minecraft.getInstance().player);
	}

	@Override
	public void refreshGui() {
		clearCachedData();

		if (questScreen != null) {
			persistedData = questScreen.getPersistedScreenData();
			if (ClientUtils.getCurrentGuiAs(QuestScreen.class) != null) {
				Minecraft.getInstance().setScreen(null);  // ensures prevScreen is null, so we can close correctly
				questScreen = new QuestScreen(this, persistedData);
				questScreen.openGui();
			}
		}
	}

	public static QuestScreen openGui() {
		if (INSTANCE != null) {
			return INSTANCE.openQuestGui();
		} else {
			Player player = Minecraft.getInstance().player;
			if (player != null) {
				MISSING_DATA_ERR.forEach(s -> player.displayClientMessage(Component.literal(s).withStyle(ChatFormatting.RED), false));
			}
			return null;
		}
	}

	public static QuestScreen openGui(Quest quest, boolean focused) {
		QuestScreen screen = openGui();
		if (screen != null) screen.open(quest, focused);
		return screen;
	}

	private QuestScreen openQuestGui() {
		if (exists()) {
			if (disableGui && !canEdit()) {
				Minecraft.getInstance().getToasts().addToast(new CustomToast(Component.translatable("item.ftbquests.book.disabled"), Icons.BARRIER, Component.empty()));
			} else if (self.isLocked()) {
				Minecraft.getInstance().getToasts().addToast(new CustomToast(lockMessage.isEmpty() ? Component.literal("Quests locked!") : TextUtils.parseRawText(lockMessage), Icons.BARRIER, Component.empty()));
			} else {
				if (canEdit()) {
					StructureTask.maybeRequestStructureSync();
				}
				questScreen = new QuestScreen(this, persistedData);
				questScreen.openGui();
				questScreen.refreshWidgets();
				return questScreen;
			}
		}
		return null;
	}

	@Override
	public Env getSide() {
		return Env.CLIENT;
	}

	@Override
	public void deleteObject(long id) {
		new DeleteObjectMessage(id).sendToServer();
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();
		QuestTheme.instance.clearCache();
	}

	@Override
	public TeamData getData(Entity player) {
		return player == Minecraft.getInstance().player ? self : getData(Objects.requireNonNull(ClientTeamManager.INSTANCE.getKnownPlayer(player.getUUID()), "Non-null team required!").teamId);
	}

	public void setPersistedScreenInfo(QuestScreen.PersistedData persistedData) {
		this.persistedData = persistedData;
	}

	public static boolean canClientPlayerEdit() {
		return exists() && INSTANCE.self.getCanEdit(FTBQuests.PROXY.getClientPlayer());
	}

	public static boolean isQuestPinned(long id) {
		return exists() && INSTANCE.self.isQuestPinned(FTBQuests.PROXY.getClientPlayer(), id);
	}

	@Override
	public boolean isPlayerOnTeam(Player player, TeamData teamData) {
		KnownClientPlayer knownPlayer = ClientTeamManager.INSTANCE.getKnownPlayer(player.getUUID());
		return knownPlayer != null && knownPlayer.teamId.equals(teamData.uuid);
	}
}
