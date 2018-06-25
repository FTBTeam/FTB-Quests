package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftblib.lib.util.InvUtils;
import com.feed_the_beast.ftbquests.FTBQuestsConfig;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskKey;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileQuest extends TileBase implements ITickable, IItemHandlerModifiable
{
	private FTBQuestsTeamData owner;
	private String ownerName = "";
	private QuestTask task;
	private QuestTaskKey taskName = null;
	private boolean canEdit = FTBQuestsConfig.general.default_can_edit;
	private ItemStack storedItem = ItemStack.EMPTY;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!ownerName.isEmpty())
		{
			nbt.setString("Owner", ownerName);
		}

		if (taskName != null)
		{
			nbt.setString("Quest", taskName.quest.toString());
			nbt.setInteger("Task", taskName.index);
		}

		if (canEdit != FTBQuestsConfig.general.default_can_edit)
		{
			nbt.setBoolean("CanEdit", canEdit);
		}

		if (!storedItem.isEmpty())
		{
			nbt.setTag("Item", storedItem.serializeNBT());
		}
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		task = null;
		owner = null;
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		owner = null;
		ownerName = nbt.getString("Owner");
		task = null;
		String questName = nbt.getString("Quest");
		taskName = questName.isEmpty() ? null : new QuestTaskKey(new ResourceLocation(questName), nbt.getInteger("Task"));
		canEdit = nbt.hasKey("CanEdit") ? nbt.getBoolean("CanEdit") : FTBQuestsConfig.general.default_can_edit;
		storedItem = nbt.hasKey("Item") ? new ItemStack(nbt.getCompoundTag("Item")) : ItemStack.EMPTY;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return true;
		}

		QuestTask task = getTask();
		return task != null && task.hasCapability(capability, facing) || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return (T) this;
		}

		QuestTask task = getTask();
		return task != null && task.hasCapability(capability, facing) ? task.getCapability(capability, facing) : super.getCapability(capability, facing);
	}

	@Override
	public void update()
	{
		if (!world.isRemote && !storedItem.isEmpty())
		{
			QuestTask task = getTask();

			if (task != null)
			{
				FTBQuestsTeamData data = getOwner();

				if (data != null)
				{
					ItemStack result = task.processItem(data, storedItem);

					if (storedItem.getCount() != result.getCount() || !InvUtils.stacksAreEqual(storedItem, result))
					{
						storedItem = result.isEmpty() ? ItemStack.EMPTY : result;
						markDirty();
					}
				}
			}
		}

		checkIfDirty();
	}

	@Nullable
	public FTBQuestsTeamData getOwner()
	{
		if (ownerName.isEmpty())
		{
			return null;
		}
		else if (owner == null)
		{
			ForgeTeam team = Universe.get().getTeam(ownerName);

			if (team.isValid())
			{
				owner = FTBQuestsTeamData.get(team);
			}

			if (owner == null)
			{
				setOwner("");
			}
		}

		return owner;
	}

	public void setOwner(String team)
	{
		owner = null;
		ownerName = team;
		markDirty();
	}

	@Nullable
	public QuestTask getTask()
	{
		if (taskName == null)
		{
			return null;
		}
		else if (task == null)
		{
			setTask(ServerQuestList.INSTANCE.getTask(taskName));
		}

		return task;
	}

	public void setTask(@Nullable QuestTask q)
	{
		task = q;
		taskName = task == null ? null : task.key;
		markDirty();
	}

	public boolean canEdit()
	{
		return canEdit;
	}

	@Override
	public boolean shouldDrop()
	{
		return taskName != null || canEdit != FTBQuestsConfig.general.default_can_edit || !storedItem.isEmpty();
	}

	@Override
	public void setStackInSlot(int slot, ItemStack is)
	{
		storedItem = is;
		markDirty();
	}

	@Override
	public int getSlots()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return storedItem;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (stack.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		ItemStack existing = storedItem;
		int limit = stack.getMaxStackSize();

		if (!existing.isEmpty())
		{
			if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
			{
				return stack;
			}

			limit -= existing.getCount();
		}

		if (limit <= 0)
		{
			return stack;
		}

		boolean reachedLimit = stack.getCount() > limit;

		if (!simulate)
		{
			if (existing.isEmpty())
			{
				storedItem = reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack;
			}
			else
			{
				existing.grow(reachedLimit ? limit : stack.getCount());
			}

			markDirty();
		}

		return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (amount <= 0 || storedItem.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		int toExtract = Math.min(amount, storedItem.getMaxStackSize());

		if (storedItem.getCount() <= toExtract)
		{
			ItemStack stack = storedItem;

			if (!simulate)
			{
				storedItem = ItemStack.EMPTY;
				markDirty();
			}

			return stack;
		}
		else
		{
			if (!simulate)
			{
				storedItem = ItemHandlerHelper.copyStackWithSize(storedItem, storedItem.getCount() - toExtract);
				markDirty();
			}

			return ItemHandlerHelper.copyStackWithSize(storedItem, toExtract);
		}
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}
}