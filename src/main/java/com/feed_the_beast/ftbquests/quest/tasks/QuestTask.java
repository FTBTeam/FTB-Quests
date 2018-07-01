package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftbquests.events.QuestTaskEvent;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestList;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class QuestTask extends ProgressingQuestObject
{
	@Nullable
	public static QuestTask createTask(Quest quest, int id, JsonObject json)
	{
		if (json.has("item"))
		{
			int count = json.has("count") ? json.get("count").getAsInt() : 1;

			if (count > 0)
			{
				ItemTask.QuestItem item = ItemTask.QuestItem.fromJson(json.get("item"));

				if (!item.isEmpty())
				{
					return new ItemTask(quest, id, item, count);
				}
			}

			return null;
		}
		else if (json.has("fluid"))
		{
			int amount = json.has("amount") ? json.get("amount").getAsInt() : 1000;

			if (amount > 0)
			{
				Fluid fluid = FluidRegistry.getFluid(json.get("fluid").getAsString());

				if (fluid != null)
				{
					return new FluidTask(quest, id, new FluidStack(fluid, amount, json.has("nbt") ? (NBTTagCompound) JsonUtils.toNBT(json.get("nbt")) : null));
				}
			}

			return null;
		}
		else if (json.has("forge_energy"))
		{
			int energy = json.get("forge_energy").getAsInt();

			if (energy > 0)
			{
				return new ForgeEnergyTask(quest, id, energy);
			}
		}

		QuestTaskEvent event = new QuestTaskEvent(quest, id, json);
		event.post();
		return event.getTask();
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
	public String getDisplayName()
	{
		return toJson().toString();
	}

	public abstract JsonObject toJson();
}