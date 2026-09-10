package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.client.GuiProviders;
import dev.ftb.mods.ftbquests.quest.RegisteredQuestObjectType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public final class TaskType extends RegisteredQuestObjectType<Task> {
	public TaskType(Identifier typeId, TaskType.Provider provider, Supplier<Icon<?>> iconSupplier, int internalId) {
		super(typeId, provider, iconSupplier, internalId,
				Component.translatable(typeId.toLanguageKey("ftbquests.task")),
				GuiProviders.defaultTaskGuiProvider(provider)
		);
	}

	@Nullable
	public static TaskType get(String typeStr) {
		return RegisteredQuestObjectType.get(typeStr, TaskTypes.TYPES, "item");
    }

	public static TaskType getOrThrow(String typeStr) {
		return RegisteredQuestObjectType.getOrThrow(typeStr, TaskTypes.TYPES, "item");
	}

	public interface Provider extends RegisteredQuestObjectType.Provider<Task> {
	}

	public interface GuiProvider extends RegisteredQuestObjectType.GuiProvider<Task> {
	}
}
