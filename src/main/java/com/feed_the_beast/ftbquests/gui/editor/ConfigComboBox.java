package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.config.ConfigEnum;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import net.minecraft.client.Minecraft;

/**
 * @author LatvianModder
 */
public class ConfigComboBox<T> extends ComboBox<T> implements EventHandler<ActionEvent>
{
	public final ConfigEnum<T> config;
	public final ConfigEditedCallback callback;

	public ConfigComboBox(ConfigEnum<T> c, ConfigEditedCallback ca)
	{
		config = c;
		callback = ca;

		NameMap<T> nameMap = config.getNameMap();
		getItems().addAll(nameMap.values);
		setOnAction(this);
		setCellFactory(callback -> new ListCell<T>()
		{
			@Override
			public void updateItem(T object, boolean empty)
			{
				super.updateItem(object, empty);

				if (empty)
				{
					setText(null);
				}
				else
				{
					setText(config.getNameMap().getDisplayName(Minecraft.getMinecraft().player, object).getUnformattedText());
				}
			}
		});

		getSelectionModel().select(config.getValue());
	}

	@Override
	public void handle(ActionEvent event)
	{
		config.setValue(getSelectionModel().getSelectedItem());
		callback.configEdited(config);
	}
}