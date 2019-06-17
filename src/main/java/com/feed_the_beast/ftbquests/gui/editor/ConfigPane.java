package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigEnum;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigValue;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import net.minecraft.client.Minecraft;

/**
 * @author LatvianModder
 */
public class ConfigPane extends GridPane implements ConfigEditedCallback
{
	public final QuestObjectBase object;
	public ConfigGroup original, group;
	public Button buttonReset, buttonSave;

	public ConfigPane(QuestObjectBase o)
	{
		object = o;
		setMinSize(400, 200);
		setPadding(new Insets(10, 10, 10, 10));
		setVgap(5);
		setHgap(5);
		setAlignment(Pos.CENTER);

		buttonReset = new Button();
		buttonReset.setText("Reset");
		buttonReset.setDisable(true);
		buttonReset.setOnAction(event -> {
			group.deserializeEditedNBT(original.serializeNBT());
			refreshConfig();
		});

		buttonSave = new Button();
		buttonSave.setText("Save");
		buttonSave.setDefaultButton(true);
		buttonSave.setDisable(true);
		buttonSave.setOnAction(event -> {
			Editor.scheduleObjectEdit(object);
			buttonSave.setDisable(true);
		});

		original = ConfigGroup.newGroup(FTBQuests.MOD_ID);
		object.getConfig(Minecraft.getMinecraft().player, object.createSubGroup(original));

		group = original.copy();
		refreshConfig();
	}

	public void refreshConfig()
	{
		getChildren().clear();
		int row = 0;

		ConfigGroup prevGroup = null;

		for (ConfigValueInstance instance : group.getValueTree())
		{
			ConfigGroup g = instance.getGroup();

			if (prevGroup == null)
			{
				prevGroup = g;
			}
			else if (prevGroup != g)
			{
				prevGroup = g;
				addRow(row, new Separator(), new Separator());
				row++;
			}

			ConfigValue value = instance.getValue();
			Node valueNode;

			if (value instanceof ConfigBoolean)
			{
				valueNode = new ConfigCheckbox((ConfigBoolean) value, this);
			}
			else if (value instanceof ConfigEnum)
			{
				valueNode = new ConfigComboBox((ConfigEnum) value, this);
			}
			else
			{
				TextField field = new TextField(value.getString());
				field.setOnAction(event -> {
					value.setValueFromString(Minecraft.getMinecraft().player, field.getText(), false);
					configEdited(value);
				});
				field.setOnKeyTyped(event -> {
					value.setValueFromString(Minecraft.getMinecraft().player, field.getText(), false);
					configEdited(value);
				});
				valueNode = field;
			}

			addRow(row, new Text(instance.getDisplayName().getUnformattedText()), valueNode);
			row++;
		}

		addRow(row, new Separator(), new Separator());
		row++;
		addRow(row, buttonReset, buttonSave);
	}

	@Override
	public void configEdited(ConfigValue value)
	{
		buttonReset.setDisable(false);
		buttonSave.setDisable(false);
	}
}