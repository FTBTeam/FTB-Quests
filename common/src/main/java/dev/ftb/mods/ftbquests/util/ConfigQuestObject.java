package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.SelectQuestObjectScreen;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class ConfigQuestObject<T extends QuestObjectBase> extends ConfigValue<T> {
	public final Predicate<QuestObjectBase> predicate;

	public ConfigQuestObject(Predicate<QuestObjectBase> t) {
		predicate = t;
	}

	@Override
	public Component getStringForGUI(@Nullable QuestObjectBase value) {
		if (value == null) {
			return Component.empty();
		}

		return value.getTitle();
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		if (getCanEdit()) {
			new SelectQuestObjectScreen<>(this, callback).openGui();
		}
	}

	@Override
	public void addInfo(TooltipList list) {
		if (value != null) {
			list.add(info("ID", value));
		}
	}
}
