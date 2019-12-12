package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiRewardTables;
import com.feed_the_beast.ftbquests.net.MessageChangeProgress;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.theme.ThemeLoader;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonEditSettings extends ButtonTab
{
	public ButtonEditSettings(Panel panel)
	{
		super(panel, I18n.format("gui.settings"), ThemeProperties.SETTINGS_ICON.get());
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();

		if (treeGui.contextMenu != null)
		{
			treeGui.closeContextMenu();
			return;
		}

		List<ContextMenuItem> contextMenu = new ArrayList<>();
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.edit_file"), ThemeProperties.SETTINGS_ICON.get(), treeGui.file::onEditButtonClicked));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.reset_progress"), ThemeProperties.RELOAD_ICON.get(), () -> new MessageChangeProgress(ClientQuestFile.INSTANCE.self.uuid, treeGui.file.id, ChangeProgress.RESET).sendToServer()).setYesNo(new TranslationTextComponent("ftbquests.gui.reset_progress_q")));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.complete_instantly"), ThemeProperties.CHECK_ICON.get(), () -> new MessageChangeProgress(ClientQuestFile.INSTANCE.self.uuid, treeGui.file.id, ChangeProgress.COMPLETE).sendToServer()).setYesNo(new TranslationTextComponent("ftbquests.gui.complete_instantly_q")));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.reward_tables"), ThemeProperties.REWARD_TABLE_ICON.get(), () -> new GuiRewardTables().openGui()));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.save_as_file"), ThemeProperties.DOWNLOAD_ICON.get(), () -> {
			try
			{
				Calendar time = Calendar.getInstance();
				StringBuilder fileName = new StringBuilder("local/ftbquests/saved/");
				appendNum(fileName, time.get(Calendar.YEAR), '-');
				appendNum(fileName, time.get(Calendar.MONTH) + 1, '-');
				appendNum(fileName, time.get(Calendar.DAY_OF_MONTH), '-');
				appendNum(fileName, time.get(Calendar.HOUR_OF_DAY), '-');
				appendNum(fileName, time.get(Calendar.MINUTE), '-');
				appendNum(fileName, time.get(Calendar.SECOND), '\0');
				File file = new File(Minecraft.getInstance().gameDir, fileName.toString()).getCanonicalFile();
				ClientQuestFile.INSTANCE.writeDataFull(file);
				ITextComponent component = new TranslationTextComponent("ftbquests.gui.saved_as_file", file.getPath().replace(Minecraft.getInstance().gameDir.getCanonicalFile().getAbsolutePath(), ""));
				component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()));
				Minecraft.getInstance().player.sendMessage(component);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}));

		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.reload_theme"), ThemeProperties.RELOAD_ICON.get(), () -> {
			Minecraft mc = Minecraft.getInstance();
			//FIXME: mc.getTextureManager().onResourceManagerReload(mc.getResourceManager());
			ThemeLoader.loadTheme(mc.getResourceManager());
			ClientQuestFile.INSTANCE.refreshGui();
		}));

		treeGui.openContextMenu(contextMenu);
	}

	private void appendNum(StringBuilder sb, int num, char c)
	{
		if (num < 10)
		{
			sb.append('0');
		}
		sb.append(num);
		if (c != '\0')
		{
			sb.append(c);
		}
	}
}