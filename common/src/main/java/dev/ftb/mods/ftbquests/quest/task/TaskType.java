package dev.ftb.mods.ftbquests.quest.task;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.GuiProviders;
import dev.ftb.mods.ftbquests.quest.Quest;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.apache.commons.lang3.Validate;

public final class TaskType {
	private final Identifier typeId;
	private final Provider provider;
	private final Supplier<Icon<?>> iconSupplier;
	private final Component displayName;
	private GuiProvider guiProvider;
	public int internalId;

	TaskType(Identifier typeId, Provider provider, Supplier<Icon<?>> iconSupplier) {
		this.typeId = typeId;
		this.provider = provider;
		this.iconSupplier = iconSupplier;

		displayName = Component.translatable(typeId.toLanguageKey("ftbquests.task"));
		guiProvider = GuiProviders.defaultTaskGuiProvider(provider);
	}

	public Identifier getTypeId() {
		return typeId;
	}

	public static Task createTask(long id, Quest quest, String typeId) {
		if (typeId.isEmpty()) {
			typeId = FTBQuestsAPI.MOD_ID + ":item";
		} else if (typeId.indexOf(':') == -1) {
			typeId = FTBQuestsAPI.MOD_ID + ':' + typeId;
		}

		TaskType type = TaskTypes.TYPES.get(Identifier.tryParse(typeId));
		Validate.isTrue(type != null, "Unknown task type: " + type);
		return type.provider.create(id, quest);
	}

	public Task createTask(long id, Quest quest) {
		return provider.create(id, quest);
	}

	public String getTypeForNBT() {
		return typeId.getNamespace().equals(FTBQuestsAPI.MOD_ID) ? typeId.getPath() : typeId.toString();
	}

	public CompoundTag makeExtraNBT() {
		return Util.make(new CompoundTag(), t -> t.putString("type", getTypeForNBT()));
	}

	public Component getDisplayName() {
		return displayName;
	}

	public Icon<?> getIconSupplier() {
		return iconSupplier.get();
	}

	public TaskType setGuiProvider(GuiProvider p) {
		guiProvider = p;
		return this;
	}

	public GuiProvider getGuiProvider() {
		return guiProvider;
	}

	@FunctionalInterface
	public interface Provider {
		Task create(long id, Quest quest);
	}

	@FunctionalInterface
	public interface GuiProvider {
		void openCreationGui(Panel panel, Quest quest, BiConsumer<Task,CompoundTag> callback);
	}

}
