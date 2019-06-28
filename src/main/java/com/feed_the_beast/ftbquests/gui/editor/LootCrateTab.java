package com.feed_the_beast.ftbquests.gui.editor;

/**
 * @author LatvianModder
 */
public class LootCrateTab
{
	/*
	public LootCrateTab(Editor e)
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel dropPanel = new JPanel(new GridLayout(0, 4, 6, 6));
		dropPanel.setBorder(BorderFactory.createTitledBorder("Loot Crate Drops"));

		dropPanel.add(labelWithTooltip("Crate", "Crate name"));
		dropPanel.add(labelWithTooltip("Passive", "Drop weight from passive entities"));
		dropPanel.add(labelWithTooltip("Monster", "Drop weight from monsters"));
		dropPanel.add(labelWithTooltip("Boss", "Drop weight from bosses"));

		dropPanel.add(labelWithTooltip("No Drop", "Weight for no crate dropping"));

		dropPanel.add(textField(editor.file.lootCrateNoDrop.passive, IntVerifier.NON_NEGATIVE, s -> {
			editor.file.lootCrateNoDrop.passive = Integer.parseInt(s);
			new MessageEditObject(editor.file).sendToServer();
		}));

		dropPanel.add(textField(editor.file.lootCrateNoDrop.monster, IntVerifier.NON_NEGATIVE, s -> {
			editor.file.lootCrateNoDrop.monster = Integer.parseInt(s);
			new MessageEditObject(editor.file).sendToServer();
		}));

		dropPanel.add(textField(editor.file.lootCrateNoDrop.boss, IntVerifier.NON_NEGATIVE, s -> {
			editor.file.lootCrateNoDrop.boss = Integer.parseInt(s);
			new MessageEditObject(editor.file).sendToServer();
		}));

		for (RewardTable table : editor.file.rewardTables)
		{
			if (table.lootCrate != null)
			{
				JButton button = new JButton(table.getUnformattedTitle());
				button.setToolTipText(table.lootCrate.stringID + " | " + table.getCodeString());
				button.addActionListener(e1 -> new LootCrateDialog(editor, table.lootCrate).setVisible(true));
				dropPanel.add(button);

				dropPanel.add(textField(table.lootCrate.drops.passive, IntVerifier.NON_NEGATIVE, s -> {
					table.lootCrate.drops.passive = Integer.parseInt(s);
					Editor.scheduleObjectEdit(table);
				}));

				dropPanel.add(textField(table.lootCrate.drops.monster, IntVerifier.NON_NEGATIVE, s -> {
					table.lootCrate.drops.monster = Integer.parseInt(s);
					Editor.scheduleObjectEdit(table);
				}));

				dropPanel.add(textField(table.lootCrate.drops.boss, IntVerifier.NON_NEGATIVE, s -> {
					table.lootCrate.drops.boss = Integer.parseInt(s);
					Editor.scheduleObjectEdit(table);
				}));
			}
		}

		mainPanel.add(dropPanel);

		add(mainPanel);
	}

	public static JLabel labelWithTooltip(String label, String tooltip)
	{
		JLabel l = new JLabel(label);
		l.setHorizontalAlignment(JTextField.CENTER);
		l.setToolTipText(tooltip);
		return l;
	}

	public static JTextField textField(Object text, Predicate<String> verifier, Consumer<String> callback)
	{
		JTextField textField = new JTextField();
		textField.setHorizontalAlignment(JTextField.TRAILING);
		textField.setText(String.valueOf(text));
		textField.setInputVerifier(new InputVerifier()
		{
			@Override
			public boolean verify(JComponent input)
			{
				return verifier.test(textField.getText());
			}
		});

		Editor.addChangeListener(textField, event -> {
			String txt = textField.getText();

			if (verifier.test(txt))
			{
				callback.accept(txt);
			}
		});

		return textField;
	}
	*/
}