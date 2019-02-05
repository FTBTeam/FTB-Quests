package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigTeam;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileWithTeam extends TileBase
{
	public String team = "";
	public boolean indestructible = false;

	protected ITeamData cTeam;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!team.isEmpty())
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

	@Override
	public void writeToItem(ItemStack stack)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeData(nbt, EnumSaveType.ITEM);

		if (!nbt.isEmpty())
		{
			stack.setTagCompound(nbt);
		}
	}

	@Override
	public void readFromItem(ItemStack stack)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		readData(nbt == null ? new NBTTagCompound() : nbt, EnumSaveType.ITEM);
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		cTeam = null;
	}

	@Nullable
	public final ITeamData getTeam()
	{
		if (team.isEmpty())
		{
			return null;
		}
		else if (cTeam == null && world != null)
		{
			cTeam = FTBQuests.PROXY.getQuestFile(world).getData(team);
		}

		return cTeam;
	}

	public final boolean isOwner(EntityPlayer player)
	{
		return team.isEmpty() || FTBLibAPI.getTeam(player.getUniqueID()).equals(team);
	}

	protected ConfigTeam createTeamConfig()
	{
		return new ConfigTeam(() -> Universe.get().getTeam(team), v -> {
			team = v.getID();
			cTeam = null;
		});
	}

	public void setIDFromPlacer(EntityLivingBase placer)
	{
		if (team.isEmpty() && placer instanceof EntityPlayerMP)
		{
			team = FTBLibAPI.getTeam(placer.getUniqueID());
		}
	}
}