package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileTaskScreenPart extends TileBase implements ITaskScreen
{
	private byte offX, offY, offZ;
	private TileTaskScreenCore parent;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		nbt.setByte("OffsetX", offX);
		nbt.setByte("OffsetY", offY);
		nbt.setByte("OffsetZ", offZ);
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		offX = nbt.getByte("OffsetX");
		offY = nbt.getByte("OffsetY");
		offZ = nbt.getByte("OffsetZ");
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		TileTaskScreenCore screen = getScreen();
		return (screen != null && screen.hasCapability(capability, facing)) || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		TileTaskScreenCore screen = getScreen();
		T object = screen != null ? screen.getCapability(capability, facing) : null;
		return object != null ? object : super.getCapability(capability, facing);
	}

	@Override
	public void markDirty()
	{
		world.markChunkDirty(pos, this);

		parent = getScreen();

		if (parent != null)
		{
			parent.markDirty();
		}
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		parent = null;
	}

	@Override
	protected boolean notifyBlock()
	{
		return !world.isRemote;
	}

	@Override
	public boolean canBeWrenched(EntityPlayer player)
	{
		return false;
	}

	@Override
	public int getOffsetX()
	{
		return offX;
	}

	@Override
	public int getOffsetY()
	{
		return offY;
	}

	@Override
	public int getOffsetZ()
	{
		return offZ;
	}

	@Override
	@Nullable
	public TileTaskScreenCore getScreen()
	{
		if (parent == null || parent.isInvalid())
		{
			parent = null;

			TileEntity tileEntity = world.getTileEntity(pos.add(-offX, -offY, -offZ));

			if (tileEntity instanceof TileTaskScreenCore)
			{
				parent = (TileTaskScreenCore) tileEntity;
			}
		}

		return parent;
	}

	public void setOffset(int x, int y, int z)
	{
		offX = (byte) x;
		offY = (byte) y;
		offZ = (byte) z;
		parent = null;
		markDirty();
	}
}