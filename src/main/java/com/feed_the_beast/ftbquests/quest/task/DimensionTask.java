package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class DimensionTask extends Task
{
	public int dimension = -1;

	public DimensionTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.DIMENSION;
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

		if (dimension == 0 && nbt.hasKey("dim", Constants.NBT.TAG_STRING))
		{
			try
			{
				dimension = DimensionType.byName(nbt.getString("dim")).getId();
			}
			catch (Exception ex)
			{
				dimension = -1;
			}
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
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("dim", () -> dimension, v -> dimension = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.task.ftbquests.dimension"));
	}

	public static String getDimensionName(int dim)
	{
		switch (dim)
		{
			case 0:
				return I18n.format("createWorld.customize.preset.overworld");
			case -1:
				return I18n.format("advancements.nether.root.title");
			case 1:
				return I18n.format("advancements.end.root.title");
			default:
				for (DimensionType type : DimensionType.values())
				{
					if (type.getId() == dim)
					{
						return type.getName();
					}
				}

				return "dim_" + dim;
		}
	}

	@Override
	public String getAltTitle()
	{
		return I18n.format("ftbquests.task.ftbquests.dimension") + ": " + TextFormatting.DARK_GREEN + getDimensionName(dimension);
	}

	@Override
	public int autoSubmitOnPlayerTick()
	{
		return 20;
	}

	@Override
	public TaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<DimensionTask>
	{
		private Data(DimensionTask task, QuestData data)
		{
			super(task, data);
		}

		@Override
		public boolean canSubmit(EntityPlayerMP player)
		{
			return player.dimension == task.dimension && !player.isSpectator();
		}
	}
}