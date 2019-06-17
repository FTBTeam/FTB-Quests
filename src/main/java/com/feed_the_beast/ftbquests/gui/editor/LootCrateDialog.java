package com.feed_the_beast.ftbquests.gui.editor;

import javax.swing.*;

/**
 * @author LatvianModder
 */
public class LootCrateDialog extends JDialog
{
	/*
	public final LootCrate crate;

	public LootCrateDialog(Editor f, LootCrate l)
	{
		super(f, l.table.getUnformattedTitle());
		crate = l;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		Panel panel = new Panel(new GridLayout(0, 2, 6, 6));

		Editor.add(panel, "ftbquests.reward_table.loot_crate.id", new JTextField(crate.stringID)).setEnabled(false);
		Editor.add(panel, "ftbquests.reward_table.loot_crate.item_name", new JTextField(crate.itemName)).setEnabled(false);
		Editor.add(panel, "ftbquests.reward_table.loot_crate.color", new CallbackButton("Select Color", () -> {
			JDialog colorDialog = new JDialog(f, "Select Color");

			colorDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			JColorChooser colorChooser = new JColorChooser(new java.awt.Color(crate.color.rgba()));
			colorChooser.getSelectionModel().addChangeListener(e -> {
				crate.color = Color4I.rgb(colorChooser.getColor().getRGB());
				Editor.scheduleObjectEdit(crate.table);
			});
			colorDialog.add(colorChooser);
			colorDialog.pack();
			colorDialog.setLocationRelativeTo(LootCrateDialog.this);
			colorDialog.setResizable(false);
			colorDialog.setVisible(true);
		}));
		Editor.add(panel, "ftbquests.reward_table.loot_crate.glow", new CallbackCheckBox(crate.glow, v -> {
			crate.glow = v;
			Editor.scheduleObjectEdit(crate.table);
		}));

		mainPanel.add(panel);
		add(mainPanel);
		pack();
		setResizable(false);
		setIconImage(crate.table.getIcon().getWrappedIcon().getImage());
		setLocationRelativeTo(f);
	}
	*/
}