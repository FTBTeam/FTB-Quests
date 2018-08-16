package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class TileQuestChest extends TileBase
{
	public final ConfigBoolean indestructible = new ConfigBoolean(false);

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (indestructible.getBoolean())
		{
			nbt.setBoolean("Indestructible", true);
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		indestructible.setBoolean(nbt.getBoolean("Indestructible"));
	}
}