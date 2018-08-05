package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageOpenTask;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileScreen extends TileScreenBase
{
	public EnumFacing cachedFacing;
	public String owner = "";
	public short task = 0;
	public int size = 0;
	public int amountMode = 0;

	private IProgressData cachedOwner;
	private QuestTaskData cachedTaskData;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		nbt.setString("Owner", owner);
		nbt.setShort("Task", task);
		nbt.setByte("Size", (byte) size);
		nbt.setByte("AmountMode", (byte) amountMode);
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		owner = nbt.getString("Owner");
		task = nbt.getShort("Task");
		size = nbt.getByte("Size");
		amountMode = nbt.getByte("AmountMode");
	}

	@Override
	public TileScreen getScreen()
	{
		return this;
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		cachedFacing = null;
		cachedOwner = null;
		cachedTaskData = null;
	}

	public EnumFacing getFacing()
	{
		if (cachedFacing == null)
		{
			cachedFacing = getBlockState().getValue(BlockHorizontal.FACING);
		}

		return cachedFacing;
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
			cachedOwner = FTBQuests.PROXY.getQuestList(world.isRemote).getData(owner);
		}

		return cachedOwner;
	}

	@Nullable
	public QuestTaskData getTaskData()
	{
		if (task == 0 || owner.isEmpty())
		{
			return null;
		}
		else if (cachedTaskData == null || cachedTaskData.task.invalid)
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
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return new AxisAlignedBB(pos.add(-size, 0, -size), pos.add(size + 1, size * 2 + 1, size + 1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		double d = 32D * (2 + size);
		return d * d;
	}

	public void onClicked(EntityPlayer player, double x, double y)
	{
		if (y >= 0.81D)
		{
			amountMode++;

			if (amountMode == 3)
			{
				amountMode = 0;
			}

			markDirty();
		}
		else if (y >= 0.29D && !world.isRemote)
		{
			cachedTaskData = getTaskData();

			if (cachedTaskData != null)
			{
				MessageOpenTask.openGUI(cachedTaskData, (EntityPlayerMP) player, null);
			}
		}
	}
}