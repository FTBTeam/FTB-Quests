package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.gui.quests.GuiValidItems;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.util.NBTUtils;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.Tristate;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconAnimation;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import dev.latvian.mods.itemfilters.api.ItemFiltersAPI;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class ItemTask extends Task implements Predicate<ItemStack>
{
	public ItemStack item;
	public long count;
	public Tristate consumeItems;
	public Tristate onlyFromCrafting;

	public ItemTask(Quest quest)
	{
		super(quest);
		item = ItemStack.EMPTY;
		count = 1;
		consumeItems = Tristate.DEFAULT;
		onlyFromCrafting = Tristate.DEFAULT;
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
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		NBTUtils.write(nbt, "item", item);

		if (count > 1)
		{
			nbt.putLong("count", count);
		}

		consumeItems.write(nbt, "consume_items");
		onlyFromCrafting.write(nbt, "only_from_crafting");
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		item = NBTUtils.read(nbt, "item");
		count = nbt.getLong("count");

		if (count < 1)
		{
			count = 1;
		}

		consumeItems = Tristate.read(nbt, "consume_items");
		onlyFromCrafting = Tristate.read(nbt, "only_from_crafting");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		FTBQuestsNetHandler.writeItemType(buffer, item);
		buffer.writeVarLong(count);
		Tristate.NAME_MAP.write(buffer, consumeItems);
		Tristate.NAME_MAP.write(buffer, onlyFromCrafting);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		item = FTBQuestsNetHandler.readItemType(buffer);
		count = buffer.readVarLong();
		consumeItems = Tristate.NAME_MAP.read(buffer);
		onlyFromCrafting = Tristate.NAME_MAP.read(buffer);
	}

	public List<ItemStack> getValidItems()
	{
		List<ItemStack> list = new ArrayList<>();
		ItemFiltersAPI.getValidItems(item, list);
		return list;
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
	public IFormattableTextComponent getAltTitle()
	{
		if (count > 1)
		{
			return new StringTextComponent(count + "x ").append(item.getDisplayName());
		}

		return new StringTextComponent("").append(item.getDisplayName());
	}

	@Override
	public boolean test(ItemStack stack)
	{
		return ItemFiltersAPI.filter(item, stack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addItemStack("item", item, v -> item = v, ItemStack.EMPTY, true, false).setNameKey("ftbquests.task.ftbquests.item");
		config.addLong("count", count, v -> count = v, 1, 1, Long.MAX_VALUE);
		config.addEnum("consume_items", consumeItems, v -> consumeItems = v, Tristate.NAME_MAP);
		config.addEnum("only_from_crafting", onlyFromCrafting, v -> onlyFromCrafting = v, Tristate.NAME_MAP);
	}

	@Override
	public boolean consumesResources()
	{
		return consumeItems.get(quest.chapter.file.defaultTeamConsumeItems);
	}

	@Override
	public boolean canInsertItem()
	{
		return consumesResources();
	}

	@Override
	public boolean submitItemsOnInventoryChange()
	{
		return !consumesResources();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void onButtonClicked(Button button, boolean canClick)
	{
		button.playClickSound();

		List<ItemStack> validItems = getValidItems();

		if (!consumesResources() && validItems.size() == 1 && ModList.get().isLoaded("jei"))
		{
			showJEIRecipe(validItems.get(0));
		}
		else
		{
			new GuiValidItems(this, validItems, canClick).openGui();
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void showJEIRecipe(ItemStack stack)
	{
		FTBQuestsJEIHelper.showRecipes(stack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addMouseOverText(TooltipList list, @Nullable TaskData data)
	{
		if (consumesResources())
		{
			list.blankLine();
			list.add(new TranslationTextComponent("ftbquests.task.click_to_submit").mergeStyle(TextFormatting.YELLOW, TextFormatting.UNDERLINE));
		}
		else if (getValidItems().size() > 1)
		{
			list.blankLine();
			list.add(new TranslationTextComponent("ftbquests.task.ftbquests.item.view_items").mergeStyle(TextFormatting.YELLOW, TextFormatting.UNDERLINE));
		}
		else if (ModList.get().isLoaded("jei"))
		{
			list.blankLine();
			list.add(new TranslationTextComponent("ftbquests.task.ftbquests.item.click_recipe").mergeStyle(TextFormatting.YELLOW, TextFormatting.UNDERLINE));
		}
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new Data(this, data);
	}

	public static class Data extends TaskData<ItemTask>
	{
		private Data(ItemTask t, PlayerData data)
		{
			super(t, data);
		}

		public ItemStack insert(ItemStack stack, boolean simulate)
		{
			if (!isComplete() && task.test(stack))
			{
				long add = Math.min(stack.getCount(), task.count - progress);

				if (add > 0L)
				{
					if (!simulate && !data.file.getSide().isClient())
					{
						addProgress(add);
					}

					return ItemHandlerHelper.copyStackWithSize(stack, (int) (stack.getCount() - add));
				}
			}

			return stack;
		}

		@Override
		public void submitTask(ServerPlayerEntity player, ItemStack item)
		{
			if (isComplete())
			{
				return;
			}

			if (!task.consumesResources())
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
				ItemStack stack1 = insert(stack, false);

				if (stack != stack1)
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