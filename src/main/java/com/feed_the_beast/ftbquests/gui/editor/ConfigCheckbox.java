package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;

/**
 * @author LatvianModder
 */
public class ConfigCheckbox extends CheckBox implements EventHandler<ActionEvent>
{
	public final ConfigBoolean config;
	public final ConfigEditedCallback callback;

	public ConfigCheckbox(ConfigBoolean c, ConfigEditedCallback ca)
	{
		config = c;
		callback = ca;
		setSelected(config.getBoolean());
		setText(isSelected() ? "True" : "False");
	}

	@Override
	public void handle(ActionEvent event)
	{
		config.setBoolean(isSelected());
		setText(isSelected() ? "True" : "False");
		callback.configEdited(config);
	}
}