package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.LongConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TaskType {
	private final ResourceLocation typeId;
	private final Provider provider;
	private final Supplier<Icon> iconSupplier;
	private Component displayName;
	private GuiProvider guiProvider;
	public int internalId;

	TaskType(ResourceLocation typeId, Provider provider, Supplier<Icon> iconSupplier) {
		this.typeId = typeId;
		this.provider = provider;
		this.iconSupplier = iconSupplier;

		displayName = null;
		guiProvider = (gui, quest, callback) -> {
			Task task = TaskType.this.provider.create(0L, quest);

			if (task instanceof ISingleLongValueTask slvTask) {
				LongConfig c = new LongConfig(slvTask.getMinConfigValue(), slvTask.getMaxConfigValue());

				EditConfigFromStringScreen.open(c, slvTask.getDefaultConfigValue(), slvTask.getDefaultConfigValue(), accepted -> {
					if (accepted) {
						slvTask.setValue(c.getValue());
						callback.accept(task);
					}
					gui.run();
				});
			} else {
				ConfigGroup group = new ConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> {
					if (accepted) {
						callback.accept(task);
					}
					gui.run();
				});
				task.fillConfigGroup(task.createSubGroup(group));
				new EditConfigScreen(group).openGui();
			}
		};
	}

	public ResourceLocation getTypeId() {
		return typeId;
	}

	@Nullable
	public static Task createTask(long id, Quest quest, String typeId) {
		if (typeId.isEmpty()) {
			typeId = FTBQuestsAPI.MOD_ID + ":item";
		} else if (typeId.indexOf(':') == -1) {
			typeId = FTBQuestsAPI.MOD_ID + ':' + typeId;
		}

		TaskType type = TaskTypes.TYPES.get(new ResourceLocation(typeId));

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
		void openCreationGui(Runnable gui, Quest quest, Consumer<Task> callback);
	}

}
