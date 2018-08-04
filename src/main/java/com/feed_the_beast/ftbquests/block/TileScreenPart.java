package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileScreenPart extends TileScreenBase
{
	private byte offX, offY, offZ;
	private TileScreen parent;

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
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		parent = null;
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
	public TileScreen getScreen()
	{
		if (parent == null)
		{
			TileEntity tileEntity = world.getTileEntity(pos.add(-offX, -offY, -offZ));

			if (tileEntity instanceof TileScreen)
			{
				parent = (TileScreen) tileEntity;
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