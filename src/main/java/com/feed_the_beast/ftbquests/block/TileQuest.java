package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsConfig;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileQuest extends TileBase
{
	private String owner = "";
	private int task = 0;
	private boolean canEdit = FTBQuestsConfig.general.default_can_edit;

	private IProgressData cachedOwner;
	private QuestTaskData cachedTaskData;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!owner.isEmpty())
		{
			nbt.setString("Owner", owner);
		}

		if (task > 0)
		{
			nbt.setInteger("Task", task);
		}

		if (canEdit != FTBQuestsConfig.general.default_can_edit)
		{
			nbt.setBoolean("CanEdit", canEdit);
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		owner = nbt.getString("Owner");
		task = nbt.getInteger("Task");
		canEdit = nbt.hasKey("CanEdit") ? nbt.getBoolean("CanEdit") : FTBQuestsConfig.general.default_can_edit;
		updateContainingBlockInfo();
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		cachedOwner = null;
		cachedTaskData = null;
	}

	@Nullable
	public QuestTaskData getTaskData()
	{
		if (task <= 0 || owner.isEmpty())
		{
			return null;
		}
		else if (cachedTaskData != null && cachedTaskData.task.isInvalid())
		{
			cachedTaskData = null;
		}

		if (cachedTaskData == null)
		{
			cachedOwner = getOwner();

			if (cachedOwner == null)
			{
				return null;
			}

			cachedTaskData = cachedOwner.getQuestTaskData(task);
		}

		return cachedTaskData;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		cachedTaskData = getTaskData();
		return cachedTaskData != null && cachedTaskData.hasCapability(capability, facing) || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		cachedTaskData = getTaskData();
		return cachedTaskData != null ? cachedTaskData.getCapability(capability, facing) : super.getCapability(capability, facing);
	}

	@Override
	public void markDirty()
	{
		sendDirtyUpdate();
	}

	public String getOwnerTeam()
	{
		return owner;
	}

	@Nullable
	public IProgressData getOwner()
	{
		if (owner.isEmpty())
		{
			return null;
		}
		else if (cachedOwner == null)
		{
			cachedOwner = FTBQuests.PROXY.getOwner(owner, !isServerSide());
		}

		return cachedOwner;
	}

	public void setOwner(String team)
	{
		owner = team;
		updateContainingBlockInfo();
		markDirty();
	}

	public int getTaskID()
	{
		return task;
	}

	public void setTask(int id)
	{
		task = id;
		updateContainingBlockInfo();
		markDirty();
	}

	public boolean canEdit()
	{
		return canEdit;
	}

	@Override
	public boolean shouldDrop()
	{
		return canEdit != FTBQuestsConfig.general.default_can_edit || getTaskData() != null;
	}
}