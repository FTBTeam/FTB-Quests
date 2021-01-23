package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigLong;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public final class TaskType
{
	private static Registry<TaskType> REGISTRY;
	private ResourceLocation registryName;

	public static void createRegistry()
	{
		if (REGISTRY == null)
		{
			ResourceLocation registryName = new ResourceLocation(FTBQuests.MOD_ID, "tasks");
			REGISTRY = (ForgeRegistry<TaskType>) new RegistryBuilder<TaskType>().setType(TaskType.class).setName(registryName).create();
			MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<>(registryName, REGISTRY));
		}
	}

	public static Registry<TaskType> getRegistry()
	{
		return REGISTRY;
	}

	@Nullable
	public static Task createTask(Quest quest, String id)
	{
		if (id.isEmpty())
		{
			id = FTBQuests.MOD_ID + ":item";
		}
		else if (id.indexOf(':') == -1)
		{
			id = FTBQuests.MOD_ID + ':' + id;
		}

		TaskType type = REGISTRY.get(new ResourceLocation(id));

		if (type == null)
		{
			return null;
		}

		return type.provider.create(quest);
	}

	@FunctionalInterface
	public interface Provider
	{
		Task create(Quest quest);
	}

	public interface GuiProvider
	{
		@Environment(EnvType.CLIENT)
		void openCreationGui(Runnable gui, Quest quest, Consumer<Task> callback);
	}

	public final Provider provider;
	private MutableComponent displayName;
	private Icon icon;
	private GuiProvider guiProvider;

	public TaskType(Provider p)
	{
		provider = p;
		displayName = null;
		icon = GuiIcons.ACCEPT;
		guiProvider = new GuiProvider()
		{
			@Override
			@Environment(EnvType.CLIENT)
			public void openCreationGui(Runnable gui, Quest quest, Consumer<Task> callback)
			{
				Task task = provider.create(quest);

				if (task instanceof ISingleLongValueTask)
				{
					ISingleLongValueTask t = (ISingleLongValueTask) task;
					ConfigLong c = new ConfigLong(0L, t.getMaxConfigValue());

					GuiEditConfigFromString.open(c, t.getDefaultConfigValue(), t.getDefaultConfigValue(), accepted -> {
						if (accepted)
						{
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
					if (accepted)
					{
						callback.accept(task);
					}
					gui.run();
				};
				new GuiEditConfig(group).openGui();
			}
		};
	}

	public String getTypeForNBT()
	{
		return getRegistryName().getNamespace().equals(FTBQuests.MOD_ID) ? getRegistryName().getPath() : getRegistryName().toString();
	}
    
    public ResourceLocation getRegistryName() {
        return registryName;
    }
    
    public TaskType setDisplayName(MutableComponent name)
	{
		displayName = name;
		return this;
	}

	public MutableComponent getDisplayName()
	{
		if (displayName == null)
		{
			ResourceLocation id = getRegistryName();
			displayName = id == null ? new TextComponent("error") : new TranslatableComponent("ftbquests.task." + id.getNamespace() + '.' + id.getPath());
		}

		return displayName;
	}

	public TaskType setIcon(Icon i)
	{
		icon = i;
		return this;
	}

	public Icon getIcon()
	{
		return icon;
	}

	public TaskType setGuiProvider(GuiProvider p)
	{
		guiProvider = p;
		return this;
	}

	public GuiProvider getGuiProvider()
	{
		return guiProvider;
	}
}