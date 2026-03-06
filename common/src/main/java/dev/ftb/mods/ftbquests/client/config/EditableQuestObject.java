package dev.ftb.mods.ftbquests.client.config;

import net.minecraft.network.chat.Component;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.SelectQuestObjectScreen;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;

import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

public class EditableQuestObject<T extends QuestObjectBase> extends EditableConfigValue<T> {
	public final Predicate<QuestObjectBase> predicate;
	@Nullable
	private final Function<T, Component> formatter;

	public EditableQuestObject(Predicate<QuestObjectBase> predicate, @Nullable Function<T, Component> formatter) {
		this.predicate = predicate;
		this.formatter = formatter;
	}

	public EditableQuestObject(Predicate<QuestObjectBase> predicate) {
		this(predicate, null);
	}

	public static Component formatEntry(QuestObjectBase qo) {
		return qo.getMutableTitle().withStyle(qo.getObjectType().getColor());
	}

	@Override
	public Component getStringForGUI(@Nullable T value) {
		if (value == null) {
			return Component.empty();
		}

		return value.getTitle();
	}

	@Override
	public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
		if (getCanEdit()) {
			new SelectQuestObjectScreen<>(this, callback).withFormatter(formatter).openGui();
		}
	}

	@Override
	public void addInfo(TooltipList list, Theme theme) {
		list.add(info("ID", value));
	}
}
