package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileQuest extends TileBase
{
	protected final QuestBlockData data = new QuestBlockData(this);

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		NBTUtils.copyTags(data.serializeNBT(), nbt);
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		data.deserializeNBT(nbt);
		data.clearCache();
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		data.clearCache();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		return data.hasCapability(capability, facing) || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		T object = data.getCapability(capability, facing);
		return object != null ? object : super.getCapability(capability, facing);
	}

	@Override
	public void markDirty()
	{
		sendDirtyUpdate();
	}

	@Override
	public boolean shouldDrop()
	{
		return data.getTaskData() != null;
	}
}