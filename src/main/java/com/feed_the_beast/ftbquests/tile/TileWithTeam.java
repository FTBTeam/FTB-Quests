package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigTeam;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class TileWithTeam extends TileBase
{
	public String team = "";
	public boolean indestructible = false;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!team.isEmpty() && !type.item)
		{
			nbt.setString("Team", team);
		}

		if (indestructible)
		{
			nbt.setBoolean("Indestructible", true);
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		team = nbt.getString("Team");
		indestructible = nbt.getBoolean("Indestructible");
		updateContainingBlockInfo();
	}

	public final boolean isOwner(EntityPlayer player)
	{
		return team.isEmpty() || FTBLibAPI.getTeam(player.getUniqueID()).equals(team);
	}

	protected ConfigTeam createTeamConfig()
	{
		return new ConfigTeam(() -> Universe.get().getTeam(team), v -> team = v.getID());
	}

	public void setIDFromPlacer(EntityLivingBase placer)
	{
		if (team.isEmpty() && placer instanceof EntityPlayerMP)
		{
			team = FTBLibAPI.getTeam(placer.getUniqueID());
		}
	}
}