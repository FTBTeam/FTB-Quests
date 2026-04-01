package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.platform.Env;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.DeleteObjectMessage;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.task.StructureTask;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.util.TextUtils;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.client.KnownClientPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

// NOTE: don't import anything from net.minecraft.client into this class
// ClientQuestFile.exists() can theoretically be called from either side
public class ClientQuestFile extends BaseQuestFile {
	private static final List<String> MISSING_DATA_ERR = List.of(
			"Unable to open Quest GUI: no quest book data received from server!",
			"- Check that FTB Quests and FTB Teams are installed on the server",
			"  and that no server-side errors were logged when you connected."
	);

	@Nullable
	private static ClientQuestFile INSTANCE;

	public TeamData selfTeamData = TeamData.NONE;  // TeamData for the player on this client
	@Nullable
	private QuestScreen questScreen;
	private QuestScreen.@Nullable PersistedData persistedData;
	private boolean editorPermission;

	public static boolean exists() {
		return INSTANCE != null && !INSTANCE.invalid;
	}

	public static ClientQuestFile getInstance() {
		return Objects.requireNonNull(INSTANCE);
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
		selfTeamData = TeamData.NONE;

		refreshGui();
		FTBQuests.getRecipeModHelper().refreshRecipes(this);
	}

	@Override
	public boolean canEdit() {
        return hasEditorPermission() && ClientUtils.getOptionalClientPlayer()
				.map(player -> selfTeamData.isValid() && selfTeamData.getCanEdit(player))
				.orElse(false);
	}

	@Override
	public void refreshGui() {
		clearCachedData();

		if (questScreen != null) {
			persistedData = questScreen.getPersistedScreenData();
			if (ClientUtils.getCurrentGuiAs(QuestScreen.class) != null) {
				questScreen = QuestScreen.reopen(this, persistedData);
			}
		}
	}

	public Optional<QuestScreen> getQuestScreen() {
		return Optional.ofNullable(questScreen);
	}

	@Nullable
	public static QuestScreen openGui() {
		if (INSTANCE != null) {
			return INSTANCE.openQuestGui();
		} else {
			ClientUtils.getOptionalClientPlayer().ifPresent(player ->
					MISSING_DATA_ERR.forEach(s -> player.sendSystemMessage(Component.literal(s).withStyle(ChatFormatting.RED)))
			);
			return null;
		}
	}

	@Nullable
	public static QuestScreen openGui(Quest quest, boolean focused) {
		QuestScreen screen = openGui();
		if (screen != null) screen.open(quest, focused);
		return screen;
	}

	@Nullable
	private QuestScreen openQuestGui() {
		if (exists() && selfTeamData.isValid()) {
			if (isDisableGui() && !canEdit()) {
				FTBQuestsClient.showErrorToast(Component.translatable("item.ftbquests.book.disabled"));
			} else if (selfTeamData.isLocked()) {
				Component msg = lockMessage.isEmpty() ? Component.literal("Quests locked!") : TextUtils.parseRawText(lockMessage, holderLookup());
				FTBQuestsClient.showErrorToast(msg);
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
	public HolderLookup.Provider holderLookup() {
		return FTBQuestsClient.holderLookup();
	}

	@Override
	public void deleteObject(long id) {
		Play2ServerNetworking.send(new DeleteObjectMessage(id));
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();

		QuestTheme.getInstance().clearCache();
	}
	
	@Override
	public Optional<TeamData> getTeamData(Player player) {
		KnownClientPlayer kcp = FTBTeamsAPI.api().getClientManager().getKnownPlayer(player.getUUID())
				.orElseThrow(() -> new RuntimeException("Unknown client player " + player.getUUID()));

		return Optional.of(kcp.id().equals(ClientUtils.getClientPlayer().getUUID()) ? selfTeamData : getOrCreateTeamData(kcp.teamId()));
	}

	public void setPersistedScreenInfo(QuestScreen.PersistedData persistedData) {
		this.persistedData = persistedData;
	}

	public static boolean canClientPlayerEdit() {
		return exists() && INSTANCE.selfTeamData.getCanEdit(ClientUtils.getClientPlayer());
	}

	public static boolean isQuestPinned(long id) {
		return exists() && INSTANCE.selfTeamData.isQuestPinned(ClientUtils.getClientPlayer(), id);
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

	@Override
	public String getLocale() {
		String locale = FTBQuestsClientConfig.EDITING_LOCALE.get();
		return locale.isEmpty() ? ClientUtils.getCurrentLanguageCode() : locale;
	}

	@Override
	public String getFallbackLocale() {
		String fallback = FTBQuestsClientConfig.FALLBACK_LOCALE.get();
		return fallback.isEmpty() ? super.getFallbackLocale() : fallback;
	}

	public void setEditorPermission(boolean hasPermission) {
		editorPermission = hasPermission;
	}

	public boolean hasEditorPermission() {
		return editorPermission;
	}

	public static void openBookToQuestObject(long id) {
		if (exists()) {
			ClientQuestFile file = ClientQuestFile.getInstance();
			if (file.questScreen == null) {
				ClientQuestFile.getInstance().openQuestGui();
			}
			if (file.questScreen != null) {
				if (id != 0L) {
					QuestObject qo = file.get(id);
					if (qo != null) {
						file.questScreen.open(qo, true);
					}
				} else {
					file.questScreen.openGui();
				}
			}
		}
	}

	public static void addTranslationWarning(TooltipList list, TranslationKey key) {
		list.add(Component.translatable("ftbquests.message.missing_xlate_1",
						Component.translatable(key.getTranslationKey()),
						ClientQuestFile.getInstance().getLocale())
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
		);
		list.add(Component.translatable("ftbquests.message.missing_xlate_2", INSTANCE.getFallbackLocale())
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
	}

	public boolean isChapterSelected(Chapter chapter) {
		return getQuestScreen().map(screen -> screen.isChapterSelected(chapter)).orElse(false);
	}
}
