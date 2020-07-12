package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.integration.FTBLibJEIIntegration;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.EnumTristate;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.Bits;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.gui.tree.GuiValidItems;
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.net.MessageSubmitTask;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.latmod.mods.itemfilters.api.ItemFiltersAPI;
import com.latmod.mods.itemfilters.filters.NBTMatchingMode;
import com.latmod.mods.itemfilters.item.ItemFiltersItems;
import com.latmod.mods.itemfilters.item.ItemMissing;
import net.minecraft.client.gui.GuiScreen;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class ItemTask extends Task implements Predicate<ItemStack>
{
	public final List<ItemStack> items;
	public long count;
	public EnumTristate consumeItems;
	public boolean ignoreDamage;
	public NBTMatchingMode nbtMode;
	public EnumTristate onlyFromCrafting;

	public ItemTask(Quest quest)
	{
		super(quest);
		items = new ArrayList<>();
		count = 1L;
		consumeItems = EnumTristate.DEFAULT;
		ignoreDamage = false;
		nbtMode = NBTMatchingMode.MATCH;
		onlyFromCrafting = EnumTristate.DEFAULT;
	}

	@Override
	public TaskType getType()
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

		NBTTagList list = new NBTTagList();

		for (ItemStack stack : items)
		{
			if (!stack.isEmpty())
			{
				list.appendTag(ItemMissing.write(stack, true));
			}
		}

		nbt.setTag("items", list);

		if (count > 1L)
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

		onlyFromCrafting.write(nbt, "only_from_crafting");
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
			else
			{
				FTBQuests.LOGGER.warn("That's odd... Item is empty from tag " + nbt.getTag("item") + " in " + quest.chapter + "/" + quest + "/" + this);
			}
		}
		else
		{
			for (int i = 0; i < list.tagCount(); i++)
			{
				ItemStack stack = ItemMissing.read(list.get(i));

				if (!stack.isEmpty())
				{
					items.add(stack);
				}
				else
				{
					FTBQuests.LOGGER.warn("That's odd... Item is empty from tag " + list.get(i) + " in " + quest.chapter + "/" + quest + "/" + this);
				}
			}
		}

		if (items.isEmpty())
		{
			FTBQuests.LOGGER.warn("Item list is empty? That's not good... Task: " + quest.chapter + "/" + quest + "/" + this);
		}

		count = nbt.getLong("count");

		if (count < 1)
		{
			count = 1;
		}

		consumeItems = EnumTristate.read(nbt, "consume_items");
		ignoreDamage = nbt.getBoolean("ignore_damage");
		nbtMode = NBTMatchingMode.VALUES[nbt.getByte("ignore_nbt")];
		onlyFromCrafting = EnumTristate.read(nbt, "only_from_crafting");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, count > 1L);
		flags = Bits.setFlag(flags, 2, ignoreDamage);
		flags = Bits.setFlag(flags, 4, nbtMode != NBTMatchingMode.MATCH);
		flags = Bits.setFlag(flags, 8, nbtMode == NBTMatchingMode.CONTAIN);
		flags = Bits.setFlag(flags, 16, consumeItems != EnumTristate.DEFAULT);
		flags = Bits.setFlag(flags, 32, consumeItems == EnumTristate.TRUE);
		flags = Bits.setFlag(flags, 64, items.size() == 1);
		flags = Bits.setFlag(flags, 128, onlyFromCrafting != EnumTristate.DEFAULT);
		flags = Bits.setFlag(flags, 256, onlyFromCrafting == EnumTristate.TRUE);
		data.writeVarInt(flags);

		if (items.size() == 1)
		{
			DataOut.ITEM_STACK.write(data, items.get(0));
		}
		else
		{
			data.writeCollection(items, DataOut.ITEM_STACK);
		}

		if (count > 1L)
		{
			data.writeVarLong(count);
		}
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		int flags = data.readVarInt();
		ignoreDamage = Bits.getFlag(flags, 2);
		nbtMode = Bits.getFlag(flags, 4) ? Bits.getFlag(flags, 8) ? NBTMatchingMode.CONTAIN : NBTMatchingMode.IGNORE : NBTMatchingMode.MATCH;
		consumeItems = Bits.getFlag(flags, 16) ? Bits.getFlag(flags, 32) ? EnumTristate.TRUE : EnumTristate.FALSE : EnumTristate.DEFAULT;
		onlyFromCrafting = Bits.getFlag(flags, 128) ? Bits.getFlag(flags, 256) ? EnumTristate.TRUE : EnumTristate.FALSE : EnumTristate.DEFAULT;

		if (Bits.getFlag(flags, 64))
		{
			items.clear();
			items.add(DataIn.ITEM_STACK.read(data));
		}
		else
		{
			data.readCollection(items, DataIn.ITEM_STACK);
		}

		if (Bits.getFlag(flags, 1))
		{
			count = data.readVarLong();
		}
		else
		{
			count = 1L;
		}
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
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addList("items", items, new ConfigItemStack(ItemStack.EMPTY, true), v -> new ConfigItemStack(v, true), ConfigItemStack::getStack);
		config.addLong("count", () -> count, v -> count = v, 1, 1, Long.MAX_VALUE);
		config.addEnum("consume_items", () -> consumeItems, v -> consumeItems = v, EnumTristate.NAME_MAP).setCanEdit(!quest.canRepeat);
		config.addBool("ignore_damage", () -> ignoreDamage, v -> ignoreDamage = v, false);
		config.addEnum("nbt_mode", () -> nbtMode, v -> nbtMode = v, NameMap.create(NBTMatchingMode.MATCH, NBTMatchingMode.VALUES));
		config.addEnum("only_from_crafting", () -> onlyFromCrafting, v -> onlyFromCrafting = v, EnumTristate.NAME_MAP).setCanEdit(!quest.canRepeat);
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
	@SideOnly(Side.CLIENT)
	public void onButtonClicked(boolean canClick)
	{
		GuiHelper.playClickSound();

		List<ItemStack> validItems = getValidItems();

		if (!consumesResources() && validItems.size() == 1 && Loader.isModLoaded("jei"))
		{
			showJEIRecipe(validItems.get(0));
		}
		else if (GuiScreen.isShiftKeyDown())
		{
			int r = GuiScreen.isCtrlKeyDown() ? 16 : 1;

			for (int i = 0; i < r; i++)
			{
				new MessageSubmitTask(id).sendToServer();

				for (Reward reward : quest.rewards)
				{
					new MessageClaimReward(reward.id, false).sendToServer();
				}
			}
		}
		else
		{
			new GuiValidItems(this, validItems, canClick).openGui();
		}
	}

	@SideOnly(Side.CLIENT)
	private void showJEIRecipe(ItemStack stack)
	{
		FTBLibJEIIntegration.showRecipe(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list, @Nullable TaskData data)
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
	public TaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends TaskData<ItemTask>
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
			if (!isComplete() && task.test(stack))
			{
				long add = Math.min(stack.getCount(), task.count - progress);

				if (add > 0L)
				{
					if (singleItem)
					{
						add = 1L;
					}

					if (!simulate && !data.getFile().isClient())
					{
						addProgress(add);
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
		public void submitTask(EntityPlayerMP player, ItemStack item)
		{
			if (isComplete())
			{
				return;
			}

			if (!task.canInsertItem())
			{
				if (task.onlyFromCrafting.get(false))
				{
					if (item.isEmpty() || !task.test(item))
					{
						return;
					}

					long count = Math.min(task.count, item.getCount());

					if (count > progress)
					{
						setProgress(count);
						return;
					}
				}

				long count = 0;

				for (ItemStack stack : player.inventory.mainInventory)
				{
					if (!stack.isEmpty() && task.test(stack))
					{
						count += stack.getCount();
					}
				}

				count = Math.min(task.count, count);

				if (count > progress)
				{
					setProgress(count);
					return;
				}

				return;
			}

			if (!item.isEmpty())
			{
				return;
			}

			boolean changed = false;

			for (int i = 0; i < player.inventory.mainInventory.size(); i++)
			{
				ItemStack stack = player.inventory.mainInventory.get(i);
				ItemStack stack1 = insertItem(stack, false, false, player);

				if (!ItemStack.areItemStacksEqual(stack, stack1))
				{
					changed = true;
					player.inventory.mainInventory.set(i, stack1.isEmpty() ? ItemStack.EMPTY : stack1);
				}
			}

			if (changed)
			{
				player.inventory.markDirty();
				player.openContainer.detectAndSendChanges();
			}
		}
	}
}