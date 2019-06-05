package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author LatvianModder
 */
public class KillTask extends QuestTask
{
	public static final ResourceLocation ZOMBIE = new ResourceLocation("minecraft:zombie");
	public ResourceLocation entity = ZOMBIE;
	public long value = 100L;

	public static StatBase get(String id)
	{
		for (StatBase base : StatList.BASIC_STATS)
		{
			if (base.statId.equals(id))
			{
				return base;
			}
		}

		return StatList.MOB_KILLS;
	}

	public KillTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestTaskType getType()
	{
		return FTBQuestsTasks.KILL;
	}

	@Override
	public long getMaxProgress()
	{
		return value;
	}

	@Override
	public String getMaxProgressString()
	{
		return Long.toUnsignedString(value);
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setString("entity", entity.toString());
		nbt.setLong("value", value);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		entity = new ResourceLocation(nbt.getString("entity"));
		value = nbt.getInteger("value");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeString(entity.toString());
		data.writeVarLong(value);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		entity = new ResourceLocation(data.readString());
		value = data.readVarInt();
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		List<ResourceLocation> ids = new ArrayList<>();

		for (EntityEntry entry : ForgeRegistries.ENTITIES)
		{
			if (EntityLivingBase.class.isAssignableFrom(entry.getEntityClass()))
			{
				ids.add(entry.getRegistryName());
			}
		}

		config.addEnum("entity", () -> entity, v -> entity = v, NameMap.create(ZOMBIE, NameMap.ObjectProperties.withName((sender, s) -> new TextComponentTranslation("entity." + EntityList.getTranslationName(s) + ".name")), ids.toArray(new ResourceLocation[0])));
		config.addLong("value", () -> value, v -> value = v, 100L, 1L, Long.MAX_VALUE);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		ITextComponent textComponent = getType().getDisplayName().createCopy();
		textComponent.appendText(": " + Long.toUnsignedString(value) + "x ");
		textComponent.appendSibling(new TextComponentTranslation("entity." + EntityList.getTranslationName(entity) + ".name"));
		return textComponent;
	}

	@Override
	public Icon getAltIcon()
	{
		if (EntityList.ENTITY_EGGS.containsKey(entity))
		{
			ItemStack stack = new ItemStack(Items.SPAWN_EGG);
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("id", entity.toString());
			stack.setTagInfo("EntityTag", nbt);
			return ItemIcon.getItemIcon(stack);
		}

		return super.getAltIcon();
	}

	@Override
	public QuestTaskData createData(ITeamData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<KillTask>
	{
		private Data(KillTask task, ITeamData data)
		{
			super(task, data);
		}

		@Override
		public String getProgressString()
		{
			return Long.toUnsignedString(progress);
		}

		public void kill(EntityLivingBase entity)
		{
			if (progress < task.value && task.entity.equals(EntityList.getKey(entity)))
			{
				progress++;
				sync();
			}
		}

		@Override
		public boolean submitTask(EntityPlayerMP player, Collection<ItemStack> itemsToCheck, boolean simulate)
		{
			return false;
		}
	}
}