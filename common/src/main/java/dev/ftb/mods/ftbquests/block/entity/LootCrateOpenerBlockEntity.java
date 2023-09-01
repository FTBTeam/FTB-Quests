package dev.ftb.mods.ftbquests.block.entity;

import dev.ftb.mods.ftbquests.item.LootCrateItem;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class LootCrateOpenerBlockEntity extends BlockEntity {
    private static final ItemEntry EMPTY_ENTRY = new ItemEntry(ItemStack.EMPTY);
    private static final int MAX_ITEM_TYPES = 64;

    private UUID owner = null;
    private final Map<ItemEntry, Integer> outputs = new LinkedHashMap<>();

    public LootCrateOpenerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(FTBQuestsBlockEntities.LOOT_CRATE_OPENER.get(), blockPos, blockState);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        outputs.clear();
        ListTag itemTag = compoundTag.getList("Items", Tag.TAG_COMPOUND);
        itemTag.forEach(el -> {
            if (el instanceof CompoundTag tag) {
                ItemStack stack = ItemStack.of(tag.getCompound("item"));
                int amount = tag.getInt("amount");
                outputs.put(new ItemEntry(stack), amount);
            }
        });

        owner = compoundTag.hasUUID("Owner") ? compoundTag.getUUID("Owner") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        ListTag itemTag = new ListTag();
        outputs.forEach((item, amount) -> {
            CompoundTag tag = new CompoundTag();
            tag.put("item", item.stack.save(new CompoundTag()));
            tag.putInt("amount", amount);
            itemTag.add(tag);
        });
        if (!itemTag.isEmpty()) compoundTag.put("Items", itemTag);

        if (owner != null) compoundTag.putUUID("Owner", owner);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public int getOutputCount() {
        return outputs.values().stream().mapToInt(v -> v).sum();
    }

    /**
     * Allow using an itemstack as a key (keyed by item and tag if present, not amount)
     *
     * @param stack the itemstack
     */
    private record ItemEntry(ItemStack stack) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemEntry itemEntry = (ItemEntry) o;
            return stack.getItem() == itemEntry.stack.getItem()
                    && (stack.getTag() == null || stack.getTag().equals(itemEntry.stack.getTag()));
        }

        @Override
        public int hashCode() {
            return stack.getTag() != null ?
                    Objects.hash(stack.getItem(), stack.getTag().hashCode()) :
                    Objects.hash(stack.getItem());
        }
    }

    // Note: platform-agnostic insertion/extraction methods. See Forge/Fabric implementations for public methods

    protected int _getSlots() {
        return 2;
    }

    protected ItemStack _getStackInSlot(int slot) {
        return slot == 0 ? ItemStack.EMPTY : outputs.keySet().stream().findFirst().orElse(EMPTY_ENTRY).stack;
    }

    protected ItemStack _insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot != 0 || level == null || level.getServer() == null || level.isClientSide || outputs.size() >= MAX_ITEM_TYPES) {
            return stack;
        }

        LootCrate crate = LootCrateItem.getCrate(stack);
        if (crate == null) {
            return stack;
        }

        ServerPlayer player = owner == null ? null : level.getServer().getPlayerList().getPlayer(owner);
        boolean update = false;

        int nAttempts = stack.getCount();
        for (WeightedReward wr : crate.getTable().generateWeightedRandomRewards(level.getRandom(), nAttempts, true)) {
            List<ItemStack> stacks = new ArrayList<>();
            if (wr.getReward().automatedClaimPre(this, stacks, level.random, owner, player)) {
                update = true;

                if (!simulate) {
                    for (ItemStack stack1 : stacks) {
                        ItemEntry entry = new ItemEntry(stack1);
                        int newAmount = outputs.getOrDefault(entry, 0) + stack1.getCount();
                        outputs.put(entry, newAmount);

                    }
                    wr.getReward().automatedClaimPost(this, owner, player);
                }
            }
        }

        if (update && !simulate) {
            setChanged();
        }

        return ItemStack.EMPTY;
    }

    protected boolean _isItemValid(int slot, ItemStack stack) {
        return slot == 0 && LootCrateItem.getCrate(stack) != null;
    }

    protected ItemStack _extractItem(int slot, int amount, boolean simulate) {
        if (level == null || slot == 0 || amount <= 0 || outputs.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemEntry entry = outputs.keySet().stream().findFirst().orElseThrow(); // we already verified it's not empty
        ItemStack stack1 = entry.stack().copy();
        int count = outputs.get(entry);

        int toExtract = Math.min(count, Math.min(amount, stack1.getMaxStackSize()));
        stack1.setCount(toExtract);

        if (!simulate && !level.isClientSide) {
            count -= toExtract;
            if (count <= 0) {
                outputs.remove(entry);
            } else {
                outputs.put(entry, count);
            }
            setChanged();
        }

        return stack1;
    }
}
