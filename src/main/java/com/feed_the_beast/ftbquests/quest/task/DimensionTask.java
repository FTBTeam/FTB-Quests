package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.ServerUtils;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class DimensionTask extends QuestTask
{
	public DimensionType dimension = DimensionType.NETHER;

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
		nbt.setString("dim", dimension.getName());
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);

		try
		{
			String s = nbt.getString("dim");

			if (!s.isEmpty())
			{
				dimension = DimensionType.byName(s);
			}
			else
			{
				dimension = DimensionType.getById(nbt.getInteger("dim"));
			}
		}
		catch (Exception ex)
		{
			dimension = DimensionType.NETHER;
		}
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(dimension.getId());
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		int dim = data.readVarInt();

		try
		{
			dimension = DimensionType.getById(dim);
		}
		catch (Exception ex)
		{
			dimension = DimensionType.NETHER;
		}
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addEnum("dim", () -> dimension, v -> dimension = v, NameMap.create(DimensionType.NETHER, NameMap.ObjectProperties.withName((sender, dim) -> ServerUtils.getDimensionName(dim.getId())), DimensionType.values())).setDisplayName(new TextComponentTranslation("ftbquests.task.ftbquests.dimension"));
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		ITextComponent text;

		switch (dimension)
		{
			case OVERWORLD:
				text = new TextComponentTranslation("createWorld.customize.preset.overworld");
				break;
			case NETHER:
				text = new TextComponentTranslation("advancements.nether.root.title");
				break;
			case THE_END:
				text = new TextComponentTranslation("advancements.end.root.title");
				break;
			default:
				text = new TextComponentString(dimension.getName());
		}

		text.getStyle().setColor(TextFormatting.DARK_GREEN);
		return new TextComponentTranslation("ftbquests.task.ftbquests.dimension", text).appendText(": ").appendSibling(text);
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
			if (progress < 1L && !player.isSpectator() && player.dimension == task.dimension.getId())
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