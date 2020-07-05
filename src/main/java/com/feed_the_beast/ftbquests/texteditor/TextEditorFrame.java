package com.feed_the_beast.ftbquests.texteditor;

import com.feed_the_beast.ftbquests.quest.Quest;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class TextEditorFrame extends JFrame
{
	public static void open(Quest quest)
	{
		new TextEditorFrame(quest).requestFocus();
	}

	public final Quest quest;

	private TextEditorFrame(Quest q)
	{
		super("[WIP] FTB Quests Text Editor | " + q.chapter.getTitle() + " | " + q.getTitle());
		quest = q;
		setResizable(true);
		setSize(1000, 700);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JTabbedPane tabbedPane = new JTabbedPane();

		List<String> languages = new ArrayList<>();
		languages.add("en_us");

		for (String s : languages)
		{
			JPanel panel = new JPanel();
			panel.add(new JButton("Test!"));
			panel.add(new JButton("Delete Language")).setEnabled(false);
			tabbedPane.addTab(s, panel);
		}

		JPanel panelNewLang = new JPanel();
		panelNewLang.add(new JButton("Test!"));
		tabbedPane.addTab("+", panelNewLang);

		setContentPane(tabbedPane);

		setVisible(true);
	}
}
