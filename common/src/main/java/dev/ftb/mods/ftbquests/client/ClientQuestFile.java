package dev.ftb.mods.ftbquests.client;

import dev.architectury.utils.Env;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.util.ClientUtils;
import dev.ftb.mods.ftbquests.gui.CustomToast;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.FTBQuestsJEIHelper;
import dev.ftb.mods.ftbquests.net.DeleteObjectMessage;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.StructureTask;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import dev.ftb.mods.ftbquests.util.TextUtils;
import dev.ftb.mods.ftbteams.data.ClientTeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

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
		return self.getCanEdit();
	}

	@Override
	public void refreshGui() {
		clearCachedData();

		boolean guiOpen = false;
		if (questScreen != null) {
			persistedData = questScreen.getPersistedScreenData();
			if (ClientUtils.getCurrentGuiAs(QuestScreen.class) != null) {
				guiOpen = true;
			}
		}

		if (guiOpen) {
			Minecraft.getInstance().setScreen(null);  // ensures prevScreen is null, so we can close correctly
		}

		questScreen = new QuestScreen(this, persistedData);

		if (guiOpen) {
			questScreen.openGui();
		}
	}

	public static void openGui() {
		if (INSTANCE != null) {
			INSTANCE.openQuestGui();
		} else {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				MISSING_DATA_ERR.forEach(s -> player.displayClientMessage(Component.literal(s).withStyle(ChatFormatting.RED), false));
			}
		}
	}

	private void openQuestGui() {
		if (exists()) {
			if (disableGui && !canEdit()) {
				Minecraft.getInstance().getToasts().addToast(new CustomToast(Component.translatable("item.ftbquests.book.disabled"), Icons.BARRIER, Component.empty()));
			} else if (self.isLocked()) {
				Minecraft.getInstance().getToasts().addToast(new CustomToast(lockMessage.isEmpty() ? Component.literal("Quests locked!") : TextUtils.parseRawText(lockMessage), Icons.BARRIER, Component.empty()));
			} else {
				if (self.getCanEdit()) {
					StructureTask.maybeRequestStructureSync();
				}
				questScreen = new QuestScreen(this, persistedData);
				questScreen.openGui();
				questScreen.refreshWidgets();
			}
		}
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
}
