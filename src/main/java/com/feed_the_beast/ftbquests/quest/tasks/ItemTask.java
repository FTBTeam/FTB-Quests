package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.config.ConfigLong;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class ItemTask extends QuestTask implements Predicate<ItemStack>
{
	public final List<ItemStack> items;
	public long count;
	public boolean checkOnly;
	public boolean ignoreDamage;
	public boolean ignoreNBT;

	public ItemTask(Quest quest, NBTTagCompound nbt)
	{
		super(quest);
		items = new ArrayList<>();

		NBTTagList list = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND);

		if (list.isEmpty())
		{
			ItemStack stack = readOrDummy(nbt.getCompoundTag("item"));

			if (!stack.isEmpty())
			{
				items.add(stack);
			}
		}
		else
		{
			for (int i = 0; i < list.tagCount(); i++)
			{
				ItemStack stack = readOrDummy(list.getCompoundTagAt(i));

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

		checkOnly = nbt.getBoolean("check_only");
		ignoreDamage = nbt.getBoolean("ignore_damage");
		ignoreNBT = nbt.getBoolean("ignore_nbt");
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

		if (checkOnly)
		{
			nbt.setBoolean("check_only", true);
		}

		if (ignoreDamage)
		{
			nbt.setBoolean("ignore_damage", true);
		}

		if (ignoreNBT)
		{
			nbt.setBoolean("ignore_nbt", true);
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

		if (icons.isEmpty())
		{
			return GuiIcons.ACCEPT;
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

		Item item = stack.getItem();
		int meta = ignoreDamage ? 0 : stack.getMetadata();
		NBTTagCompound nbt = item.getNBTShareTag(stack);

		for (ItemStack stack1 : items)
		{
			if (item == stack1.getItem())
			{
				if (ignoreDamage || meta == stack1.getMetadata())
				{
					if (ignoreNBT || Objects.equals(nbt, stack1.getItem().getNBTShareTag(stack1)))
					{
						return true;
					}
				}
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

		group.add("count", new ConfigLong.SimpleLong(1, Long.MAX_VALUE, () -> count, v -> count = v), new ConfigLong(1));
		group.add("check_only", new ConfigBoolean.SimpleBoolean(() -> checkOnly, v -> checkOnly = v), new ConfigBoolean(false));
		group.add("ignore_damage", new ConfigBoolean.SimpleBoolean(() -> ignoreDamage, v -> ignoreDamage = v), new ConfigBoolean(false));
		group.add("ignore_nbt", new ConfigBoolean.SimpleBoolean(() -> ignoreNBT, v -> ignoreNBT = v), new ConfigBoolean(false));
	}

	@Override
	public boolean canInsertItem()
	{
		return !checkOnly;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list, @Nullable QuestTaskData data)
	{
		super.addMouseOverText(list, data);

		list.add("");
		list.add(TextFormatting.GRAY + "Valid items:");

		boolean first = true;

		for (ItemStack stack : items)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				list.add("---");
			}

			GuiHelper.addStackTooltip(stack, list, "");
		}
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

		@Override
		public boolean submitItems(EntityPlayerMP player)
		{
			if (task.checkOnly)
			{
				long count = 0;

				for (int i = 0; i < player.inventory.mainInventory.size(); i++)
				{
					ItemStack stack = player.inventory.mainInventory.get(i);

					if (task.test(stack))
					{
						count += stack.getCount();
					}
					else
					{
						IItemHandler itemHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

						if (itemHandler != null)
						{
							for (int j = 0; j < itemHandler.getSlots(); j++)
							{
								ItemStack stack1 = itemHandler.getStackInSlot(j);

								if (task.test(stack1))
								{
									count += stack1.getCount();
								}
							}
						}
					}
				}

				if (count > progress)
				{
					progress = Math.min(task.count, count);
					sync();
				}

				return false;
			}

			boolean changed = false;

			for (int i = 0; i < player.inventory.mainInventory.size(); i++)
			{
				ItemStack stack = player.inventory.mainInventory.get(i);
				ItemStack stack1 = insertItem(stack, false, false, player);

				if (!ItemStack.areItemStacksEqual(stack, stack1))
				{
					changed = true;
					player.inventory.mainInventory.set(i, stack1);
				}
			}

			return changed;
		}
	}
}