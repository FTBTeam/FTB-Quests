package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.FTBLibConfig;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.GuiVariables;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
import com.feed_the_beast.ftbquests.net.MessageCompleteInstantly;
import com.feed_the_beast.ftbquests.net.MessageResetProgress;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonEditSettings extends ButtonTab
{
	public ButtonEditSettings(Panel panel)
	{
		super(panel, I18n.format("gui.settings"), GuiIcons.SETTINGS);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		GuiBase gui = getGui();

		if (gui.contextMenu != null)
		{
			gui.closeContextMenu();
			return;
		}

		List<ContextMenuItem> contextMenu = new ArrayList<>();
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.edit_file"), GuiIcons.SETTINGS, () -> new MessageEditObject(treeGui.questFile.getID()).sendToServer()));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.reset_progress"), GuiIcons.REFRESH, () -> new MessageResetProgress(treeGui.questFile.getID()).sendToServer()).setYesNo(I18n.format("ftbquests.gui.reset_progress_q")));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.complete_instantly"), QuestsTheme.COMPLETED, () -> new MessageCompleteInstantly(treeGui.questFile.getID()).sendToServer()).setYesNo(I18n.format("ftbquests.gui.complete_instantly_q")));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.variables"), GuiIcons.CONTROLLER, () -> new GuiVariables().openGui()));

		if (FTBLibConfig.debugging.gui_widget_bounds)
		{
			contextMenu.add(new ContextMenuItem("Reload GUI", GuiIcons.REFRESH, treeGui.questFile::refreshGui));
		}

		Panel panel = gui.openContextMenu(contextMenu);
		panel.setPos(gui.width - panel.width - 2, height + 1);
	}
}