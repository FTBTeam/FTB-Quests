package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftbquests.quest.CallbackButton;
import com.feed_the_beast.ftbquests.quest.CallbackCheckBox;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author LatvianModder
 */
public class LootCrateDialog extends JDialog
{
	public final LootCrate crate;

	public LootCrateDialog(EditorFrame f, LootCrate l)
	{
		super(f, l.table.getDisplayName().getUnformattedText());
		crate = l;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		Panel panel = new Panel(new GridLayout(0, 2, 6, 6));

		EditorFrame.add(panel, "ftbquests.reward_table.loot_crate.id", new JTextField(crate.stringID)).setEnabled(false);
		EditorFrame.add(panel, "ftbquests.reward_table.loot_crate.item_name", new JTextField(crate.itemName)).setEnabled(false);
		EditorFrame.add(panel, "ftbquests.reward_table.loot_crate.color", new CallbackButton("Select Color", () -> {
			JDialog colorDialog = new JDialog(f, "Select Color");

			colorDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			JColorChooser colorChooser = new JColorChooser(new java.awt.Color(crate.color.rgba()));
			colorChooser.getSelectionModel().addChangeListener(e -> {
				crate.color = Color4I.rgb(colorChooser.getColor().getRGB());
				EditorFrame.scheduleObjectEdit(crate.table);
			});
			colorDialog.add(colorChooser);
			colorDialog.pack();
			colorDialog.setLocationRelativeTo(LootCrateDialog.this);
			colorDialog.setResizable(false);
			colorDialog.setVisible(true);
		}));
		EditorFrame.add(panel, "ftbquests.reward_table.loot_crate.glow", new CallbackCheckBox(crate.glow, v -> {
			crate.glow = v;
			EditorFrame.scheduleObjectEdit(crate.table);
		}));

		mainPanel.add(panel);
		add(mainPanel);
		pack();
		setResizable(false);
		setIconImage(crate.table.getIcon().getWrappedIcon().getImage());
		setLocationRelativeTo(f);
	}
}