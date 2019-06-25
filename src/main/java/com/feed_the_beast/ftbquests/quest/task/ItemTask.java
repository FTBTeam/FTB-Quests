package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.integration.FTBLibJEIIntegration;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.EnumTristate;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.Bits;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.gui.tree.GuiValidItems;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.latmod.mods.itemfilters.api.ItemFiltersAPI;
import com.latmod.mods.itemfilters.filters.NBTMatchingMode;
import com.latmod.mods.itemfilters.item.ItemFiltersItems;
import com.latmod.mods.itemfilters.item.ItemMissing;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.CapabilityItemHandler;
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
	public final List<ItemStack> items;
	public long count;
	public EnumTristate consumeItems;
	public boolean ignoreDamage;
	public NBTMatchingMode nbtMode;

	public ItemTask(Quest quest)
	{
		super(quest);
		items = new ArrayList<>();
		consumeItems = EnumTristate.DEFAULT;
		ignoreDamage = false;
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

		consumeItems.write(nbt, "consume_items");

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

		consumeItems = EnumTristate.read(nbt, "consume_items");
		ignoreDamage = nbt.getBoolean("ignore_damage");
		nbtMode = NBTMatchingMode.VALUES[nbt.getByte("ignore_nbt")];
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeCollection(items, DataOut.ITEM_STACK);
		data.writeVarLong(count);
		int flags = 0;
		//flags = Bits.setFlag(flags, 1, consumeItems);
		flags = Bits.setFlag(flags, 2, ignoreDamage);
		flags = Bits.setFlag(flags, 4, nbtMode != NBTMatchingMode.MATCH);
		flags = Bits.setFlag(flags, 8, nbtMode == NBTMatchingMode.CONTAIN);
		data.writeVarInt(flags);
		EnumTristate.NAME_MAP.write(data, consumeItems);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		data.readCollection(items, DataIn.ITEM_STACK);
		count = data.readVarLong();
		int flags = data.readVarInt();
		//consumeItems = Bits.getFlag(flags, 1);
		ignoreDamage = Bits.getFlag(flags, 2);
		nbtMode = Bits.getFlag(flags, 4) ? Bits.getFlag(flags, 8) ? NBTMatchingMode.CONTAIN : NBTMatchingMode.IGNORE : NBTMatchingMode.MATCH;
		consumeItems = EnumTristate.NAME_MAP.read(data);
	}

	public List<ItemStack> getValidItems()
	{
		if (items.size() == 1 && ItemFiltersAPI.isFilter(items.get(0)))
		{
			List<ItemStack> validItems = new ArrayList<>();
			ItemFiltersAPI.getValidItems(items.get(0), validItems);
			return validItems;
		}

		return items;
	}

	@Override
	public Icon getAltIcon()
	{
		List<Icon> icons = new ArrayList<>();

		for (ItemStack stack : getValidItems())
		{
			Icon icon = ItemIcon.getItemIcon(ItemHandlerHelper.copyStackWithSize(stack, 1));

			if (!icon.isEmpty())
			{
				icons.add(icon);
			}
		}

		if (icons.isEmpty())
		{
			return super.getAltIcon();
		}

		return IconAnimation.fromList(icons, false);
	}

	@Override
	public String getAltTitle()
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

		return name;
	}

	@Override
	public boolean test(ItemStack stack)
	{
		if (stack.isEmpty() || stack.getItem() == ItemFiltersItems.MISSING)
		{
			return false;
		}
		else if (items.size() == 1 && ItemFiltersAPI.isFilter(items.get(0)))
		{
			return ItemFiltersAPI.filter(items.get(0), stack);
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
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.addList("items", items, new ConfigItemStack(ItemStack.EMPTY, true), v -> new ConfigItemStack(v, true), ConfigItemStack::getStack);
		config.addLong("count", () -> count, v -> count = v, 1, 1, Long.MAX_VALUE);
		config.addEnum("consume_items", () -> consumeItems, v -> consumeItems = v, EnumTristate.NAME_MAP).setCanEdit(!quest.canRepeat);
		config.addBool("ignore_damage", () -> ignoreDamage, v -> ignoreDamage = v, false);
		config.addEnum("nbt_mode", () -> nbtMode, v -> nbtMode = v, NameMap.create(NBTMatchingMode.MATCH, NBTMatchingMode.VALUES));
	}

	@Override
	public boolean canInsertItem()
	{
		return quest.canRepeat || consumeItems.get(quest.chapter.file.defaultTeamConsumeItems);
	}

	@Override
	public boolean submitItemsOnInventoryChange()
	{
		return !canInsertItem();
	}

	@Override
	public void onButtonClicked(boolean canClick)
	{
		List<ItemStack> validItems = getValidItems();

		if (!consumesResources() && validItems.size() == 1 && Loader.isModLoaded("jei"))
		{
			showJEIRecipe(validItems.get(0));
		}
		else
		{
			new GuiValidItems(this, validItems, canClick).openGui();
		}
	}

	private void showJEIRecipe(ItemStack stack)
	{
		FTBLibJEIIntegration.showRecipe(stack);
	}

	@Override
	public void addMouseOverText(List<String> list, @Nullable QuestTaskData data)
	{
		if (consumesResources())
		{
			list.add("");
			list.add(TextFormatting.YELLOW.toString() + TextFormatting.UNDERLINE + I18n.format("ftbquests.task.click_to_submit"));
		}
		else if (getValidItems().size() > 1)
		{
			list.add("");
			list.add(TextFormatting.YELLOW.toString() + TextFormatting.UNDERLINE + I18n.format("ftbquests.task.ftbquests.item.view_items"));
		}
		else if (Loader.isModLoaded("jei"))
		{
			list.add("");
			list.add(TextFormatting.YELLOW.toString() + TextFormatting.UNDERLINE + I18n.format("ftbquests.task.ftbquests.item.click_recipe"));
		}
	}

	@Override
	public QuestTaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<ItemTask>
	{
		private Data(ItemTask t, QuestData data)
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
					if (!stack.isEmpty() && task.test(stack))
					{
						count += stack.getCount();
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