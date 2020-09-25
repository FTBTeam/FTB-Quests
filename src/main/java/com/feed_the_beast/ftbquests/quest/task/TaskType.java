package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigLong;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
public final class TaskType extends ForgeRegistryEntry<TaskType>
{
	private static ForgeRegistry<TaskType> REGISTRY;

	public static void createRegistry()
	{
		if (REGISTRY == null)
		{
			ResourceLocation registryName = new ResourceLocation(FTBQuests.MOD_ID, "tasks");
			REGISTRY = (ForgeRegistry<TaskType>) new RegistryBuilder<TaskType>().setType(TaskType.class).setName(registryName).create();
			MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<>(registryName, REGISTRY));
		}
	}

	public static ForgeRegistry<TaskType> getRegistry()
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

		TaskType type = REGISTRY.getValue(new ResourceLocation(id));

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
		@OnlyIn(Dist.CLIENT)
		void openCreationGui(Runnable gui, Quest quest, Consumer<Task> callback);
	}

	public final Provider provider;
	private ITextComponent displayName;
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
			@OnlyIn(Dist.CLIENT)
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

	public TaskType setDisplayName(ITextComponent name)
	{
		displayName = name;
		return this;
	}

	public ITextComponent getDisplayName()
	{
		if (displayName == null)
		{
			ResourceLocation id = getRegistryName();
			displayName = id == null ? new StringTextComponent("error") : new TranslationTextComponent("ftbquests.task." + id.getNamespace() + '.' + id.getPath());
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