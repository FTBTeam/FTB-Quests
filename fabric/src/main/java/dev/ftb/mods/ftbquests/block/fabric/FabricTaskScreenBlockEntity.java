package dev.ftb.mods.ftbquests.block.fabric;

import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.EnergyTask;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.latvian.mods.itemfilters.api.ItemFiltersAPI;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;

@SuppressWarnings("UnstableApiUsage")
public class FabricTaskScreenBlockEntity extends TaskScreenBlockEntity {
    private final Storage<ItemVariant> itemStorage = new ItemStorageHandler();
    private final Storage<FluidVariant> fluidStorage = new FluidStorageHandler();
    private final EnergyStorage energyStorage = new RebornEnergyStorageHandler();

    public FabricTaskScreenBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    public Storage<ItemVariant> getItemStorage() {
        return itemStorage;
    }

    public Storage<FluidVariant> getFluidStorage() {
        return fluidStorage;
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private class ItemStorageHandler extends SingleVariantStorage<ItemVariant> {
        @Override
        protected ItemVariant getBlankVariant() {
            return ItemVariant.blank();
        }

        @Override
        protected long getCapacity(ItemVariant variant) {
            return getTask() instanceof ItemTask t ? t.getMaxProgress() : 0L;
        }

        @Override
        public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
            TeamData data = getCachedTeamData();
            ItemStack stack = insertedVariant.toStack();
            if (getTask() instanceof ItemTask task && data.canStartTasks(task.getQuest())) {
                // task.insert() handles testing the item is valid and the task isn't already completed
                ItemStack res = task.insert(data, stack, true);
                int nAdded = stack.getCount() - res.getCount();
                if (nAdded > 0) {
                    transaction.addCloseCallback((transaction1, result) -> {
                        if (result.wasCommitted()) data.addProgress(task, nAdded);
                    });
                }
                return nAdded;
            }
            return 0L;
        }

        @Override
        public long extract(ItemVariant extractedVariant, long maxAmount, TransactionContext transaction) {
            if (!isInputOnly() && getTask() instanceof ItemTask task && !ItemFiltersAPI.isFilter(task.getItemStack())) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(task.getQuest()) && !data.isCompleted(task)) {
                    int itemsRemoved = (int) Math.min(data.getProgress(task), maxAmount);
                    updateSnapshots(transaction);
                    transaction.addCloseCallback((transaction1, result) -> {
                        if (result.wasCommitted()) data.addProgress(task, -itemsRemoved);
                    });
                    return itemsRemoved;
                }
            }
            return 0L;
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public ItemVariant getResource() {
            return getTask() instanceof ItemTask t ? ItemVariant.of(t.getItemStack()) : getBlankVariant();
        }
    }

    private class FluidStorageHandler extends SingleVariantStorage<FluidVariant> {
        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return getTask() instanceof FluidTask t ? t.getMaxProgress() : 0L;
        }

        @Override
        public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
            if (getTask() instanceof FluidTask task) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(task.getQuest()) && !data.isCompleted(task)) {
                    updateSnapshots(transaction);
                    long space = task.getMaxProgress() - data.getProgress(task);
                    long toAdd = Math.min(maxAmount, space);
                    transaction.addCloseCallback((transaction1, result) -> {
                        if (result.wasCommitted()) data.addProgress(task, toAdd);
                    });
                    return (int) toAdd;
                }
            }
            return 0L;
        }

        @Override
        public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
            if (getTask() instanceof FluidTask task) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(task.getQuest()) && !data.isCompleted(task)) {
                    long toTake = Math.min(maxAmount, data.getProgress(task));
                    updateSnapshots(transaction);
                    transaction.addCloseCallback((transaction1, result) -> {
                        if (result.wasCommitted()) data.addProgress(task, -toTake);
                    });
                    return toTake;
                }
            }
            return 0L;
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public FluidVariant getResource() {
            return getTask() instanceof FluidTask t ? FluidVariant.of(t.getFluid(), t.getFluidNBT()) : getBlankVariant();
        }
    }

    private class RebornEnergyStorageHandler implements EnergyStorage {
        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            if (getTask() instanceof EnergyTask task) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(task.getQuest()) && !data.isCompleted(task)) {
                    long space = task.getMaxProgress() - data.getProgress(task);
                    long toAdd = Math.min(task.getMaxInput(), Math.min(maxAmount, space));
                    transaction.addCloseCallback((transaction1, result) -> {
                        if (result.wasCommitted()) data.addProgress(task, toAdd);
                    });
                    return toAdd;
                }
            }
            return 0L;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            return 0L;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public long getAmount() {
            return getTask() instanceof EnergyTask task ? (int) getCachedTeamData().getProgress(task) : 0L;
        }

        @Override
        public long getCapacity() {
            return getTask() instanceof EnergyTask t ? t.getValue() : 0L;
        }
    }
}
