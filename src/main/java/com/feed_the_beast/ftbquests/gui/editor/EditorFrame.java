package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

/**
 * @author LatvianModder
 */
public final class EditorFrame extends JFrame
{
	private static EditorFrame editor = null;

	public static void open(boolean reload)
	{
		IconWrapper.clearCache();

		if (editor == null || reload)
		{
			if (editor != null)
			{
				editor.dispose();
			}

			try
			{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			}
			catch (Exception ex)
			{
			}

			editor = new EditorFrame(ClientQuestFile.INSTANCE);
			editor.setLocationRelativeTo(null);
		}

		editor.setVisible(true);
		editor.setLocationRelativeTo(null);
	}

	public final ClientQuestFile file;

	public static String toString(ITextComponent component)
	{
		return StringUtils.unformatted(component.getFormattedText());
	}

	public static String toString(ItemStack stack)
	{
		return StringUtils.unformatted(stack.getDisplayName());
	}

	private EditorFrame(ClientQuestFile f)
	{
		super("FTB Quests Editor");
		file = f;
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setMinimumSize(new Dimension(500, 300));
		setResizable(true);
		setIconImage(IconWrapper.from(com.feed_the_beast.ftblib.lib.icon.Icon.getIcon(FTBQuests.MOD_ID + ":textures/logotransparent.png")).getImage());

		JMenuBar menuBar = new JMenuBar();
		JMenu menuSettings = new JMenu(I18n.format("ftbquests.file"));
		menuSettings.add(menuItem(I18n.format("ftbquests.gui.reset_progress"), GuiIcons.REFRESH, () -> System.out.println("Hi")));
		menuSettings.add(menuItem(I18n.format("ftbquests.gui.complete_instantly"), GuiIcons.ACCEPT, () -> System.out.println("Hi")));
		menuSettings.add(menuItem(I18n.format("ftbquests.gui.save_as_file"), GuiIcons.DOWN, () -> System.out.println("Hi")));
		menuBar.add(menuSettings);
		setJMenuBar(menuBar);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab(I18n.format("ftbquests.gui.edit_file"), IconWrapper.from(GuiIcons.SETTINGS), new TabSettings(this));
		tabs.addTab(I18n.format("ftbquests.chapters"), IconWrapper.from(GuiIcons.COLOR_RGB), new TabChapters(this));
		tabs.addTab(I18n.format("ftbquests.reward_tables"), IconWrapper.from(GuiIcons.MONEY_BAG), new TabRewardTables(this));
		tabs.addTab(I18n.format("ftbquests.variables"), IconWrapper.from(GuiIcons.CONTROLLER), new TabVariables(this));
		setContentPane(tabs);
		pack();
		setSize(new Dimension(1000, 600));
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