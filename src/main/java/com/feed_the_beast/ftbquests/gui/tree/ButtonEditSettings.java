package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.FTBQuestsTheme;
import com.feed_the_beast.ftbquests.gui.GuiRewardTables;
import com.feed_the_beast.ftbquests.gui.GuiVariables;
import com.feed_the_beast.ftbquests.net.MessageCompleteInstantly;
import com.feed_the_beast.ftbquests.net.MessageResetProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

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
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.edit_file"), GuiIcons.SETTINGS, treeGui.questFile::onEditButtonClicked));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.reset_progress"), GuiIcons.REFRESH, () -> new MessageResetProgress(treeGui.questFile.id).sendToServer()).setYesNo(I18n.format("ftbquests.gui.reset_progress_q")));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.complete_instantly"), FTBQuestsTheme.COMPLETED, () -> new MessageCompleteInstantly(treeGui.questFile.id).sendToServer()).setYesNo(I18n.format("ftbquests.gui.complete_instantly_q")));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.variables"), GuiIcons.CONTROLLER, () -> new GuiVariables().openGui()));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.reward_tables"), GuiIcons.MONEY_BAG, () -> new GuiRewardTables().openGui()));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.save_as_file"), GuiIcons.DOWN, () -> {
			try
			{
				NBTTagCompound nbt = new NBTTagCompound();
				ClientQuestFile.INSTANCE.writeDataFull(nbt);
				Calendar time = Calendar.getInstance();
				StringBuilder fileName = new StringBuilder("local/ftbquests/saved/");
				appendNum(fileName, time.get(Calendar.YEAR), '-');
				appendNum(fileName, time.get(Calendar.MONTH) + 1, '-');
				appendNum(fileName, time.get(Calendar.DAY_OF_MONTH), '-');
				appendNum(fileName, time.get(Calendar.HOUR_OF_DAY), '-');
				appendNum(fileName, time.get(Calendar.MINUTE), '-');
				appendNum(fileName, time.get(Calendar.SECOND), '\0');
				fileName.append(".nbt");
				File file = new File(Minecraft.getMinecraft().gameDir, fileName.toString());
				NBTUtils.writeNBT(file, nbt);
				Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation("ftbquests.gui.saved_as_file", fileName.toString()));
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}));

		Panel panel = gui.openContextMenu(contextMenu);
		panel.setPos(gui.width - panel.width - 2, height + 1);
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