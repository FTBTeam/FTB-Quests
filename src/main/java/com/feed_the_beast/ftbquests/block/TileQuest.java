package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

/**
 * @author LatvianModder
 */
public class TileQuest extends TileBase implements IItemHandler
{
	public FTBQuestsTeamData owner;
	public ResourceLocation selectedQuest;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (type.save && owner != null)
		{
			nbt.setString("Owner", owner.team.getName());
		}

		if (selectedQuest != null)
		{
			nbt.setString("Quest", selectedQuest.toString());
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (type.save)
		{
			owner = FTBQuestsTeamData.get(Universe.get().getTeam(nbt.getString("Owner")));
		}

		selectedQuest = nbt.hasKey("Quest") ? new ResourceLocation(nbt.getString("Quest")) : null;
	}

	public void onRightClick(EntityPlayer player)
	{
		if (owner == null)
		{
			owner = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);
		}

		//TODO: Open quest selector
	}

	@Override
	public int getSlots()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}
}