package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class QuestTasks
{
	public interface TaskProvider
	{
		@Nullable
		QuestTask create(Quest quest, int id, NBTTagCompound nbt);
	}

	public static final Map<String, TaskProvider> MAP = new HashMap<>();

	public static void add(String type, TaskProvider provider)
	{
		MAP.put(type, provider);
	}

	public static void init()
	{
		add("item", (quest, id, nbt) -> {
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

			return new ItemTask(quest, id, list, nbt.hasKey("count") ? nbt.getInteger("count") : 1);
		});

		add("fluid", (quest, id, nbt) -> {
			Fluid fluid = FluidRegistry.getFluid(nbt.getString("fluid"));

			if (fluid != null)
			{
				return new FluidTask(quest, id, new FluidStack(fluid, nbt.hasKey("amount") ? nbt.getInteger("amount") : 1000, nbt.hasKey("nbt") ? nbt.getCompoundTag("nbt") : null));
			}

			return null;
		});

		add("forge_energy", (quest, id, nbt) -> new ForgeEnergyTask(quest, id, nbt.getInteger("value")));
	}

	public static QuestTask createTask(Quest quest, int id, NBTTagCompound nbt)
	{
		TaskProvider provider = MAP.get(nbt.getString("type"));

		if (provider != null)
		{
			QuestTask task = provider.create(quest, id, nbt);

			if (task != null)
			{
				return task;
			}
		}

		return new UnknownTask(quest, id, nbt);
	}
}