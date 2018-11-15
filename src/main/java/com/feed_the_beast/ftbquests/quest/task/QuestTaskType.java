package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.IOpenableGui;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfig;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public final class QuestTaskType extends IForgeRegistryEntry.Impl<QuestTaskType>
{
	private static ForgeRegistry<QuestTaskType> REGISTRY;

	public static void createRegistry()
	{
		if (REGISTRY == null)
		{
			ResourceLocation registryName = new ResourceLocation(FTBQuests.MOD_ID, "tasks");
			REGISTRY = (ForgeRegistry<QuestTaskType>) new RegistryBuilder<QuestTaskType>().setType(QuestTaskType.class).setName(registryName).create();
			MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<>(registryName, REGISTRY));
		}
	}

	public static ForgeRegistry<QuestTaskType> getRegistry()
	{
		return REGISTRY;
	}

	@Nullable
	public static QuestTask createTask(Quest quest, String id)
	{
		if (id.isEmpty())
		{
			id = FTBQuests.MOD_ID + ":item";
		}
		else if (id.indexOf(':') == -1)
		{
			id = FTBQuests.MOD_ID + ':' + id;
		}

		QuestTaskType type = REGISTRY.getValue(new ResourceLocation(id));

		if (type == null)
		{
			return null;
		}

		QuestTask task = type.provider.create(quest);

		if (task == null)
		{
			return null;
		}

		return task;
	}

	@FunctionalInterface
	public interface Provider
	{
		@Nullable
		QuestTask create(Quest quest);
	}

	public interface GuiProvider
	{
		@SideOnly(Side.CLIENT)
		void openCreationGui(IOpenableGui gui, Quest quest);
	}

	public final Class typeClass;
	public final Provider provider;
	private ITextComponent displayName;
	private Icon icon;
	private GuiProvider guiProvider;

	public QuestTaskType(Class<? extends QuestTask> c, Provider p)
	{
		typeClass = c;
		provider = p;
		displayName = null;
		icon = GuiIcons.ACCEPT;
		guiProvider = new GuiProvider()
		{
			@Override
			@SideOnly(Side.CLIENT)
			public void openCreationGui(IOpenableGui gui, Quest quest)
			{
				QuestTask task = provider.create(quest);

				if (task == null)
				{
					return;
				}

				ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
				ConfigGroup g = task.createSubGroup(group);
				task.getConfig(g);
				task.getExtraConfig(g);

				new GuiEditConfig(group, (g1, sender) -> {
					NBTTagCompound extra = new NBTTagCompound();
					extra.setString("type", getTypeForNBT());
					new MessageCreateObject(task, extra).sendToServer();
				}).openGui();
			}
		};
	}

	public String getTypeForNBT()
	{
		return getRegistryName().getNamespace().equals(FTBQuests.MOD_ID) ? getRegistryName().getPath() : toString();
	}

	public QuestTaskType setDisplayName(ITextComponent name)
	{
		displayName = name;
		return this;
	}

	public ITextComponent getDisplayName()
	{
		if (displayName != null)
		{
			return displayName.createCopy();
		}

		ResourceLocation id = getRegistryName();
		return new TextComponentTranslation("ftbquests.task." + id.getNamespace() + '.' + id.getPath());
	}

	public QuestTaskType setIcon(Icon i)
	{
		icon = i;
		return this;
	}

	public Icon getIcon()
	{
		return icon;
	}

	public QuestTaskType setGuiProvider(GuiProvider p)
	{
		guiProvider = p;
		return this;
	}

	public GuiProvider getGuiProvider()
	{
		return guiProvider;
	}
}