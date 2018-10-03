package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;
import com.feed_the_beast.ftbquests.item.ItemMissing;
import com.feed_the_beast.ftbquests.net.MessageSubmitItems;
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
	public void readData(NBTTagCompound nbt)
	{
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

		checkOnly = nbt.getBoolean("check_only");
		ignoreDamage = nbt.getBoolean("ignore_damage");
		ignoreNBT = nbt.getBoolean("ignore_nbt");
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
			return false;
		}

		return !checkOnly;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onButtonClicked()
	{
		new MessageSubmitItems(getID()).sendToServer();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list, @Nullable QuestTaskData data)
	{
		list.add(TextFormatting.GRAY + (canInsertItem() ? I18n.format("ftbquests.task.ftbquests.item.consume_true") : I18n.format("ftbquests.task.ftbquests.item.consume_false")));
		list.add(TextFormatting.GRAY + I18n.format("ftbquests.task.click_to_submit"));
		list.add("");

		if (items.size() == 1)
		{
			GuiHelper.addStackTooltip(items.get(0), list, "");
		}
		else
		{
			list.add(TextFormatting.GRAY + I18n.format("ftbquests.task.ftbquests.item.valid_items"));

			boolean first = true;

			for (ItemStack stack : items)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					list.add(" - - -");
				}

				GuiHelper.addStackTooltip(stack, list, "");
			}
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
		public boolean submitItems(EntityPlayerMP player, boolean simulate)
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