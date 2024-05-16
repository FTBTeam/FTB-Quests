package dev.ftb.mods.ftbquests.block.fabric;

import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.integration.item_filtering.ItemMatchingSystem;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.EnergyTask;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

@SuppressWarnings("UnstableApiUsage")
public class FabricTaskScreenBlockEntity extends TaskScreenBlockEntity {
    private final ItemStorageHandler itemStorage = new ItemStorageHandler();
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
        public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
            TeamData data = getCachedTeamData();
            ItemStack stack = insertedVariant.toStack();
            if (getTask() instanceof ItemTask itemTask && data.canStartTasks(itemTask.getQuest())) {
                // task.insert() handles testing the item is valid and the task isn't already completed
                ItemStack res = itemTask.insert(data, stack, true);
                int nAdded = stack.getCount() - res.getCount();
                if (nAdded > 0) {
                    updateSnapshots(transaction);
                    amount = getCachedTeamData().getProgress(itemTask) + nAdded;
                }
                return nAdded;
            }
            return 0L;
        }

        @Override
        public long extract(ItemVariant extractedVariant, long maxAmount, TransactionContext transaction) {
            if (getTask() instanceof ItemTask itemTask && !isInputOnly() && !ItemMatchingSystem.INSTANCE.isItemFilter(itemTask.getItemStack())) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(itemTask.getQuest()) && !data.isCompleted(itemTask)) {
                    int nRemoved = (int) Math.min(data.getProgress(itemTask), maxAmount);
                    if (nRemoved > 0) {
                        updateSnapshots(transaction);
                        amount = getCachedTeamData().getProgress(itemTask) - nRemoved;
                    }
                    return nRemoved;
                }
            }
            return 0L;
        }

        @Override
        protected void onFinalCommit() {
            if (getTask() instanceof ItemTask itemTask && getCachedTeamData() != null) {
                getCachedTeamData().setProgress(itemTask, amount);
            }
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        protected long getCapacity(ItemVariant variant) {
            return getTask() instanceof ItemTask itemTask ? itemTask.getMaxProgress() : 0L;
        }

        @Override
        public long getAmount() {
            return getTask() instanceof ItemTask itemTask && getCachedTeamData() != null ? getCachedTeamData().getProgress(itemTask) : 0L;
        }

        @Override
        public ItemVariant getResource() {
            return getTask() instanceof ItemTask itemTask ? ItemVariant.of(itemTask.getItemStack()) : getBlankVariant();
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
            if (getTask() instanceof FluidTask fluidTask) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(fluidTask.getQuest()) && !data.isCompleted(fluidTask) && fluidTask.getFluid() == insertedVariant.getFluid()) {
                    long curProgress = data.getProgress(fluidTask);
                    long space = fluidTask.getMaxProgress() - curProgress;
                    long toAdd = Math.min(maxAmount, space);
                    if (toAdd > 0L) {
                        updateSnapshots(transaction);
                        amount = curProgress + toAdd;
                    }
                    return toAdd;
                }
            }
            return 0L;
        }

        @Override
        public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
            if (getTask() instanceof FluidTask fluidTask) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(fluidTask.getQuest()) && !data.isCompleted(fluidTask)) {
                    long curProgress = data.getProgress(fluidTask);
                    long toTake = Math.min(maxAmount, curProgress);
                    if (toTake > 0L) {
                        updateSnapshots(transaction);
                        amount = curProgress - toTake;
                    }
                    return toTake;
                }
            }
            return 0L;
        }

        @Override
        protected void onFinalCommit() {
            if (getTask() instanceof FluidTask fluidTask && getCachedTeamData() != null) {
                getCachedTeamData().setProgress(fluidTask, amount);
            }
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public FluidVariant getResource() {
            return getTask() instanceof FluidTask fluidTask ?
                    FluidVariant.of(fluidTask.getFluid(), fluidTask.getFluidDataComponentPatch()) :
                    getBlankVariant();
        }

        @Override
        public long getAmount() {
            return getTask() instanceof FluidTask fluidTask && getCachedTeamData() != null ? getCachedTeamData().getProgress(fluidTask) : 0L;
        }
    }

    private class RebornEnergyStorageHandler extends SimpleEnergyStorage {
        public RebornEnergyStorageHandler() {
            // these don't really matter; we override any methods which use them
            super(Long.MAX_VALUE, Long.MAX_VALUE, 0L);
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            if (getTask() instanceof EnergyTask energyTask) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(energyTask.getQuest()) && !data.isCompleted(energyTask)) {
                    long space = energyTask.getMaxProgress() - data.getProgress(energyTask);
                    long toAdd = Math.min(energyTask.getMaxInput(), Math.min(maxAmount, space));
                    if (toAdd > 0L) {
                        updateSnapshots(transaction);
                        amount = data.getProgress(energyTask) + toAdd;
                    }
                    return toAdd;
                }
            }
            return 0L;
        }

        @Override
        protected void onFinalCommit() {
            if (getTask() instanceof EnergyTask energyTask && getCachedTeamData() != null) {
                getCachedTeamData().setProgress(energyTask, amount);
            }
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
            return getTask() instanceof EnergyTask energyTask && getCachedTeamData() != null ? (int) getCachedTeamData().getProgress(energyTask) : 0L;
        }

        @Override
        public long getCapacity() {
            return getTask() instanceof EnergyTask energyTask ? energyTask.getValue() : 0L;
        }
    }
}
