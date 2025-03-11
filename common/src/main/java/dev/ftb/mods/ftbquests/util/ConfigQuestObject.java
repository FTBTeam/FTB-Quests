package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.SelectQuestObjectScreen;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

public class ConfigQuestObject<T extends QuestObjectBase> extends ConfigValue<T> {
	public final Predicate<QuestObjectBase> predicate;
	private final Function<T, Component> formatter;

	public ConfigQuestObject(Predicate<QuestObjectBase> predicate, Function<T, Component> formatter) {
		this.predicate = predicate;
		this.formatter = formatter;
	}

	public ConfigQuestObject(Predicate<QuestObjectBase> predicate) {
		this(predicate, null);
	}

	public static Component formatEntry(QuestObjectBase qo) {
		return qo.getMutableTitle().withStyle(qo.getObjectType().getColor());
	}

	@Override
	public Component getStringForGUI(@Nullable QuestObjectBase value) {
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
	public void addInfo(TooltipList list) {
		if (value != null) {
			list.add(info("ID", value));
		}
	}
}
