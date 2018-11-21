package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.Bits;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;
import com.feed_the_beast.ftbquests.item.ItemMissing;
import com.feed_the_beast.ftbquests.net.MessageSubmitTask;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.resources.I18n;
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

	public ItemTask(Quest quest)
	{
		super(quest);
		items = new ArrayList<>();
		checkOnly = quest.chapter.file.defaultCheckOnly;
	}

	@Override
	public QuestTaskType getType()
	{
		return FTBQuestsTasks.ITEM;
	}

	@Override
	public long getMaxProgress()
	{
		return count;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);

		if (items.size() == 1)
		{
			nbt.setTag("item", ItemMissing.write(items.get(0), false));
		}
		else
		{
			NBTTagList list = new NBTTagList();

			for (ItemStack stack : items)
			{
				if (!stack.isEmpty())
				{
					list.appendTag(ItemMissing.write(stack, true));
				}
			}

			nbt.setTag("items", list);
		}

		if (count > 1)
		{
			nbt.setLong("count", count);
		}

		if (checkOnly != quest.chapter.file.defaultCheckOnly)
		{
			nbt.setBoolean("check_only", checkOnly);
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
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		NBTTagList list = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND);

		if (list.isEmpty())
		{
			ItemStack stack = ItemMissing.read(nbt.getTag("item"));

			if (!stack.isEmpty())
			{
				items.add(stack);
			}
		}
		else
		{
			for (int i = 0; i < list.tagCount(); i++)
			{
				ItemStack stack = ItemMissing.read(list.getCompoundTagAt(i));

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

		checkOnly = nbt.hasKey("check_only") ? nbt.getBoolean("check_only") : quest.chapter.file.defaultCheckOnly;
		ignoreDamage = nbt.getBoolean("ignore_damage");
		ignoreNBT = nbt.getBoolean("ignore_nbt");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeCollection(items, DataOut.ITEM_STACK);
		data.writeVarLong(count);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, checkOnly);
		flags = Bits.setFlag(flags, 2, ignoreDamage);
		flags = Bits.setFlag(flags, 4, ignoreNBT);
		data.writeVarInt(flags);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		data.readCollection(items, DataIn.ITEM_STACK);
		count = data.readVarLong();
		int flags = data.readVarInt();
		checkOnly = Bits.getFlag(flags, 1);
		ignoreDamage = Bits.getFlag(flags, 2);
		ignoreNBT = Bits.getFlag(flags, 4);
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
			return Icon.getIcon("minecraft:items/diamond");
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
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addList("items", items, new ConfigItemStack(ItemStack.EMPTY, true), v -> new ConfigItemStack(v, true), ConfigItemStack::getStack);
		config.addLong("count", () -> count, v -> count = v, 1, 1, Long.MAX_VALUE);
		config.addBool("check_only", () -> checkOnly, v -> checkOnly = v, false).setCanEdit(!quest.canRepeat);
		config.addBool("ignore_damage", () -> ignoreDamage, v -> ignoreDamage = v, false);
		config.addBool("ignore_nbt", () -> ignoreNBT, v -> ignoreNBT = v, false);
	}

	@Override
	public boolean canInsertItem()
	{
		if (quest.canRepeat)
		{
			return true;
		}

		return !checkOnly;
	}

	@Override
	public boolean submitItemsOnInventoryChange()
	{
		return !canInsertItem();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onButtonClicked()
	{
		new MessageSubmitTask(uid).sendToServer();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list, @Nullable QuestTaskData data)
	{
		list.add(TextFormatting.GRAY + (canInsertItem() ? I18n.format("ftbquests.task.ftbquests.item.consume_true") : I18n.format("ftbquests.task.ftbquests.item.consume_false")));
		list.add(TextFormatting.GRAY + I18n.format("ftbquests.task.click_to_submit"));
		list.add("");

		if (items.isEmpty())
		{
			return;
		}
		else if (items.size() > 1)
		{
			list.add(TextFormatting.GRAY + I18n.format("ftbquests.task.ftbquests.item.valid_items"));
		}

		GuiHelper.addStackTooltip(items.get(0), list, "");

		for (int i = 1; i < items.size(); i++)
		{
			list.add(" - - -");
			GuiHelper.addStackTooltip(items.get(i), list, "");
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
		public boolean submitTask(EntityPlayerMP player, boolean simulate)
		{
			if (!task.canInsertItem())
			{
				long count = 0;

				for (int i = 0; i < player.inventory.mainInventory.size(); i++)
				{
					ItemStack stack = player.inventory.mainInventory.get(i);

					if (task.test(stack))
					{
						count += stack.getCount();
					}
					else if (!stack.isEmpty())
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
					if (!simulate)
					{
						progress = Math.min(task.count, count);
						sync();
					}

					return true;
				}

				return false;
			}

			boolean changed = false;

			for (int i = 0; i < player.inventory.mainInventory.size(); i++)
			{
				ItemStack stack = player.inventory.mainInventory.get(i);
				ItemStack stack1 = insertItem(stack, false, simulate, player);

				if (!ItemStack.areItemStacksEqual(stack, stack1))
				{
					changed = true;

					if (!simulate)
					{
						player.inventory.mainInventory.set(i, stack1);
					}
				}
			}

			return changed;
		}
	}
}