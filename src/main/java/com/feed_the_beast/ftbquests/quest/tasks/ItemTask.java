package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;
import com.feed_the_beast.ftbquests.gui.ContainerItemTask;
import com.feed_the_beast.ftbquests.gui.ContainerTaskBase;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class ItemTask extends QuestTask implements Predicate<ItemStack>
{
	private final List<ItemStack> items;
	private final int count;
	private Icon icon = null;

	public ItemTask(Quest quest, int id, List<ItemStack> i, int c)
	{
		super(quest, id);
		items = i;
		count = c;
	}

	@Override
	public int getMaxProgress()
	{
		return count;
	}

	@Override
	public Icon getIcon()
	{
		if (icon == null)
		{
			List<Icon> icons = new ArrayList<>();

			for (ItemStack stack : items)
			{
				Icon icon = ItemIcon.getItemIcon(stack);

				if (!icon.isEmpty())
				{
					icons.add(icon);
				}
			}

			icon = icons.isEmpty() ? Icon.EMPTY : icons.size() == 1 ? icons.get(0) : new IconAnimation(icons);
		}

		return icon;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		NBTTagList list = new NBTTagList();

		for (ItemStack stack : items)
		{
			if (!stack.isEmpty())
			{
				list.appendTag(stack.serializeNBT());
			}
		}

		nbt.setTag("items", list);
		nbt.setInteger("count", count);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		String name;

		if (items.size() == 1)
		{
			name = items.get(0).getDisplayName();
		}
		else
		{
			String[] s = new String[items.size()];

			for (int i = 0; i < s.length; i++)
			{
				s[i] = items.get(i).getDisplayName();
			}

			name = StringJoiner.with(", ").joinStrings(s);
		}

		if (count > 1)
		{
			name = name + "x " + count;
		}

		return name;
	}

	@Override
	public boolean test(ItemStack stack)
	{
		if (stack.isEmpty())
		{
			return false;
		}
		else if (stack.getCount() != 1)
		{
			stack = ItemHandlerHelper.copyStackWithSize(stack, 1);
		}

		for (ItemStack stack1 : items)
		{
			if (ItemStack.areItemStacksEqual(stack, stack1))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public QuestTaskData createData(IProgressData data)
	{
		return new Data(this, data);
	}

	public static class Data extends QuestTaskData<ItemTask> implements IItemHandler
	{
		private Data(ItemTask t, IProgressData data)
		{
			super(t, data);
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		}

		@Nullable
		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) this : null;
		}

		@Override
		public ContainerTaskBase getContainer(EntityPlayer player)
		{
			return new ContainerItemTask(player, this);
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
			if (getProgress() < task.count && task.test(stack))
			{
				int add = Math.min(stack.getCount(), task.count - getProgress());

				if (add > 0 && setProgress(getProgress() + add, simulate))
				{
					return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - add);
				}
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
			return task.count;
		}
	}
}