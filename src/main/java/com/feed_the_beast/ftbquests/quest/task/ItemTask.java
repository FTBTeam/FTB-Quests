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
import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class ItemTask extends QuestTask implements Predicate<ItemStack>
{
	public enum NBTMatchingMode implements IWithID
	{
		MATCH("match"),
		IGNORE("ignore"),
		CONTAIN("contain");

		public static final NameMap<NBTMatchingMode> NAME_MAP = NameMap.create(MATCH, values());

		private final String id;

		NBTMatchingMode(String i)
		{
			id = i;
		}

		@Override
		public String getID()
		{
			return id;
		}
	}

	public final List<ItemStack> items;
	public long count;
	public boolean consumeItems;
	public boolean ignoreDamage;
	public NBTMatchingMode nbtMode;

	public ItemTask(Quest quest)
	{
		super(quest);
		items = new ArrayList<>();
		consumeItems = quest.chapter.file.defaultConsumeItems;
		nbtMode = NBTMatchingMode.MATCH;
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

		if (consumeItems != quest.chapter.file.defaultConsumeItems)
		{
			nbt.setBoolean("consume_items", consumeItems);
		}

		if (ignoreDamage)
		{
			nbt.setBoolean("ignore_damage", true);
		}

		if (nbtMode != NBTMatchingMode.MATCH)
		{
			nbt.setByte("ignore_nbt", (byte) nbtMode.ordinal());
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		items.clear();
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

		consumeItems = nbt.hasKey("consume_items") ? nbt.getBoolean("consume_items") : nbt.hasKey("check_only") ? nbt.getBoolean("check_only") : quest.chapter.file.defaultConsumeItems;
		ignoreDamage = nbt.getBoolean("ignore_damage");
		nbtMode = NBTMatchingMode.NAME_MAP.get(nbt.getByte("ignore_nbt"));
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeCollection(items, DataOut.ITEM_STACK);
		data.writeVarLong(count);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, consumeItems);
		flags = Bits.setFlag(flags, 2, ignoreDamage);
		flags = Bits.setFlag(flags, 4, nbtMode != NBTMatchingMode.MATCH);
		flags = Bits.setFlag(flags, 8, nbtMode == NBTMatchingMode.CONTAIN);
		data.writeVarInt(flags);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		data.readCollection(items, DataIn.ITEM_STACK);
		count = data.readVarLong();
		int flags = data.readVarInt();
		consumeItems = Bits.getFlag(flags, 1);
		ignoreDamage = Bits.getFlag(flags, 2);
		nbtMode = Bits.getFlag(flags, 4) ? Bits.getFlag(flags, 8) ? NBTMatchingMode.CONTAIN : NBTMatchingMode.IGNORE : NBTMatchingMode.MATCH;
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
			return super.getIcon();
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
		NBTTagCompound nbt = nbtMode == NBTMatchingMode.CONTAIN ? stack.getTagCompound() : item.getNBTShareTag(stack);

		for (ItemStack stack1 : items)
		{
			if (item == stack1.getItem())
			{
				if (ignoreDamage || meta == stack1.getMetadata())
				{
					switch (nbtMode)
					{
						case MATCH:
							return Objects.equals(nbt, stack1.getItem().getNBTShareTag(stack1));
						case IGNORE:
							return true;
						case CONTAIN:
						{
							NBTTagCompound nbt1 = stack1.getTagCompound();

							if (nbt1 == null || nbt1.isEmpty())
							{
								return true;
							}
							else if (nbt == null || nbt.isEmpty())
							{
								return false;
							}

							for (String s : nbt1.getKeySet())
							{
								if (!Objects.equals(nbt.getTag(s), nbt1.getTag(s)))
								{
									return false;
								}
							}

							return true;
						}
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
		config.addBool("consume_items", () -> consumeItems, v -> consumeItems = v, quest.chapter.file.defaultConsumeItems).setCanEdit(!quest.canRepeat);
		config.addBool("ignore_damage", () -> ignoreDamage, v -> ignoreDamage = v, false);
		config.addEnum("nbt_mode", () -> nbtMode, v -> nbtMode = v, NBTMatchingMode.NAME_MAP);
	}

	@Override
	public boolean canInsertItem()
	{
		return quest.canRepeat || consumeItems;
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
		new MessageSubmitTask(id).sendToServer();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list, @Nullable QuestTaskData data)
	{
		list.add(TextFormatting.GRAY + (canInsertItem() ? I18n.format("ftbquests.task.ftbquests.item.consume_true") : I18n.format("ftbquests.task.ftbquests.item.consume_false")));
		list.add(TextFormatting.GRAY + (canInsertItem() ? I18n.format("ftbquests.task.click_to_submit") : I18n.format("ftbquests.task.auto_detected")));
		list.add("");

		if (items.isEmpty())
		{
			return;
		}
		else if (items.size() > 1)
		{
			list.add(TextFormatting.GRAY + I18n.format("ftbquests.task.ftbquests.item.valid_items"));
		}

		GuiHelper.addStackTooltip(items.get(0), list);

		for (int i = 1; i < items.size(); i++)
		{
			list.add(" - - -");
			GuiHelper.addStackTooltip(items.get(i), list);
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
		public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		}

		@Nullable
		@Override
		public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
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
		public boolean submitTask(EntityPlayerMP player, Collection<ItemStack> itemsToCheck, boolean simulate)
		{
			if (!task.canInsertItem())
			{
				long count = 0;

				if (itemsToCheck.isEmpty())
				{
					itemsToCheck = player.inventory.mainInventory;
				}

				for (ItemStack stack : itemsToCheck)
				{
					if (stack.isEmpty())
					{
						continue;
					}

					if (task.test(stack))
					{
						count += stack.getCount();
					}
					else
					{
						IItemHandler itemHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

						if (itemHandler != null)
						{
							for (int slot = 0; slot < itemHandler.getSlots(); slot++)
							{
								ItemStack stack1 = itemHandler.getStackInSlot(slot);

								if (task.test(stack1))
								{
									count += stack1.getCount();
								}
							}
						}
					}
				}

				count = Math.min(task.count, count);

				if (count > progress)
				{
					if (!simulate)
					{
						progress = count;
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