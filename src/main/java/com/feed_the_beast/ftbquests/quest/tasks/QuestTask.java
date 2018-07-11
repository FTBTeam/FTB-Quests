package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.events.QuestTaskEvent;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestTask extends ProgressingQuestObject
{
	public static QuestTask createTask(Quest quest, int id, NBTTagCompound nbt)
	{
		if (nbt.hasKey("item"))
		{
			int count = nbt.hasKey("count") ? nbt.getInteger("count") : 1;

			if (count <= 0)
			{
				throw new IllegalArgumentException("count can't be <= 0!");
			}

			List<ItemStack> list = new ArrayList<>();

			if (nbt.hasKey("item", Constants.NBT.TAG_LIST))
			{
				NBTTagList list1 = nbt.getTagList("item", Constants.NBT.TAG_COMPOUND);

				for (int i = 0; i < list1.tagCount(); i++)
				{
					ItemStack stack = new ItemStack(list1.getCompoundTagAt(i));

					if (!stack.isEmpty())
					{
						list.add(stack);
					}
				}
			}
			else
			{
				ItemStack stack = new ItemStack(nbt.getCompoundTag("item"));

				if (!stack.isEmpty())
				{
					list.add(stack);
				}
			}

			return new ItemTask(quest, id, list, count);
		}
		else if (nbt.hasKey("fluid"))
		{
			int amount = nbt.hasKey("amount") ? nbt.getInteger("amount") : 1000;

			if (amount > 0)
			{
				Fluid fluid = FluidRegistry.getFluid(nbt.getString("fluid"));

				if (fluid != null)
				{
					return new FluidTask(quest, id, new FluidStack(fluid, amount, nbt.hasKey("nbt") ? nbt.getCompoundTag("nbt") : null));
				}
			}
		}
		else if (nbt.hasKey("forge_energy"))
		{
			int energy = nbt.getInteger("forge_energy");

			if (energy > 0)
			{
				return new ForgeEnergyTask(quest, id, energy);
			}
		}

		QuestTaskEvent event = new QuestTaskEvent(quest, id, nbt);
		event.post();
		return event.getTask() == null ? new UnknownTask(quest, id, nbt) : event.getTask();
	}

	public final Quest quest;

	public QuestTask(Quest q, int id)
	{
		super(id);
		quest = q;
	}

	@Override
	public QuestList getQuestList()
	{
		return quest.getQuestList();
	}

	@Override
	public final int getProgress(IProgressData data)
	{
		return data.getQuestTaskData(id).getProgress();
	}

	@Override
	public int getMaxProgress()
	{
		return 1;
	}

	@Override
	public final void resetProgress(IProgressData data)
	{
		data.getQuestTaskData(id).setProgress(0, false);
	}

	public abstract QuestTaskData createData(IProgressData data);

	public abstract Icon getIcon();

	@SideOnly(Side.CLIENT)
	public abstract String getDisplayName();

	public abstract void writeData(NBTTagCompound nbt);
}