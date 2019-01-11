package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.item.ItemLootCrate;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * @author LatvianModder
 */
public class TileLootCrateStorage extends TileBase implements IItemHandler
{
	public Object2IntOpenHashMap<String> crates = new Object2IntOpenHashMap<>();
	private ItemStack tempLootcrate = new ItemStack(FTBQuestsItems.LOOTCRATE);

	public TileLootCrateStorage()
	{
	}

	public TileLootCrateStorage(World world)
	{
		QuestFile file = FTBQuests.PROXY.getQuestFile(world);

		for (RewardTable table : file.rewardTables)
		{
			if (table.lootCrate != null && !table.lootCrate.stringID.isEmpty())
			{
				crates.put(table.lootCrate.stringID, 0);
			}
		}
	}

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (type.full && !crates.isEmpty())
		{
			NBTTagCompound nbt1 = new NBTTagCompound();

			for (Object2IntOpenHashMap.Entry<String> entry : crates.object2IntEntrySet())
			{
				nbt1.setInteger(entry.getKey(), entry.getIntValue());
			}

			nbt.setTag("crates", nbt1);
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (type.full)
		{
			NBTTagCompound nbt1 = nbt.getCompoundTag("crates");
			crates = new Object2IntOpenHashMap<>(nbt1.getSize());

			for (String s : nbt1.getKeySet())
			{
				crates.put(s, nbt1.getInteger(s));
			}
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) this : super.getCapability(capability, facing);
	}

	@Override
	public int getSlots()
	{
		return 1 + crates.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		if (slot <= 0 || slot - 1 >= crates.size())
		{
			return ItemStack.EMPTY;
		}

		int i = 0;

		Iterator<Object2IntOpenHashMap.Entry<String>> iterator = crates.object2IntEntrySet().fastIterator();

		while (iterator.hasNext())
		{
			Object2IntOpenHashMap.Entry<String> entry = iterator.next();

			if (slot == i)
			{
				if (entry.getIntValue() <= 0)
				{
					return ItemStack.EMPTY;
				}

				tempLootcrate.setTagInfo("type", new NBTTagString(entry.getKey()));
				tempLootcrate.setCount(entry.getIntValue());
				return tempLootcrate;
			}

			i++;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (slot != 0)
		{
			return stack;
		}

		LootCrate crate = ItemLootCrate.getCrate(world, stack);

		if (crate == null)
		{
			return stack;
		}

		if (!simulate && !world.isRemote)
		{
			crates.put(crate.stringID, crates.getInt(crate.stringID) + stack.getCount());
			markDirty();
		}

		return ItemStack.EMPTY;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return slot == 0 && ItemLootCrate.getCrate(world, stack) != null;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (slot == 0 || amount <= 0)
		{
			return ItemStack.EMPTY;
		}

		int i = 1;

		Iterator<Object2IntOpenHashMap.Entry<String>> iterator = crates.object2IntEntrySet().fastIterator();

		while (iterator.hasNext())
		{
			Object2IntOpenHashMap.Entry<String> entry = iterator.next();

			if (slot == i)
			{
				if (entry.getIntValue() <= 0)
				{
					return ItemStack.EMPTY;
				}

				int a = Math.min(64, Math.min(entry.getIntValue(), amount));
				ItemStack stack = new ItemStack(FTBQuestsItems.LOOTCRATE);
				stack.setCount(a);
				stack.setTagInfo("type", new NBTTagString(entry.getKey()));

				if (!simulate)
				{
					crates.put(entry.getKey(), entry.getIntValue() - a);
					markDirty();
				}

				return stack;
			}

			i++;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public void markDirty()
	{
		sendDirtyUpdate();
	}
}