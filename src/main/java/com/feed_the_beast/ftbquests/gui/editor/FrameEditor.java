package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

/**
 * @author LatvianModder
 */
public final class FrameEditor extends JFrame
{
	private static FrameEditor editor = null;

	public static void open(boolean reload)
	{
		if (editor == null || reload)
		{
			if (editor != null)
			{
				editor.dispose();
			}

			editor = new FrameEditor();
			editor.setLocationRelativeTo(null);
		}

		editor.setVisible(true);
		editor.setLocationRelativeTo(null);
	}

	private FrameEditor()
	{
		super("FTB Quests Editor");
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setMinimumSize(new Dimension(500, 300));
		setResizable(true);

		JMenuBar menuBar = new JMenuBar();
		JMenu menuSettings = new JMenu("Settings");
		menuSettings.add(menuItem(I18n.format("ftbquests.gui.edit_file"), GuiIcons.SETTINGS, () -> System.out.println("Hi")));
		menuSettings.add(menuItem(I18n.format("ftbquests.gui.reset_progress"), GuiIcons.REFRESH, () -> System.out.println("Hi")));
		menuSettings.add(menuItem(I18n.format("ftbquests.gui.complete_instantly"), GuiIcons.ACCEPT, () -> System.out.println("Hi")));
		menuSettings.add(menuItem(I18n.format("ftbquests.variables"), GuiIcons.CONTROLLER, () -> System.out.println("Hi")));
		menuSettings.add(menuItem(I18n.format("ftbquests.reward_tables"), GuiIcons.MONEY_BAG, () -> System.out.println("Hi")));
		menuSettings.add(menuItem(I18n.format("ftbquests.gui.save_as_file"), GuiIcons.DOWN, () -> System.out.println("Hi")));
		menuBar.add(menuSettings);
		setJMenuBar(menuBar);

		JTabbedPane tabs = new JTabbedPane();

		for (QuestChapter chapter : ClientQuestFile.INSTANCE.chapters)
		{
			JPanel panel = new JPanel();
			tabs.addTab(chapter.getDisplayName().getUnformattedText(), panel);
		}

		setContentPane(tabs);
		pack();
	}

	private JMenuItem menuItem(String title, @Nullable com.feed_the_beast.ftblib.lib.icon.Icon icon, Runnable action)
	{
		JMenuItem item = new JMenuItem(title);
		Icon icon1 = IconWrapper.from(icon);

		if (icon1 != null)
		{
			item.setIcon(icon1);
		}

		item.addActionListener(e -> action.run());

		return item;
	}
}