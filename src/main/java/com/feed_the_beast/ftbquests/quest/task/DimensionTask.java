package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author LatvianModder
 */
public class DimensionTask extends QuestTask
{
	public int dimension = -1;

	public DimensionTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestTaskType getType()
	{
		return FTBQuestsTasks.DIMENSION;
	}

	@Override
	public long getMaxProgress()
	{
		return 1;
	}

	@Override
	public boolean hideProgressNumbers()
	{
		return true;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setInteger("dim", dimension);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);

		try
		{
			dimension = nbt.getInteger("dim");

			if (dimension == 0 && nbt.hasKey("dim", Constants.NBT.TAG_STRING))
			{
				dimension = DimensionType.byName(nbt.getString("dim")).getId();
			}
		}
		catch (Exception ex)
		{
			dimension = -1;
		}
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(dimension);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		dimension = data.readVarInt();
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		IntOpenHashSet dims = new IntOpenHashSet();

		for (DimensionType types : DimensionType.values())
		{
			if (types != null)
			{
				dims.add(types.getId());
			}
		}

		if (!quest.chapter.file.isClient())
		{
			dims.addAll(Arrays.asList(DimensionManager.getIDs()));
		}

		config.addEnum("dim", () -> dimension, v -> dimension = v, NameMap.create(-1, NameMap.ObjectProperties.withName((sender, dim) -> getName(dim)), dims.toArray(new Integer[0]))).setDisplayName(new TextComponentTranslation("ftbquests.task.ftbquests.dimension"));
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		ITextComponent text = getName(dimension);
		text.getStyle().setColor(TextFormatting.DARK_GREEN);
		return new TextComponentTranslation("ftbquests.task.ftbquests.dimension", text).appendText(": ").appendSibling(text);
	}

	public static ITextComponent getName(int dim)
	{
		switch (dim)
		{
			case 0:
				return new TextComponentTranslation("createWorld.customize.preset.overworld");
			case -1:
				return new TextComponentTranslation("advancements.nether.root.title");
			case 1:
				return new TextComponentTranslation("advancements.end.root.title");
			default:
				try
				{
					return new TextComponentString(DimensionType.getById(dim).getName());
				}
				catch (Exception ex)
				{
					return new TextComponentString("dim_" + dim);
				}
		}
	}

	@Override
	public QuestTaskData createData(ITeamData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<DimensionTask>
	{
		private Data(DimensionTask task, ITeamData data)
		{
			super(task, data);
		}

		@Override
		public boolean submitTask(EntityPlayerMP player, Collection<ItemStack> itemsToCheck, boolean simulate)
		{
			if (progress < 1L && player.dimension == task.dimension && !player.isSpectator())
			{
				if (!simulate)
				{
					progress = 1L;
					sync();
				}

				return true;
			}

			return false;
		}
	}
}