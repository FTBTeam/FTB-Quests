package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.item.ItemEntry;
import com.feed_the_beast.ftblib.lib.item.ItemEntryWithCount;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.item.ItemLootCrate;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class TileLootCrateOpener extends TileBase implements IItemHandler
{
	public List<ItemEntryWithCount> items = new ArrayList<>();
	public UUID owner = null;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!type.save)
		{
			return;
		}

		NBTTagList itemsTag = new NBTTagList();

		for (ItemEntryWithCount entry : items)
		{
			if (!entry.isEmpty())
			{
				itemsTag.appendTag(entry.serializeNBT());
			}
		}

		nbt.setTag("items", itemsTag);

		if (owner != null && !type.item)
		{
			nbt.setString("owner", StringUtils.fromUUID(owner));
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!type.save)
		{
			return;
		}

		NBTTagList itemsTag = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND);

		items = new ArrayList<>(itemsTag.tagCount());

		for (int i = 0; i < itemsTag.tagCount(); i++)
		{
			ItemEntryWithCount entry = new ItemEntryWithCount(itemsTag.get(i));

			if (!entry.isEmpty())
			{
				items.add(entry);
			}
		}

		owner = nbt.hasKey("owner") ? StringUtils.fromString(nbt.getString("owner")) : null;
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
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return slot == 0 || items.isEmpty() ? ItemStack.EMPTY : items.get(0).getStack(false);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (slot != 0 || world == null || world.isRemote)
		{
			return stack;
		}

		LootCrate crate = ItemLootCrate.getCrate(world, stack);

		if (crate == null)
		{
			return stack;
		}

		int totalWeight = crate.table.getTotalWeight(true);
		EntityPlayerMP player = owner == null ? null : world.getMinecraftServer().getPlayerList().getPlayerByUUID(owner);
		boolean update = false;

		if (totalWeight > 0)
		{
			for (int j = 0; j < stack.getCount() * crate.table.lootSize; j++)
			{
				int number = world.rand.nextInt(totalWeight) + 1;
				int currentWeight = crate.table.emptyWeight;

				if (currentWeight < number)
				{
					for (WeightedReward reward : crate.table.rewards)
					{
						currentWeight += reward.weight;

						if (currentWeight >= number)
						{
							List<ItemStack> stacks = new ArrayList<>();

							if (reward.reward.automatedClaimPre(this, stacks, world.rand, owner, player))
							{
								update = true;

								if (!simulate)
								{
									ItemEntry entry = ItemEntry.get(stack);

									for (ItemEntryWithCount entry1 : items)
									{
										if (entry1.entry.equalsEntry(entry))
										{
											entry1.count += stack.getCount();
											return ItemStack.EMPTY;
										}
									}

									items.add(new ItemEntryWithCount(entry, stack.getCount()));
									reward.reward.automatedClaimPost(this, owner, player);
								}
							}

							break;
						}
					}
				}
			}
		}

		if (update && !simulate)
		{
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
		if (slot == 0 || amount <= 0 || items.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		ItemEntryWithCount entry = items.get(0);
		ItemStack stack1 = entry.getStack(true);
		int a = Math.min(entry.count, Math.min(amount, stack1.getMaxStackSize()));
		stack1.setCount(a);

		if (!simulate && !world.isRemote)
		{
			entry.count -= a;

			if (entry.isEmpty())
			{
				items.remove(0);
			}

			markDirty();
		}

		return stack1;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public void markDirty()
	{
		sendDirtyUpdate();
	}
}