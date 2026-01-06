package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.GuiProviders;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class TaskType {
	private final Identifier typeId;
	private final Provider provider;
	private final Supplier<Icon> iconSupplier;
	private Component displayName;
	private GuiProvider guiProvider;
	public int internalId;

	TaskType(Identifier typeId, Provider provider, Supplier<Icon> iconSupplier) {
		this.typeId = typeId;
		this.provider = provider;
		this.iconSupplier = iconSupplier;

		displayName = null;
		guiProvider = GuiProviders.defaultTaskGuiProvider(provider);
	}

	public Identifier getTypeId() {
		return typeId;
	}

	@Nullable
	public static Task createTask(long id, Quest quest, String typeId) {
		if (typeId.isEmpty()) {
			typeId = FTBQuestsAPI.MOD_ID + ":item";
		} else if (typeId.indexOf(':') == -1) {
			typeId = FTBQuestsAPI.MOD_ID + ':' + typeId;
		}

		TaskType type = TaskTypes.TYPES.get(Identifier.tryParse(typeId));

		if (type == null) {
			return null;
		}

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

	public TaskType setDisplayName(Component name) {
		displayName = name;
		return this;
	}

	public Component getDisplayName() {
		if (displayName == null) {
			displayName = Component.translatable("ftbquests.task." + typeId.getNamespace() + '.' + typeId.getPath());
		}

		return displayName;
	}

	public Icon getIconSupplier() {
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
		@Environment(EnvType.CLIENT)
		void openCreationGui(Panel panel, Quest quest, BiConsumer<Task,CompoundTag> callback);
	}

}
