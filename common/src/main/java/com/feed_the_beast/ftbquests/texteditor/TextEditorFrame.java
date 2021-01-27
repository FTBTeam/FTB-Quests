package com.feed_the_beast.ftbquests.texteditor;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author LatvianModder
 */
public class TextEditorFrame extends JFrame
{
	public static void open(Quest quest)
	{
		new TextEditorFrame(quest).requestFocus();
	}

	private static BufferedImage logo;

	public final Quest quest;
	private final String originalTitle;
	private final String originalSubtitle;
	private final String originalDescription;

	public final JTextField title;
	public final JTextField subtitle;
	public final JTextArea description;
	public final JButton save;
	public final JButton reset;

	private TextEditorFrame(Quest q)
	{
		super("FTB Quests Text Editor | " + q.chapter.getTitle().getString() + " | " + q.getTitle().getString());
		quest = q;
		originalTitle = quest.title;
		originalSubtitle = quest.subtitle;
		originalDescription = String.join("\n", quest.description);

		if (logo == null)
		{
			try (InputStream stream = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(FTBQuests.MOD_ID, "textures/logotransparent.png")).getInputStream())
			{
				logo = ImageIO.read(stream);
			}
			catch (Exception ex)
			{
			}
		}

		if (logo != null)
		{
			setIconImage(logo);
		}

		setResizable(true);
		//setSize(1000, 700);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		//panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		//panel.setMinimumSize(new Dimension(500, 700));
		panel.add(title = new JTextField(originalTitle, 75));
		panel.add(subtitle = new JTextField(originalSubtitle, 75));
		panel.add(description = new JTextArea(originalDescription, 30, 75));

		title.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true), I18n.get("ftbquests.title")));
		subtitle.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true), I18n.get("ftbquests.quest.subtitle")));
		description.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true), I18n.get("ftbquests.quest.description")));

		JPanel buttonPanel = new JPanel();

		buttonPanel.add(reset = new JButton("Reset"));
		buttonPanel.add(save = new JButton("Save"));
		reset.addActionListener(this::resetClicked);
		save.addActionListener(this::saveClicked);

		panel.add(buttonPanel);

		setContentPane(panel);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void resetClicked(ActionEvent event)
	{
		title.setText(originalTitle);
		subtitle.setText(originalSubtitle);
		description.setText(originalDescription);
	}

	private void saveClicked(ActionEvent event)
	{
		Minecraft.getInstance().submit(() -> {
			quest.title = title.getText().trim();
			quest.subtitle = subtitle.getText().trim();
			quest.description.clear();
			quest.description.addAll(Arrays.asList(description.getText().split("\n")));
			quest.clearCachedData();
			setTitle("FTB Quests Text Editor | " + quest.chapter.getTitle().getString() + " | " + quest.getTitle().getString());
			new MessageEditObject(quest).sendToServer();
		});
	}
}