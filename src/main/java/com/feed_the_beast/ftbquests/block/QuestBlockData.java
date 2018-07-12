package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.tile.IChangeCallback;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class QuestBlockData implements ICapabilitySerializable<NBTTagCompound>
{
	@CapabilityInject(QuestBlockData.class)
	public static Capability<QuestBlockData> CAP;

	public static QuestBlockData get(ItemStack stack)
	{
		QuestBlockData data = stack.getCapability(CAP, null);

		if (NBTUtils.hasBlockData(stack))
		{
			data.deserializeNBT(NBTUtils.getBlockData(stack));
			NBTUtils.removeBlockData(stack);
		}

		return data;
	}

	@Nullable
	public static QuestBlockData get(@Nullable ICapabilityProvider provider)
	{
		return provider == null ? null : provider.getCapability(CAP, null);
	}

	private final IChangeCallback callback;

	private String owner = "";
	private int task = 0;

	private IProgressData cachedOwner;
	private QuestTaskData cachedTaskData;

	public QuestBlockData(@Nullable IChangeCallback c)
	{
		callback = c;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if (capability == CAP)
		{
			return true;
		}

		cachedTaskData = getTaskData();
		return cachedTaskData != null && cachedTaskData.task.getMaxProgress() > 0 && cachedTaskData.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if (capability == CAP)
		{
			return (T) this;
		}

		cachedTaskData = getTaskData();
		return cachedTaskData != null && cachedTaskData.task.getMaxProgress() > 0 ? cachedTaskData.getCapability(capability, facing) : null;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();

		if (!owner.isEmpty())
		{
			nbt.setString("Owner", owner);
		}

		if (task > 0)
		{
			nbt.setInteger("Task", task);
		}

		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		owner = nbt.getString("Owner");
		task = nbt.getInteger("Task");
		cachedTaskData = null;
	}

	public void clearCache()
	{
		cachedOwner = null;
		cachedTaskData = null;
	}

	public void copyFrom(QuestBlockData data)
	{
		owner = data.owner;
		task = data.task;
		cachedOwner = data.cachedOwner;
		cachedTaskData = data.cachedTaskData;
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
			if (callback instanceof TileEntity)
			{
				cachedOwner = FTBQuests.PROXY.getQuestList(((TileEntity) callback).getWorld().isRemote).getData(owner);
			}
			else
			{
				cachedOwner = FTBQuests.PROXY.getQuestList(FMLCommonHandler.instance().getEffectiveSide().isClient()).getData(owner);
			}
		}

		return cachedOwner;
	}

	public void setOwner(String team)
	{
		owner = team;
		clearCache();

		if (callback != null)
		{
			callback.onContentsChanged(true);
		}
	}

	public int getTaskID()
	{
		return task;
	}

	public void setTask(int id)
	{
		task = id;
		clearCache();

		if (callback != null)
		{
			callback.onContentsChanged(true);
		}
	}

	@Nullable
	public QuestTaskData getTaskData()
	{
		if (task <= 0 || owner.isEmpty())
		{
			return null;
		}
		else if (cachedTaskData == null || cachedTaskData.task.isInvalid())
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
}