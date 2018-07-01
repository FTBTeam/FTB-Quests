package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftbquests.gui.ContainerTaskBase;
import com.feed_the_beast.ftbquests.gui.GuiTaskBase;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public abstract class QuestTaskData<T extends QuestTask> implements ICapabilityProvider
{
	public final T task;
	public final IProgressData data;
	private int progress = 0;

	public QuestTaskData(T q, IProgressData d)
	{
		task = q;
		data = d;
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		if (progress > 0)
		{
			nbt.setInteger("Progress", progress);
		}
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		progress = nbt.getInteger("Progress");
	}

	public final int getProgress()
	{
		return progress;
	}

	public final boolean setProgress(int p, boolean simulate)
	{
		p = Math.max(0, p);

		if (progress == p)
		{
			return false;
		}
		else if (simulate)
		{
			return true;
		}

		progress = p;
		data.syncTaskProgress(task, progress);
		return true;
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
}