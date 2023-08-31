package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.CustomToast;
import dev.ftb.mods.ftbquests.client.gui.RewardTablesScreen;
import dev.ftb.mods.ftbquests.net.ChangeProgressMessage;
import dev.ftb.mods.ftbquests.net.ForceSaveMessage;
import dev.ftb.mods.ftbquests.net.ToggleEditingModeMessage;
import dev.ftb.mods.ftbquests.quest.task.StructureTask;
import dev.ftb.mods.ftbquests.quest.theme.ThemeLoader;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OtherButtonsPanelBottom extends OtherButtonsPanel {
	public OtherButtonsPanelBottom(Panel panel) {
		super(panel);
	}

	@Override
	public void addWidgets() {
		if (questScreen.file.canEdit()) {
			add(new EditSettingsButton(this));
		}

		if (FTBQuestsClient.getClientPlayer().hasPermissions(2) || ClientQuestFile.INSTANCE.hasEditorPermission()) {
			// note: single player owner can't use the GUI button but can use the /ftbquests editing_mode command
			// this is intentional, since there should not be an obvious "cheat" button for single player questing
			add(new ToggleEditModeButton(this));
		}
	}

	@Override
	public void alignWidgets() {
		setHeight(align(WidgetLayout.VERTICAL));
		setPos(questScreen.width - width, questScreen.height - height - 1);
	}

	/**
	 * Toggle editing mode
	 */
	public static class ToggleEditModeButton extends TabButton {
		public ToggleEditModeButton(Panel panel) {
			super(panel, makeTooltip(), ClientQuestFile.canClientPlayerEdit() ? ThemeProperties.EDITOR_ICON_ON.get() : ThemeProperties.EDITOR_ICON_OFF.get());
		}

		private static Component makeTooltip() {
			String key = ClientQuestFile.canClientPlayerEdit() ? "commands.ftbquests.editing_mode.enabled" : "commands.ftbquests.editing_mode.disabled";
			return Component.translatable(key, ClientQuestFile.INSTANCE.selfTeamData.getName());
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();

			if (!questScreen.file.selfTeamData.getCanEdit(Minecraft.getInstance().player)) {
				StructureTask.maybeRequestStructureSync();
			}

			new ToggleEditingModeMessage().sendToServer();
		}
	}

	/**
	 * Settings editor
	 */
	public static class EditSettingsButton extends TabButton {
		public EditSettingsButton(Panel panel) {
			super(panel, Component.translatable("gui.settings"), ThemeProperties.SETTINGS_ICON.get());
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();

			if (questScreen.getContextMenu().isPresent()) {
				questScreen.closeContextMenu();
				return;
			}

			List<ContextMenuItem> contextMenu = new ArrayList<>();
			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.edit_file"), ThemeProperties.SETTINGS_ICON.get(),
					() -> questScreen.file.onEditButtonClicked(this)));

			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.reset_progress"), ThemeProperties.RELOAD_ICON.get(),
					() -> ChangeProgressMessage.sendToServer(questScreen.file.selfTeamData, questScreen.file, progressChange -> progressChange.setReset(true)))
					.setYesNoText(Component.translatable("ftbquests.gui.reset_progress_q")));
			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.complete_instantly"), ThemeProperties.CHECK_ICON.get(),
					() -> ChangeProgressMessage.sendToServer(questScreen.file.selfTeamData, questScreen.file, progressChange -> progressChange.setReset(false)))
					.setYesNoText(Component.translatable("ftbquests.gui.complete_instantly_q")));

			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.reward_tables"), ThemeProperties.REWARD_TABLE_ICON.get(),
					() -> new RewardTablesScreen(questScreen).openGui()));
			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.save_on_server"), ThemeProperties.SAVE_ICON.get(),
					() -> new ForceSaveMessage().sendToServer()));
			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.save_as_file"), ThemeProperties.DOWNLOAD_ICON.get(),
					this::saveLocally));

			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.reload_theme"), ThemeProperties.RELOAD_ICON.get(),
					this::reload_theme));
			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.wiki"), Icons.INFO,
					() -> handleClick("https://help.ftb.team/mods")));

			questScreen.openContextMenu(contextMenu);
		}

		private void reload_theme() {
			Minecraft mc = Minecraft.getInstance();
			//FIXME: mc.getTextureManager().onResourceManagerReload(mc.getResourceManager());
			ThemeLoader.loadTheme(mc.getResourceManager());
			ClientQuestFile.INSTANCE.refreshGui();
			Minecraft.getInstance().getToasts().addToast(new CustomToast(Component.translatable("ftbquests.gui.reload_theme"), Icons.ACCEPT, Component.translatable("gui.done")));
		}

		private void saveLocally() {
			try {
				Calendar time = Calendar.getInstance();
				StringBuilder fileName = new StringBuilder("local/ftbquests/saved/");
				appendNum(fileName, time.get(Calendar.YEAR), '-');
				appendNum(fileName, time.get(Calendar.MONTH) + 1, '-');
				appendNum(fileName, time.get(Calendar.DAY_OF_MONTH), '-');
				appendNum(fileName, time.get(Calendar.HOUR_OF_DAY), '-');
				appendNum(fileName, time.get(Calendar.MINUTE), '-');
				appendNum(fileName, time.get(Calendar.SECOND), '\0');
				File file = new File(Minecraft.getInstance().gameDirectory, fileName.toString()).getCanonicalFile();
				ClientQuestFile.INSTANCE.writeDataFull(file.toPath());
				Component component = Component.translatable("ftbquests.gui.saved_as_file", "." + file.getPath().replace(Minecraft.getInstance().gameDirectory.getCanonicalFile().getAbsolutePath(), ""));
				component.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()));
				Minecraft.getInstance().player.sendSystemMessage(component);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		private void appendNum(StringBuilder sb, int num, char c) {
			if (num < 10) {
				sb.append('0');
			}
			sb.append(num);
			if (c != '\0') {
				sb.append(c);
			}
		}
	}
}
