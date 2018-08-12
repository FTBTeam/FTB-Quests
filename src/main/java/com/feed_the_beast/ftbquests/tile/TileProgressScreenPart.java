package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileProgressScreenPart extends TileProgressScreenBase
{
	private byte offX, offY, offZ;
	private TileProgressScreenCore parent;

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
	public TileProgressScreenCore getScreen()
	{
		if (parent == null || parent.isInvalid())
		{
			parent = null;

			TileEntity tileEntity = world.getTileEntity(pos.add(-offX, -offY, -offZ));

			if (tileEntity instanceof TileProgressScreenCore)
			{
				parent = (TileProgressScreenCore) tileEntity;
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