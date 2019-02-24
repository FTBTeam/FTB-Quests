package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigEnum;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.FTBQuests;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author LatvianModder
 */
public class TabSettings extends TabBase
{
	public TabSettings(EditorFrame e)
	{
		super(e);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel settingsPanel = new JPanel(new GridLayout(0, 2, 6, 6));
		settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));

		ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
		e.file.getConfig(e.file.createSubGroup(group));

		List<ConfigValueInstance> allInstances = group.getValueTree();

		for (ConfigValueInstance instance : allInstances)
		{
			JLabel label = new JLabel(instance.getDisplayName().getUnformattedText(), JLabel.TRAILING);
			settingsPanel.add(label);

			if (instance.getValue() instanceof ConfigBoolean)
			{
				JCheckBox box = new JCheckBox(instance.getValue().getBoolean() ? "true" : "false", instance.getValue().getBoolean());
				box.addChangeListener(event -> {
					((ConfigBoolean) instance.getValue()).setBoolean(box.isSelected());
					box.setText(instance.getValue().getBoolean() ? "true" : "false");
				});

				settingsPanel.add(box);
			}
			else if (instance.getValue() instanceof ConfigEnum)
			{
				NameMap nameMap = ((ConfigEnum) instance.getValue()).getNameMap();
				String[] s = new String[nameMap.size()];

				for (int i = 0; i < s.length; i++)
				{
					s[i] = nameMap.getDisplayName(null, nameMap.get(i)).getUnformattedText();
				}

				JComboBox<String> box = new JComboBox<>(s);
				box.setSelectedIndex(instance.getValue().getInt());
				settingsPanel.add(box);
			}
			else
			{
				JTextField field = new JTextField(instance.getValue().getString());
				field.addActionListener(event -> instance.getValue().setValueFromString(null, field.getText(), false));
				field.setColumns(20);
				settingsPanel.add(field);
			}
		}

		mainPanel.add(settingsPanel);

		JPanel buttonPanel = new JPanel();

		JButton buttonCancel = new JButton("Undo");
		buttonCancel.addActionListener(event -> {
			System.out.println("Undo!");
		});
		buttonPanel.add(buttonCancel);

		JButton buttonAccept = new JButton("Accept");
		buttonAccept.addActionListener(event -> {
			System.out.println("Accepted!");
		});
		buttonPanel.add(buttonAccept);
		buttonPanel.setMaximumSize(new Dimension(400, 20));

		mainPanel.add(buttonPanel);

		add(mainPanel);
	}
}