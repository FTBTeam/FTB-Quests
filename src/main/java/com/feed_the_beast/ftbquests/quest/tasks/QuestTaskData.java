package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftbquests.gui.ContainerTaskBase;
import com.feed_the_beast.ftbquests.gui.GuiTaskBase;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class QuestTaskData<T extends QuestTask> implements ICapabilityProvider
{
	public final T task;
	public final IProgressData data;

	public QuestTaskData(T q, IProgressData d)
	{
		task = q;
		data = d;
	}

	@Nullable
	public abstract NBTBase toNBT();

	public abstract void fromNBT(@Nullable NBTBase nbt);

	public abstract int getProgress();

	public abstract void resetProgress();

	public double getRelativeProgress()
	{
		int max = task.getMaxProgress();

		if (max == 0)
		{
			return 0D;
		}

		int progress = getProgress();

		if (progress >= max)
		{
			return 1D;
		}

		return (double) progress / (double) max;
	}

	public String getProgressString()
	{
		return getProgress() + " / " + task.getMaxProgress();
	}

	public ContainerTaskBase getContainer(EntityPlayer player)
	{
		return new ContainerTaskBase(player, this);
	}

	@SideOnly(Side.CLIENT)
	public GuiTaskBase getGui(ContainerTaskBase container)
	{
		return new GuiTaskBase(container);
	}

	public String toString()
	{
		return data + ":" + task.id;
	}
}