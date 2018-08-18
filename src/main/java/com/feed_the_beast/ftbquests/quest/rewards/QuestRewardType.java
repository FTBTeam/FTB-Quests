package com.feed_the_beast.ftbquests.quest.rewards;

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
public final class QuestRewardType extends IForgeRegistryEntry.Impl<QuestRewardType>
{
	private static IForgeRegistry<QuestRewardType> REGISTRY;
	private static Map<Class, QuestRewardType> TYPE_MAP;

	public static void createRegistry()
	{
		if (REGISTRY == null)
		{
			ResourceLocation registryName = new ResourceLocation(FTBQuests.MOD_ID, "rewards");
			REGISTRY = new RegistryBuilder<QuestRewardType>().setType(QuestRewardType.class).setName(registryName).create();
			MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<>(registryName, REGISTRY));

			TYPE_MAP = new HashMap<>();

			for (QuestRewardType type : REGISTRY)
			{
				TYPE_MAP.put(type.typeClass, type);
			}
		}
	}

	public static IForgeRegistry<QuestRewardType> getRegistry()
	{
		return REGISTRY;
	}

	@Nullable
	public static QuestReward createReward(Quest quest, NBTTagCompound nbt)
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

		QuestRewardType type = REGISTRY.getValue(new ResourceLocation(id));

		if (type == null)
		{
			return null;
		}

		QuestReward reward = type.provider.create(quest, nbt);

		if (reward == null)
		{
			return null;
		}

		reward.readID(nbt);
		reward.teamReward = nbt.getBoolean("team_reward");
		reward.title = nbt.getString("title");
		return reward;
	}

	@Nullable
	public static QuestRewardType getType(Class clazz)
	{
		return TYPE_MAP.get(clazz);
	}

	public interface Provider
	{
		@Nullable
		QuestReward create(Quest quest, NBTTagCompound nbt);
	}

	public final Class typeClass;
	public final Provider provider;
	private ITextComponent displayName;

	public QuestRewardType(Class<? extends QuestReward> c, Provider p)
	{
		typeClass = c;
		provider = p;
		displayName = null;
	}

	public String getTypeForNBT()
	{
		return getRegistryName().getNamespace().equals(FTBQuests.MOD_ID) ? getRegistryName().getPath() : toString();
	}

	public QuestRewardType setDisplayName(ITextComponent name)
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
		return new TextComponentTranslation("ftbquests.reward." + id.getNamespace() + '.' + id.getPath());
	}
}