package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.IconWrapper;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObjectDirect;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.Objects;

/**
 * @author LatvianModder
 */
public final class EditorFrame extends JFrame
{
	private static EditorFrame editor = null;

	public static void open(boolean reload)
	{
		/*
		new JFXPanel();

		Platform.runLater(() -> {
			Stage stage = new Stage();
			stage.setTitle("Test");

			javafx.scene.control.Button btn = new javafx.scene.control.Button();
			btn.setText("click me");
			BorderPane pane = new BorderPane();
			pane.setCenter(btn);

			Scene scene = new Scene(pane, 300, 250);
			stage.setScene(scene);
			stage.show();
		});
		*/

		if (editor == null || reload)
		{
			IconWrapper.clearCache();

			new Thread(() -> {
				if (editor != null)
				{
					editor.dispose();
				}

				try
				{
					//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					editor = new EditorFrame(ClientQuestFile.INSTANCE);
					editor.setLocationRelativeTo(null);
					editor.setVisible(true);
				}
				catch (Exception ex)
				{
				}

			}, "FTB Quests Editor").start();
			return;
		}

		editor.setVisible(true);
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
		super("FTB Quests Editor [Warning: Nearly nothing works properly yet!]");
		file = f;
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setMinimumSize(new Dimension(500, 300));
		setResizable(true);
		setIconImage(com.feed_the_beast.ftblib.lib.icon.Icon.getIcon(FTBQuests.MOD_ID + ":textures/logotransparent.png").getWrappedIcon().getImage());

		JMenuBar menuBar = new JMenuBar();
		JMenu menuSettings = new JMenu(I18n.format("ftbquests.file"));
		menuSettings.add(menuItem(I18n.format("ftbquests.gui.reset_progress"), GuiIcons.REFRESH.getWrappedIcon(), () -> System.out.println("Hi")));
		menuSettings.add(menuItem(I18n.format("ftbquests.gui.complete_instantly"), GuiIcons.ACCEPT.getWrappedIcon(), () -> System.out.println("Hi")));
		menuSettings.add(menuItem(I18n.format("ftbquests.gui.save_as_file"), GuiIcons.DOWN.getWrappedIcon(), () -> System.out.println("Hi")));
		menuBar.add(menuSettings);
		setJMenuBar(menuBar);

		JTabbedPane tabs = new JTabbedPane();
		addTab(tabs, I18n.format("ftbquests.gui.edit_file"), GuiIcons.SETTINGS.getWrappedIcon(), new SettingsTab(this));
		addTab(tabs, I18n.format("ftbquests.chapters"), GuiIcons.COLOR_RGB.getWrappedIcon(), new ChaptersTab(this));
		addTab(tabs, I18n.format("ftbquests.reward_tables"), GuiIcons.MONEY_BAG.getWrappedIcon(), new RewardTableTab(this));
		addTab(tabs, I18n.format("jei.ftbquests.lootcrates"), ItemIcon.getItemIcon(FTBQuestsItems.LOOTCRATE).getWrappedIcon(), new LootCrateTab(this));
		addTab(tabs, I18n.format("ftbquests.variables"), GuiIcons.CONTROLLER.getWrappedIcon(), new VariableTab(this));
		setContentPane(tabs);
		pack();
		setSize(new Dimension(1000, 600));
	}

	private void addTab(JTabbedPane tabs, String title, Icon icon, Tab tab)
	{
		tabs.addTab(title, icon, tab.scrollPage() ? new JScrollPane(tab) : tab);
	}

	private JMenuItem menuItem(String title, @Nullable Icon icon, Runnable action)
	{
		JMenuItem item = new JMenuItem(title);

		if (icon != null)
		{
			item.setIcon(icon);
		}

		item.addActionListener(e -> action.run());
		return item;
	}

	public static void addChangeListener(JTextComponent text, ChangeListener changeListener)
	{
		Objects.requireNonNull(text);
		Objects.requireNonNull(changeListener);

		DocumentListener dl = new DocumentListener()
		{
			private int lastChange = 0, lastNotifiedChange = 0;

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				changedUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				lastChange++;
				SwingUtilities.invokeLater(() -> {
					if (lastNotifiedChange != lastChange)
					{
						lastNotifiedChange = lastChange;
						changeListener.stateChanged(new ChangeEvent(text));
					}
				});
			}
		};

		text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
			Document d1 = (Document) e.getOldValue();
			Document d2 = (Document) e.getNewValue();
			if (d1 != null)
			{
				d1.removeDocumentListener(dl);
			}
			if (d2 != null)
			{
				d2.addDocumentListener(dl);
			}
			dl.changedUpdate(null);
		});

		Document d = text.getDocument();

		if (d != null)
		{
			d.addDocumentListener(dl);
		}
	}

	public static Component add(Panel panel, String key, Component component)
	{
		JLabel label = new JLabel(I18n.format(key), SwingConstants.CENTER);

		if (I18n.hasKey(key + ".tooltip"))
		{
			label.setToolTipText(I18n.format(key + ".tooltip"));
		}

		label.setLabelFor(component);
		panel.add(label);
		panel.add(component);
		return component;
	}

	public static void schedule(Runnable runnable)
	{
		Minecraft.getMinecraft().addScheduledTask(runnable);
	}

	public static void scheduleObjectEdit(QuestObjectBase object)
	{
		schedule(() -> new MessageEditObjectDirect(object).sendToServer());
	}
}