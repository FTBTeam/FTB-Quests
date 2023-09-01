package dev.ftb.mods.ftbquests.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.utils.Env;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.gui.CustomToast;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.DeleteObjectMessage;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.StructureTask;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import dev.ftb.mods.ftbquests.util.TextUtils;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.client.KnownClientPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

public class ClientQuestFile extends BaseQuestFile {
	private static final List<String> MISSING_DATA_ERR = List.of(
			"Unable to open Quest GUI: no quest book data received from server!",
			"- Check that FTB Quests and FTB Teams are installed on the server",
			"  and that no server-side errors were logged when you connected."
	);

	public static ClientQuestFile INSTANCE;

	public TeamData selfTeamData;  // TeamData for the player on this client

	private QuestScreen questScreen;
	private QuestScreen.PersistedData persistedData;
	private boolean editorPermission;

	public static boolean exists() {
		return INSTANCE != null && !INSTANCE.invalid;
	}

	public static void syncFromServer(BaseQuestFile newInstance) {
		if (!(newInstance instanceof ClientQuestFile clientInstance)) {
			throw new IllegalArgumentException("need a client quest file instance!");
		}

		if (INSTANCE != null) {
			// clean up the previous instance
			INSTANCE.deleteChildren();
			INSTANCE.deleteSelf();
		}

		INSTANCE = clientInstance;
		INSTANCE.onReplaced();
	}

	private void onReplaced() {
		selfTeamData = new TeamData(Util.NIL_UUID, INSTANCE, "Loading...");
		selfTeamData.setLocked(true);

		refreshGui();
		FTBQuests.getRecipeModHelper().refreshRecipes(INSTANCE);
	}

	@Override
	public boolean canEdit() {
		return hasEditorPermission() && selfTeamData.getCanEdit(Minecraft.getInstance().player);
	}

	@Override
	public void refreshGui() {
		clearCachedData();

		if (questScreen != null) {
			persistedData = questScreen.getPersistedScreenData();
			if (ClientUtils.getCurrentGuiAs(QuestScreen.class) != null) {
				double mx = Minecraft.getInstance().mouseHandler.xpos();
				double my = Minecraft.getInstance().mouseHandler.ypos();
				Minecraft.getInstance().setScreen(null);  // ensures prevScreen is null, so we can close correctly
				questScreen = new QuestScreen(this, persistedData);
				questScreen.openGui();
				InputConstants.grabOrReleaseMouse(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR_NORMAL, mx, my);
			}
		}
	}

	public Optional<QuestScreen> getQuestScreen() {
		return Optional.ofNullable(questScreen);
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
			if (isDisableGui() && !canEdit()) {
				Minecraft.getInstance().getToasts().addToast(new CustomToast(Component.translatable("item.ftbquests.book.disabled"), Icons.BARRIER, Component.empty()));
			} else if (selfTeamData.isLocked()) {
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
	public TeamData getOrCreateTeamData(Entity player) {
		KnownClientPlayer kcp = FTBTeamsAPI.api().getClientManager().getKnownPlayer(player.getUUID())
				.orElseThrow(() -> new RuntimeException("Unknown client player " + player.getUUID()));
		return kcp.id().equals(Minecraft.getInstance().player.getUUID()) ? selfTeamData : getOrCreateTeamData(kcp.teamId());
	}

	public void setPersistedScreenInfo(QuestScreen.PersistedData persistedData) {
		this.persistedData = persistedData;
	}

	public static boolean canClientPlayerEdit() {
		return exists() && INSTANCE.selfTeamData.getCanEdit(FTBQuestsClient.getClientPlayer());
	}

	public static boolean isQuestPinned(long id) {
		return exists() && INSTANCE.selfTeamData.isQuestPinned(FTBQuestsClient.getClientPlayer(), id);
	}

	@Override
	public boolean isPlayerOnTeam(Player player, TeamData teamData) {
		return FTBTeamsAPI.api().getClientManager().getKnownPlayer(player.getUUID())
				.map(kcp -> kcp.teamId().equals(teamData.getTeamId()))
				.orElse(false);
	}

	@Override
	public boolean moveChapterGroup(long id, boolean movingUp) {
		if (super.moveChapterGroup(id, movingUp)) {
			clearCachedData();
			QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
			if (gui != null) {
				gui.refreshChapterPanel();
			}
			return true;
		}
		return false;
	}

	public void setEditorPermission(boolean hasPermission) {
		editorPermission = hasPermission;
	}

	public boolean hasEditorPermission() {
		return editorPermission;
	}
}
