package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.config.ConfigEnum;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.util.misc.BooleanConsumer;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.CallbackCheckBox;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author LatvianModder
 */
public class SettingsTab extends Tab
{
	public SettingsTab(EditorFrame e)
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
			Component component;

			if (instance.getValue() instanceof BooleanConsumer)
			{
				component = new CallbackCheckBox(instance.getValue().getBoolean(), (BooleanConsumer) instance.getValue());
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
				component = box;
			}
			else
			{
				JTextField field = new JTextField(instance.getValue().getString());
				field.addActionListener(event -> instance.getValue().setValueFromString(null, field.getText(), false));
				field.setColumns(20);
				component = field;
			}

			JLabel label = new JLabel(instance.getDisplayName().getUnformattedText(), SwingConstants.CENTER);
			label.setLabelFor(component);
			settingsPanel.add(label);
			settingsPanel.add(component);
		}

		mainPanel.add(settingsPanel);
		add(mainPanel);
	}
}