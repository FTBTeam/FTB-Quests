package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.icon.AnimatedIcon;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.CustomToast;
import dev.ftb.mods.ftbquests.client.gui.quests.ValidItemsScreen;
import dev.ftb.mods.ftbquests.integration.item_filtering.ItemMatchingSystem;
import dev.ftb.mods.ftbquests.integration.item_filtering.ItemMatchingSystem.ComponentMatchType;
import dev.ftb.mods.ftbquests.item.MissingItem;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.registry.ModItems;
import dev.ftb.mods.ftbquests.util.PlayerInventorySummary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ItemTask extends Task implements Predicate<ItemStack> {
	private ItemStack itemStack;
	private long count;
	private Tristate consumeItems;
	private Tristate onlyFromCrafting;
	private ComponentMatchType matchComponents;
	private boolean taskScreenOnly;

	public ItemTask(long id, Quest quest) {
		super(id, quest);
		itemStack = ItemStack.EMPTY;
		count = 1;
		consumeItems = Tristate.DEFAULT;
		onlyFromCrafting = Tristate.DEFAULT;
		matchComponents = ComponentMatchType.NONE;
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
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);

		nbt.put("item", saveItemSingleLine(itemStack.copyWithCount(1)));

		if (count > 1) {
			nbt.putLong("count", count);
		}

		consumeItems.write(nbt, "consume_items");
		onlyFromCrafting.write(nbt, "only_from_crafting");
		if (matchComponents != ComponentMatchType.NONE) {
			nbt.putString("match_components", ComponentMatchType.NAME_MAP.getName(matchComponents));
		}

		if (taskScreenOnly) {
			nbt.putBoolean("task_screen_only", true);
		}
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		itemStack = itemOrMissingFromNBT(nbt.get("item"), provider);
		count = Math.max(nbt.getLongOr("count", 1), 1L);
		consumeItems = Tristate.read(nbt, "consume_items");
		onlyFromCrafting = Tristate.read(nbt, "only_from_crafting");
		matchComponents = nbt.getString("match_components").map(ComponentMatchType.NAME_MAP::get).orElse(ComponentMatchType.NONE);
		taskScreenOnly = nbt.getBooleanOr("task_screen_only", false);
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		int flags = 0;
		flags = Bits.setFlag(flags, 0x01, count > 1L);
		flags = Bits.setFlag(flags, 0x02, consumeItems != Tristate.DEFAULT);
		flags = Bits.setFlag(flags, 0x04, consumeItems == Tristate.TRUE);
		flags = Bits.setFlag(flags, 0x08, onlyFromCrafting != Tristate.DEFAULT);
		flags = Bits.setFlag(flags, 0x10, onlyFromCrafting == Tristate.TRUE);
		flags = Bits.setFlag(flags, 0x20, matchComponents != ComponentMatchType.NONE);
		flags = Bits.setFlag(flags, 0x40, matchComponents == ComponentMatchType.STRICT);
		flags = Bits.setFlag(flags, 0x100, taskScreenOnly);
		buffer.writeVarInt(flags);

		ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, itemStack);

		if (count > 1L) {
			buffer.writeVarLong(count);
		}
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		int flags = buffer.readVarInt();

		itemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
		count = Bits.getFlag(flags, 0x01) ? buffer.readVarLong() : 1L;
		consumeItems = Bits.getFlag(flags, 0x02) ? Bits.getFlag(flags, 0x04) ? Tristate.TRUE : Tristate.FALSE : Tristate.DEFAULT;
		onlyFromCrafting = Bits.getFlag(flags, 0x08) ? Bits.getFlag(flags, 0x10) ? Tristate.TRUE : Tristate.FALSE : Tristate.DEFAULT;
		matchComponents = Bits.getFlag(flags, 0x20) ? Bits.getFlag(flags, 0x40) ? ComponentMatchType.STRICT : ComponentMatchType.FUZZY : ComponentMatchType.NONE;
		taskScreenOnly = Bits.getFlag(flags, 0x100);
	}

	public List<ItemStack> getValidDisplayItems() {
		return ItemMatchingSystem.INSTANCE.getAllMatchingStacks(itemStack, getQuestFile().holderLookup());
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
	public Icon<?> getAltIcon() {
		List<Icon<?>> icons = new ArrayList<>();

		for (ItemStack stack : getValidDisplayItems()) {
			ItemStack copy = stack.copy();
			copy.setCount(1);
			Icon<?> icon = ItemIcon.ofItemStack(copy);

			if (!icon.isEmpty()) {
				icons.add(icon);
			}
		}

		if (icons.isEmpty()) {
			return ItemIcon.ofItem(ModItems.MISSING_ITEM.get());
		}

		return AnimatedIcon.fromList(icons, false);
	}

	@Override
	public boolean test(ItemStack stack) {
		if (itemStack.isEmpty()) {
			return true;
		}

		return ItemMatchingSystem.INSTANCE.doesItemMatch(itemStack, stack, matchComponents, getQuestFile().holderLookup());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addItemStack("item", itemStack, v -> itemStack = v, ItemStack.EMPTY, true, false).setNameKey("ftbquests.task.ftbquests.item");
		config.addLong("count", count, v -> count = v, 1, 1, Long.MAX_VALUE);
		config.addEnum("consume_items", consumeItems, v -> consumeItems = v, Tristate.NAME_MAP);
		config.addEnum("only_from_crafting", onlyFromCrafting, v -> onlyFromCrafting = v, Tristate.NAME_MAP);
		config.addEnum("match_components", matchComponents, v -> matchComponents = v, ComponentMatchType.NAME_MAP);
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
			FTBQuests.getRecipeModHelper().showRecipes(validItems.getFirst());
		} else if (validItems.isEmpty()) {
			Minecraft.getInstance().getToastManager().addToast(new CustomToast(Component.literal("No valid items!"), ItemIcon.ofItem(ModItems.MISSING_ITEM.get()), Component.literal("Report this bug to modpack author!")));
		} else {
			new ValidItemsScreen(this, validItems, canClick).openGui();
		}
	}

	@Override
	public void addMouseOverHeader(TooltipList list, TeamData teamData, boolean advanced) {
		if (!getRawTitle().isEmpty()) {
			// task has had a custom title set, use that in preference to the item's tooltip
			list.add(getTitle());
		} else {
			// use item's tooltip, but include a count with the item name (e.g. "3 x Stick") if appropriate
			ItemStack stack = getIcon() instanceof ItemIcon i ? i.getStack() : itemStack;
			List<Component> lines = stack.getTooltipLines(Item.TooltipContext.of(
					FTBQuestsClient.getClientLevel()),
					FTBQuestsClient.getClientPlayer(),
					advanced ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL
			);
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
				long matchCount = countMatchingItems(PlayerInventorySummary.getRelevantItems(itemStack));
				if (matchCount > teamData.getProgress(this)) {
					teamData.setProgress(this, matchCount);
				}
			}
		} else if (craftedItem.isEmpty()) {
			boolean changed = false;

			for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
				ItemStack stack = player.getInventory().getItem(i);
				ItemStack stack1 = insert(teamData, stack, false);

				if (stack != stack1) {
					changed = true;
					player.getInventory().setItem(i, stack1.isEmpty() ? ItemStack.EMPTY : stack1);
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
