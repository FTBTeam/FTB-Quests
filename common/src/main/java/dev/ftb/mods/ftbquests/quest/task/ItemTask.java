package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsTags;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.CustomToast;
import dev.ftb.mods.ftbquests.client.gui.quests.ValidItemsScreen;
import dev.ftb.mods.ftbquests.integration.item_filtering.ItemMatchingSystem;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.item.MissingItem;
import dev.ftb.mods.ftbquests.net.FTBQuestsNetHandler;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.FTBQuestsInventoryListener;
import dev.ftb.mods.ftbquests.util.NBTUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ItemTask extends Task implements Predicate<ItemStack> {
	@Deprecated(forRemoval = true)
	// this will disappear in 1.20.2+
	public static final TagKey<Item> CHECK_NBT_ITEM_FILTERS
			= TagKey.create(Registries.ITEM, new ResourceLocation("itemfilters", "check_nbt"));

	private ItemStack itemStack;
	private long count;
	private Tristate consumeItems;
	private Tristate onlyFromCrafting;
	private Tristate matchNBT;
	private boolean weakNBTmatch;
	private boolean taskScreenOnly;

	public ItemTask(long id, Quest quest) {
		super(id, quest);
		itemStack = ItemStack.EMPTY;
		count = 1;
		consumeItems = Tristate.DEFAULT;
		onlyFromCrafting = Tristate.DEFAULT;
		matchNBT = Tristate.DEFAULT;
		weakNBTmatch = false;
		taskScreenOnly = false;
	}

	@Override
	public TaskType getType() {
		return TaskTypes.ITEM;
	}

	@Override
	public long getMaxProgress() {
		return count;
	}

	public ItemTask setStackAndCount(ItemStack stack, int count) {
		itemStack = stack.copy();
		this.count = count;
		return this;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public void setConsumeItems(Tristate consumeItems) {
		this.consumeItems = consumeItems;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		NBTUtils.write(nbt, "item", itemStack);

		if (count > 1) {
			nbt.putLong("count", count);
		}

		consumeItems.write(nbt, "consume_items");
		onlyFromCrafting.write(nbt, "only_from_crafting");
		matchNBT.write(nbt, "match_nbt");
		if (weakNBTmatch) {
			nbt.putBoolean("weak_nbt_match", true);
		}
		if (taskScreenOnly) {
			nbt.putBoolean("task_screen_only", true);
		}
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		itemStack = NBTUtils.read(nbt, "item");
		count = Math.max(nbt.getLong("count"), 1L);
		consumeItems = Tristate.read(nbt, "consume_items");
		onlyFromCrafting = Tristate.read(nbt, "only_from_crafting");
		matchNBT = Tristate.read(nbt, "match_nbt");
		weakNBTmatch = nbt.getBoolean("weak_nbt_match");
		taskScreenOnly = nbt.getBoolean("task_screen_only");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		int flags = 0;
		flags = Bits.setFlag(flags, 0x01, count > 1L);
		flags = Bits.setFlag(flags, 0x02, consumeItems != Tristate.DEFAULT);
		flags = Bits.setFlag(flags, 0x04, consumeItems == Tristate.TRUE);
		flags = Bits.setFlag(flags, 0x08, onlyFromCrafting != Tristate.DEFAULT);
		flags = Bits.setFlag(flags, 0x10, onlyFromCrafting == Tristate.TRUE);
		flags = Bits.setFlag(flags, 0x20, matchNBT != Tristate.DEFAULT);
		flags = Bits.setFlag(flags, 0x40, matchNBT == Tristate.TRUE);
		flags = Bits.setFlag(flags, 0x80, weakNBTmatch);
		flags = Bits.setFlag(flags, 0x100, taskScreenOnly);
		buffer.writeVarInt(flags);

		FTBQuestsNetHandler.writeItemType(buffer, itemStack);

		if (count > 1L) {
			buffer.writeVarLong(count);
		}
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		int flags = buffer.readVarInt();

		itemStack = FTBQuestsNetHandler.readItemType(buffer);
		count = Bits.getFlag(flags, 0x01) ? buffer.readVarLong() : 1L;
		consumeItems = Bits.getFlag(flags, 0x02) ? Bits.getFlag(flags, 0x04) ? Tristate.TRUE : Tristate.FALSE : Tristate.DEFAULT;
		onlyFromCrafting = Bits.getFlag(flags, 0x08) ? Bits.getFlag(flags, 0x10) ? Tristate.TRUE : Tristate.FALSE : Tristate.DEFAULT;
		matchNBT = Bits.getFlag(flags, 0x20) ? Bits.getFlag(flags, 0x40) ? Tristate.TRUE : Tristate.FALSE : Tristate.DEFAULT;
		weakNBTmatch = Bits.getFlag(flags, 0x80);
		taskScreenOnly = Bits.getFlag(flags, 0x100);
	}

	public List<ItemStack> getValidDisplayItems() {
		return ItemMatchingSystem.INSTANCE.getAllMatchingStacks(itemStack);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		if (count > 1) {
			return Component.literal(count + "x ").append(itemStack.getHoverName());
		}

		return Component.literal("").append(itemStack.getHoverName());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		List<Icon> icons = new ArrayList<>();

		for (ItemStack stack : getValidDisplayItems()) {
			ItemStack copy = stack.copy();
			copy.setCount(1);
			Icon icon = ItemIcon.getItemIcon(copy);

			if (!icon.isEmpty()) {
				icons.add(icon);
			}
		}

		if (icons.isEmpty()) {
			return ItemIcon.getItemIcon(FTBQuestsItems.MISSING_ITEM.get());
		}

		return IconAnimation.fromList(icons, false);
	}

	@Override
	public boolean test(ItemStack stack) {
		if (itemStack.isEmpty()) {
			return true;
		}

		return ItemMatchingSystem.INSTANCE.doesItemMatch(itemStack, stack, shouldMatchNBT(), weakNBTmatch);
	}

	private boolean shouldMatchNBT() {
		return switch (matchNBT) {
			case TRUE -> true;
			case FALSE -> false;
			case DEFAULT -> hasNBTCheckTag();
		};
	}

	private boolean hasNBTCheckTag() {
		Holder.Reference<Item> itemReference = itemStack.getItem().builtInRegistryHolder();
		return itemReference.is(FTBQuestsTags.Items.CHECK_NBT)
				|| itemReference.is(CHECK_NBT_ITEM_FILTERS);  // TODO ditch in 1.20.2+
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addItemStack("item", itemStack, v -> itemStack = v, ItemStack.EMPTY, true, false).setNameKey("ftbquests.task.ftbquests.item");
		config.addLong("count", count, v -> count = v, 1, 1, Long.MAX_VALUE);
		config.addEnum("consume_items", consumeItems, v -> consumeItems = v, Tristate.NAME_MAP);
		config.addEnum("only_from_crafting", onlyFromCrafting, v -> onlyFromCrafting = v, Tristate.NAME_MAP);
		config.addEnum("match_nbt", matchNBT, v -> matchNBT = v, Tristate.NAME_MAP);
		config.addBool("weak_nbt_match", weakNBTmatch, v -> weakNBTmatch = v, false);
		config.addBool("task_screen_only", taskScreenOnly, v -> taskScreenOnly = v, false);
	}

	@Override
	public boolean consumesResources() {
		return consumeItems.get(getQuest().getChapter().consumeItems());
	}

	@Override
	public boolean canInsertItem() {
		return consumesResources();
	}

	@Override
	public boolean submitItemsOnInventoryChange() {
		return !consumesResources();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
		button.playClickSound();

		List<ItemStack> validItems = getValidDisplayItems();

		if (!consumesResources() && validItems.size() == 1 && FTBQuests.getRecipeModHelper().isRecipeModAvailable()) {
			FTBQuests.getRecipeModHelper().showRecipes(validItems.get(0));
		} else if (validItems.isEmpty()) {
			Minecraft.getInstance().getToasts().addToast(new CustomToast(Component.literal("No valid items!"), ItemIcon.getItemIcon(FTBQuestsItems.MISSING_ITEM.get()), Component.literal("Report this bug to modpack author!")));
		} else {
			new ValidItemsScreen(this, validItems, canClick).openGui();
		}
	}

	@Override
	public void addMouseOverHeader(TooltipList list, TeamData teamData, boolean advanced) {
		if (!rawTitle.isEmpty()) {
			// task has had a custom title set, use that in preference to the item's tooltip
			list.add(getTitle());
		} else {
			// use item's tooltip, but include a count with the item name (e.g. "3 x Stick") if appropriate
			ItemStack stack = getIcon() instanceof ItemIcon i ? i.getStack() : itemStack;
			List<Component> lines = stack.getTooltipLines(FTBQuestsClient.getClientPlayer(), advanced ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
			if (!lines.isEmpty()) {
				lines.set(0, getTitle());
			} else {
				lines.add(getTitle());
			}
			lines.forEach(list::add);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void addMouseOverText(TooltipList list, TeamData teamData) {
		if (taskScreenOnly) {
			list.blankLine();
			list.add(Component.translatable("ftbquests.task.task_screen_only").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
		} else if (consumesResources() && !teamData.isCompleted(this)) {
			list.blankLine();
			list.add(Component.translatable("ftbquests.task.click_to_submit").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
		} else if (getValidDisplayItems().size() > 1) {
			list.blankLine();
			list.add(Component.translatable("ftbquests.task.ftbquests.item.view_items").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
		} else if (FTBQuests.getRecipeModHelper().isRecipeModAvailable()) {
			list.blankLine();
			list.add(Component.translatable("ftbquests.task.ftbquests.item.click_recipe").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
		}
	}

	public ItemStack insert(TeamData teamData, ItemStack stack, boolean simulate) {
		if (!teamData.isCompleted(this) && consumesResources() && test(stack)) {
			long add = Math.min(stack.getCount(), count - teamData.getProgress(this));

			if (add > 0L) {
				if (!simulate && teamData.getFile().isServerSide()) {
					teamData.addProgress(this, add);
				}

				ItemStack copy = stack.copy();
				copy.setCount((int) (stack.getCount() - add));
				return copy;
			}
		}

		return stack;
	}

	private long countMatchingItems(Collection<ItemStack> toCheck) {
		long total = 0;
		for (ItemStack stack : toCheck) {
			if (test(stack)) {
				total += stack.getCount();
				if (total >= count) break;
			}
		}
		return Math.min(total, count);
	}

	@Override
	public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem) {
		if (taskScreenOnly || !checkTaskSequence(teamData) || teamData.isCompleted(this) || itemStack.getItem() instanceof MissingItem || craftedItem.getItem() instanceof MissingItem) {
			return;
		}

		if (!consumesResources()) {
			if (onlyFromCrafting.get(false)) {
				if (!craftedItem.isEmpty() && test(craftedItem)) {
					teamData.addProgress(this, craftedItem.getCount());
				}
			} else {
				var toCheck = ItemMatchingSystem.INSTANCE.isItemFilter(itemStack) ?
						FTBQuestsInventoryListener.getAllNonEmptyStacksForPlayer() :
						FTBQuestsInventoryListener.getStacksForPlayerOfType(itemStack.getItem());
				long matchCount = countMatchingItems(toCheck);
				if (matchCount > teamData.getProgress(this)) {
					teamData.setProgress(this, matchCount);
				}
			}
		} else if (craftedItem.isEmpty()) {
			boolean changed = false;

			for (int i = 0; i < player.getInventory().items.size(); i++) {
				ItemStack stack = player.getInventory().items.get(i);
				ItemStack stack1 = insert(teamData, stack, false);

				if (stack != stack1) {
					changed = true;
					player.getInventory().items.set(i, stack1.isEmpty() ? ItemStack.EMPTY : stack1);
				}
			}

			if (changed) {
				player.getInventory().setChanged();
				player.containerMenu.broadcastChanges();
			}
		}
	}

	public boolean isTaskScreenOnly() {
		return taskScreenOnly;
	}

	public boolean isOnlyFromCrafting() {
		return onlyFromCrafting.get(false);
	}
}
