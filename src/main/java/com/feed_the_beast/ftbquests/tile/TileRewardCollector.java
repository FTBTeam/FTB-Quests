package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class TileRewardCollector extends TileBase implements IItemHandler
{
	public UUID owner = null;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!type.save)
		{
			return;
		}

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
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return extractItem(0, 1, true);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		return stack;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return false;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (slot != 0 || amount <= 0 || owner == null || world == null || world.isRemote)
		{
			return ItemStack.EMPTY;
		}

		ServerQuestData data = ServerQuestFile.INSTANCE.getData(owner);

		if (data != null)
		{
			EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(owner);

			for (Chapter chapter : ServerQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.canRepeat && quest.rewards.size() > 0 && quest.isComplete(data))
					{
						for (Reward reward : quest.rewards)
						{
							if (!data.isRewardClaimed(owner, reward))
							{
								Optional<ItemStack> r = reward.claimAutomated(this, owner, player, simulate);

								if (r.isPresent())
								{
									if (!simulate)
									{
										data.setRewardClaimed(owner, reward);
									}

									return r.get();
								}
							}
						}
					}
				}
			}
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