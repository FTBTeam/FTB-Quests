package dev.ftb.mods.ftbquests.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbquests.item.LootCrateItem;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;

public class LootCrateOpenerBlockEntity extends BlockEntity {
    private static final ItemEntry EMPTY_ENTRY = new ItemEntry(ItemStack.EMPTY);
    private static final int MAX_ITEM_TYPES = 64;

    private UUID owner = null;
    private final Map<ItemEntry, Integer> outputs = new LinkedHashMap<>();

    public LootCrateOpenerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntityTypes.LOOT_CRATE_OPENER.get(), blockPos, blockState);
    }

    private record StackWithAmount(ItemStack stack, int amount) {
        public static final Codec<StackWithAmount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("stack").forGetter(StackWithAmount::stack),
                Codec.INT.fieldOf("amount").forGetter(StackWithAmount::amount)
        ).apply(instance, StackWithAmount::new));
    }

    @Override
    public void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);

        outputs.clear();
        valueInput.read("Items", StackWithAmount.CODEC).ifPresent(stack -> {
            outputs.put(new ItemEntry(stack.stack()), stack.amount());
        });

        owner = valueInput.read("Owner", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);

        List<StackWithAmount> stacks = new ArrayList<>();
        outputs.forEach((item, amount) -> {
            var entry = new StackWithAmount(item.stack, amount);
            stacks.add(entry);
        });

        if (!stacks.isEmpty()) {
            valueOutput.store("Items", StackWithAmount.CODEC.listOf(), stacks);
        }

        if (owner != null) valueOutput.store("Owner", UUIDUtil.CODEC, owner);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        super.applyImplicitComponents(dataComponentGetter);

        outputs.clear();
        dataComponentGetter.getOrDefault(ModDataComponents.LOOT_CRATE_ITEMS.get(), ItemContainerContents.EMPTY)
                .stream().forEach(stack -> outputs.put(new ItemEntry(stack), stack.getCount()));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);

        builder.set(ModDataComponents.LOOT_CRATE_ITEMS.get(),
                ItemContainerContents.fromItems(outputs.keySet().stream().map(ItemEntry::stack).toList()));
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
     * Allow using an itemstack as a key (keyed by item and any components, but not the count)
     *
     * @param stack the itemstack
     */
    private record ItemEntry(ItemStack stack) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return ItemStack.isSameItemSameComponents(stack, ((ItemEntry) o).stack);
        }

        @Override
        public int hashCode() {
            return ItemStack.hashItemAndComponents(stack);
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
        if (slot != 0 || level == null || level.getServer() == null || level.isClientSide() || outputs.size() >= MAX_ITEM_TYPES) {
            return stack;
        }

        LootCrate crate = LootCrateItem.getCrate(stack, false);
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
        return slot == 0 && LootCrateItem.getCrate(stack, level.isClientSide()) != null;
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

        if (!simulate && !level.isClientSide()) {
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
