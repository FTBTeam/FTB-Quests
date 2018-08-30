package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.config.ConfigLong;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
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
	private long count;

	public ItemTask(Quest quest, NBTTagCompound nbt)
	{
		super(quest);
		items = new ArrayList<>();

		NBTTagList list = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND);

		if (list.isEmpty())
		{
			ItemStack stack = ItemStackSerializer.read(nbt.getCompoundTag("item"));

			if (!stack.isEmpty())
			{
				items.add(stack);
			}
		}
		else
		{
			for (int i = 0; i < list.tagCount(); i++)
			{
				ItemStack stack = ItemStackSerializer.read(list.getCompoundTagAt(i));

				if (!stack.isEmpty())
				{
					items.add(stack);
				}
			}
		}

		count = nbt.getLong("count");

		if (count < 1)
		{
			count = 1;
		}
	}

	@Override
	public long getMaxProgress()
	{
		return count;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		if (items.size() == 1)
		{
			nbt.setTag("item", ItemStackSerializer.write(items.get(0)));
		}
		else
		{
			NBTTagList list = new NBTTagList();

			for (ItemStack stack : items)
			{
				if (!stack.isEmpty())
				{
					list.appendTag(ItemStackSerializer.write(stack));
				}
			}

			nbt.setTag("items", list);
		}

		if (count > 1)
		{
			nbt.setLong("count", count);
		}
	}

	@Override
	public Icon getAltIcon()
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

		return IconAnimation.fromList(icons, false);
	}

	@Override
	public ITextComponent getAltDisplayName()
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

			name = "[" + StringJoiner.with(", ").joinStrings(s) + "]";
		}

		if (count > 1)
		{
			name = count + "x " + name;
		}

		return new TextComponentString(name);
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
			if (ItemStack.areItemStacksEqualUsingNBTShareTag(stack, stack1))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("items", new ConfigList<ConfigItemStack>(new ConfigItemStack(ItemStack.EMPTY, true))
		{
			@Override
			public void readFromList()
			{
				items.clear();

				for (ConfigItemStack value : list)
				{
					items.add(value.getStack());
				}
			}

			@Override
			public void writeToList()
			{
				list.clear();

				for (ItemStack stack : items)
				{
					list.add(new ConfigItemStack(stack, true));
				}
			}
		}, new ConfigList<>(new ConfigItemStack(ItemStack.EMPTY, true)));

		group.add("count", new ConfigLong(1, 1, Long.MAX_VALUE)
		{
			@Override
			public long getLong()
			{
				return count;
			}

			@Override
			public void setLong(long v)
			{
				count = v;
			}
		}, new ConfigLong(1));
	}

	@Override
	public boolean canInsertItem()
	{
		return true;
	}

	@Override
	public QuestTaskData createData(ITeamData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<ItemTask>
	{
		private Data(ItemTask t, ITeamData data)
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
		public ItemStack insertItem(ItemStack stack, boolean singleItem, boolean simulate, @Nullable EntityPlayer player)
		{
			if (task.test(stack))
			{
				long add = Math.min(stack.getCount(), task.count - progress);

				if (add > 0L)
				{
					if (singleItem)
					{
						add = 1L;
					}

					if (!simulate)
					{
						progress += add;
						sync();
					}

					return ItemHandlerHelper.copyStackWithSize(stack, (int) (stack.getCount() - add));
				}
			}

			return stack;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return (int) Math.min(64L, task.count - progress);
		}
	}
}