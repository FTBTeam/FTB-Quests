package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public final class QuestTaskType extends IForgeRegistryEntry.Impl<QuestTaskType>
{
	private static IForgeRegistry<QuestTaskType> REGISTRY;
	private static Map<Class, QuestTaskType> TYPE_MAP;

	public static void createRegistry()
	{
		if (REGISTRY == null)
		{
			ResourceLocation registryName = new ResourceLocation(FTBQuests.MOD_ID, "tasks");
			REGISTRY = new RegistryBuilder<QuestTaskType>().setType(QuestTaskType.class).setName(registryName).create();
			MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<>(registryName, REGISTRY));

			TYPE_MAP = new HashMap<>();

			for (QuestTaskType type : REGISTRY)
			{
				TYPE_MAP.put(type.typeClass, type);
			}
		}
	}

	public static IForgeRegistry<QuestTaskType> getRegistry()
	{
		return REGISTRY;
	}

	@Nullable
	public static QuestTask createTask(Quest quest, NBTTagCompound nbt)
	{
		String id = nbt.getString("type");

		if (id.isEmpty())
		{
			return null;
		}

		if (id.indexOf(':') == -1)
		{
			id = FTBQuests.MOD_ID + ':' + id;
		}

		QuestTaskType type = REGISTRY.getValue(new ResourceLocation(id));

		if (type == null)
		{
			return null;
		}

		QuestTask task = type.provider.create(quest, nbt);

		if (task == null)
		{
			return null;
		}

		task.readID(nbt);
		task.title = nbt.getString("title");
		task.icon = ItemStackSerializer.read(nbt.getCompoundTag("icon"));
		task.completionCommand = nbt.getString("completion_command");
		return task;
	}

	@Nullable
	public static QuestTaskType getType(Class clazz)
	{
		return TYPE_MAP.get(clazz);
	}

	public static String getTypeForNBT(Class clazz)
	{
		QuestTaskType type = getType(clazz);
		return type == null ? "" : type.getTypeForNBT();
	}

	public interface Provider
	{
		@Nullable
		QuestTask create(Quest quest, NBTTagCompound nbt);
	}

	public final Class typeClass;
	public final Provider provider;
	private ITextComponent displayName;

	public QuestTaskType(Class<? extends QuestTask> c, Provider p)
	{
		typeClass = c;
		provider = p;
		displayName = null;
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
}