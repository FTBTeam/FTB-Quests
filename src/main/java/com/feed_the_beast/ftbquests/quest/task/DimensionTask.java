package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigLong;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.ServerUtils;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class DimensionTask extends QuestTask implements ISingleLongValueTask
{
	public int dimension = 1;

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
		dimension = nbt.getInteger("dim");
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
	public ConfigLong getDefaultValue()
	{
		return new ConfigLong(dimension, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public void setValue(long v)
	{
		dimension = (int) v;
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("dim", () -> dimension, v -> dimension = v, -1, Integer.MIN_VALUE, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.task.ftbquests.dimension"));
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		ITextComponent text = ServerUtils.getDimensionName(dimension);
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
			if (progress < 1L && player.dimension == task.dimension)
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