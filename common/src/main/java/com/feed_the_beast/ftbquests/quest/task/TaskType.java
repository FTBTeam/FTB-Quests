package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigLong;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import me.shedaniel.architectury.core.RegistryEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public final class TaskType extends RegistryEntry<TaskType> {
	@Nullable
	public static Task createTask(Quest quest, String id) {
		if (id.isEmpty()) {
			id = FTBQuests.MOD_ID + ":item";
		} else if (id.indexOf(':') == -1) {
			id = FTBQuests.MOD_ID + ':' + id;
		}

		TaskType type = TaskTypes.TYPES.get(new ResourceLocation(id));

		if (type == null) {
			return null;
		}

		return type.provider.create(quest);
	}

	@FunctionalInterface
	public interface Provider {
		Task create(Quest quest);
	}

	public interface GuiProvider {
		@Environment(EnvType.CLIENT)
		void openCreationGui(Runnable gui, Quest quest, Consumer<Task> callback);
	}

	public final ResourceLocation id;
	public final Provider provider;
	private final Supplier<Icon> icon;
	private Component displayName;
	private GuiProvider guiProvider;
	public int intId;

	TaskType(ResourceLocation i, Provider p, Supplier<Icon> ic) {
		id = i;
		provider = p;
		icon = ic;
		displayName = null;
		guiProvider = new GuiProvider() {
			@Override
			@Environment(EnvType.CLIENT)
			public void openCreationGui(Runnable gui, Quest quest, Consumer<Task> callback) {
				Task task = provider.create(quest);

				if (task instanceof ISingleLongValueTask) {
					ISingleLongValueTask t = (ISingleLongValueTask) task;
					ConfigLong c = new ConfigLong(0L, t.getMaxConfigValue());

					GuiEditConfigFromString.open(c, t.getDefaultConfigValue(), t.getDefaultConfigValue(), accepted -> {
						if (accepted) {
							((ISingleLongValueTask) task).setValue(c.value);
							callback.accept(task);
						}
						gui.run();
					});
					return;
				}

				ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
				task.getConfig(task.createSubGroup(group));
				group.savedCallback = accepted -> {
					if (accepted) {
						callback.accept(task);
					}
					gui.run();
				};
				new GuiEditConfig(group).openGui();
			}
		};
	}

	public String getTypeForNBT() {
		return id.getNamespace().equals(FTBQuests.MOD_ID) ? id.getPath() : id.toString();
	}

	public TaskType setDisplayName(Component name) {
		displayName = name;
		return this;
	}

	public Component getDisplayName() {
		if (displayName == null) {
			displayName = new TranslatableComponent("ftbquests.task." + id.getNamespace() + '.' + id.getPath());
		}

		return displayName;
	}

	public Icon getIcon() {
		return icon.get();
	}

	public TaskType setGuiProvider(GuiProvider p) {
		guiProvider = p;
		return this;
	}

	public GuiProvider getGuiProvider() {
		return guiProvider;
	}
}