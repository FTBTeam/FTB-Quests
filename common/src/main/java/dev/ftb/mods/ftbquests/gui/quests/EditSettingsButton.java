package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.gui.RewardTablesScreen;
import dev.ftb.mods.ftbquests.net.ChangeProgressMessage;
import dev.ftb.mods.ftbquests.net.ForceSaveMessage;
import dev.ftb.mods.ftbquests.quest.theme.ThemeLoader;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author LatvianModder
 */
public class EditSettingsButton extends TabButton {
	public EditSettingsButton(Panel panel) {
		super(panel, new TranslatableComponent("gui.settings"), ThemeProperties.SETTINGS_ICON.get());
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();

		if (questScreen.contextMenu != null) {
			questScreen.closeContextMenu();
			return;
		}

		List<ContextMenuItem> contextMenu = new ArrayList<>();
		contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.edit_file"), ThemeProperties.SETTINGS_ICON.get(), () -> questScreen.file.onEditButtonClicked(this)));

		contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.reset_progress"), ThemeProperties.RELOAD_ICON.get(), () -> ChangeProgressMessage.send(questScreen.file.self, questScreen.file, progressChange -> {
			progressChange.reset = true;
		})).setYesNo(new TranslatableComponent("ftbquests.gui.reset_progress_q")));

		contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.complete_instantly"), ThemeProperties.CHECK_ICON.get(), () -> ChangeProgressMessage.send(questScreen.file.self, questScreen.file, progressChange -> {
			progressChange.reset = false;
		})).setYesNo(new TranslatableComponent("ftbquests.gui.complete_instantly_q")));

		contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.reward_tables"), ThemeProperties.REWARD_TABLE_ICON.get(), () -> new RewardTablesScreen().openGui()));
		contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.save_on_server"), ThemeProperties.SAVE_ICON.get(), () -> new ForceSaveMessage().sendToServer()));
		contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.save_as_file"), ThemeProperties.DOWNLOAD_ICON.get(), () -> {
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
				Component component = new TranslatableComponent("ftbquests.gui.saved_as_file", "." + file.getPath().replace(Minecraft.getInstance().gameDirectory.getCanonicalFile().getAbsolutePath(), ""));
				component.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()));
				Minecraft.getInstance().player.sendMessage(component, Util.NIL_UUID);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}));

		contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.reload_theme"), ThemeProperties.RELOAD_ICON.get(), () -> {
			Minecraft mc = Minecraft.getInstance();
			//FIXME: mc.getTextureManager().onResourceManagerReload(mc.getResourceManager());
			ThemeLoader.loadTheme(mc.getResourceManager());
			ClientQuestFile.INSTANCE.refreshGui();
		}));

		contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.wiki"), Icons.INFO, () -> handleClick("https://help.ftb.team/mods")));

		questScreen.openContextMenu(contextMenu);
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
