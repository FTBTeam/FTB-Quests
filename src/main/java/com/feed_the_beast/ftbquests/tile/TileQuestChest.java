package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigTeam;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileQuestChest extends TileBase implements IItemHandler
{
	public final ConfigTeam team = new ConfigTeam("");
	public final ConfigBoolean indestructible = new ConfigBoolean(false);

	private IProgressData cTeam;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!team.isEmpty())
		{
			nbt.setString("Team", team.getString());
		}

		if (indestructible.getBoolean())
		{
			nbt.setBoolean("Indestructible", true);
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		team.setString(nbt.getString("Team"));
		indestructible.setBoolean(nbt.getBoolean("Indestructible"));
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return true;
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return (T) this;
		}

		return super.getCapability(capability, facing);
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		cTeam = null;
	}

	@Nullable
	public IProgressData getTeam()
	{
		if (team.isEmpty())
		{
			return null;
		}
		else if (cTeam == null)
		{
			cTeam = FTBQuests.PROXY.getQuestFile(world).getData(team.getString());
		}

		return cTeam;
	}


	public void openGui(EntityPlayerMP player)
	{

	}

	@Override
	public int getSlots()
	{
		return FTBQuests.PROXY.getQuestFile(world).allItemAcceptingTasks.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		QuestFile file = FTBQuests.PROXY.getQuestFile(world);

		if (slot < 0 || slot >= file.allItemAcceptingTasks.size())
		{
			return stack;
		}

		IProgressData teamData = file.getData(team.getString());

		if (teamData != null)
		{
			return teamData.getQuestTaskData(file.allItemAcceptingTasks.get(slot)).insertItem(stack, false, simulate);
		}

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