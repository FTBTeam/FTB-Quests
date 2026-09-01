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
//		try {
//			return TaskTypes.TYPES.get(MiscUtil.modDefaultedId(typeStr, "item"));
//		} catch (IdentifierException e) {
//			return null;
//		}
    }

	public static TaskType getOrThrow(String typeStr) {
		return RegisteredQuestObjectType.getOrThrow(typeStr, TaskTypes.TYPES, "item");
//		var type = get(typeStr);
//		if (type == null) {
//			throw new IllegalArgumentException("Invalid task type '" + typeStr + "'");
//		}
//		return type;
	}

	public interface Provider extends RegisteredQuestObjectType.Provider<Task> {
	}

	public interface GuiProvider extends RegisteredQuestObjectType.GuiProvider<Task> {
	}

//	public Identifier getTypeId() {
//		return typeId;
//	}
//
//	public int getInternalId() {
//		return internalId;
//	}
//
//	public Task createTask(long id, Quest quest) {
//		return provider.create(id, quest);
//	}
//
//	public String getTypeForSerialization() {
//		return MiscUtil.modDefaultedString(typeId);
//	}
//
//	public Component getDisplayName() {
//		return displayName;
//	}
//
//	public Icon<?> getIcon() {
//		return iconSupplier.get();
//	}
//
//	public TaskType setGuiProvider(GuiProvider p) {
//		guiProvider = p;
//		return this;
//	}
//
//	public GuiProvider getGuiProvider() {
//		return guiProvider;
//	}
//
//	@FunctionalInterface
//	public interface Provider {
//		Task create(long id, Quest quest);
//	}
//
//	@FunctionalInterface
//	public interface GuiProvider {
//		void openCreationGui(Panel panel, Quest quest, Consumer<Task> callback);
//	}

}
