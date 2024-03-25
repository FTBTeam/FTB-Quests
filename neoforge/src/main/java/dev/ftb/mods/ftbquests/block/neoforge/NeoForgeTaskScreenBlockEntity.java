package dev.ftb.mods.ftbquests.block.neoforge;

import dev.ftb.mods.ftbquests.block.TaskScreenBlock;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.integration.item_filtering.ItemMatchingSystem;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.neoforge.ForgeEnergyTask;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class NeoForgeTaskScreenBlockEntity extends TaskScreenBlockEntity {
    private AABB cachedRenderAABB = null;

    private final TaskItemHandler itemHandler = new TaskItemHandler();
    private final TaskFluidHandler fluidHandler = new TaskFluidHandler();
    private final TaskEnergyHandler energyHandler = new TaskEnergyHandler();

    public NeoForgeTaskScreenBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public IEnergyStorage getEnergyHandler() {
        return energyHandler;
    }

    public AABB getRenderBoundingBox() {
        if (cachedRenderAABB == null) {
            AABB box = new AABB(getBlockPos());
            if (!(getBlockState().getBlock() instanceof TaskScreenBlock tsb) || tsb.getSize() == 1) {
                cachedRenderAABB = box;
            } else {
                cachedRenderAABB = box.inflate(tsb.getSize());
            }
        }
        return cachedRenderAABB;
    }

    private class TaskItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            // exposing 2 slots allows us to keep one slot potentially "full" and the other always empty
            // - stops potential inserters thinking the inventory is completely full and not bothering
            return 2;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            // slot 1 is always empty - see above
            return getTask() instanceof ItemTask itemTask && slot == 0 ?
                    ItemHandlerHelper.copyStackWithSize(itemTask.getItemStack(), (int) Math.min(getCachedTeamData().getProgress(itemTask), itemTask.getItemStack().getMaxStackSize())) :
                    ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            TeamData data = getCachedTeamData();
            if (getTask() instanceof ItemTask itemTask && data.canStartTasks(itemTask.getQuest())) {
                // task.insert() handles testing the item is valid and the task isn't already completed
                return itemTask.insert(data, stack, simulate);
            }
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int count, boolean simulate) {
            if (!isInputOnly() && getTask() instanceof ItemTask itemTask && !ItemMatchingSystem.INSTANCE.isItemFilter(itemTask.getItemStack())) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(itemTask.getQuest()) && !data.isCompleted(itemTask)) {
                    int itemsRemoved = (int) Math.min(data.getProgress(itemTask), count);
                    if (!simulate) {
                        data.addProgress(itemTask, -itemsRemoved);
                    }
                    return ItemHandlerHelper.copyStackWithSize(itemTask.getItemStack(), itemsRemoved);
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return getTask() instanceof ItemTask itemTask ? itemTask.getItemStack().getMaxStackSize() : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return getTask() instanceof ItemTask itemTask && itemTask.test(stack);
        }
    }

    private class TaskFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 && getTask() instanceof FluidTask fluidTask ?
                    new FluidStack(fluidTask.getFluid(), (int) getCachedTeamData().getProgress(fluidTask)) :
                    FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 && getTask() instanceof FluidTask fluidTask ?
                    (int) fluidTask.getMaxProgress() :
                    0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack fluidStack) {
            return tank == 0 && getTask() instanceof FluidTask fluidTask
                    && fluidTask.getFluid() == fluidStack.getFluid()
                    && (fluidTask.getFluidNBT() == null || fluidTask.getFluidNBT().equals(fluidStack.getTag()));
        }

        @Override
        public int fill(FluidStack fluidStack, FluidAction fluidAction) {
            if (getTask() instanceof FluidTask fluidTask && isFluidValid(0, fluidStack)) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(fluidTask.getQuest()) && !data.isCompleted(fluidTask)) {
                    long space = fluidTask.getMaxProgress() - data.getProgress(fluidTask);
                    long toAdd = Math.min(fluidStack.getAmount(), space);
                    if (fluidAction.execute()) {
                        data.addProgress(fluidTask, Math.min(fluidStack.getAmount(), toAdd));
                    }
                    return (int) toAdd;
                }
            }
            return 0;
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
            return getTask() instanceof FluidTask task && fluidStack.getFluid() == task.getFluid() ?
                    drain(fluidStack.getAmount(), fluidAction) :
                    FluidStack.EMPTY;
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction fluidAction) {
            if (getTask() instanceof FluidTask task) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(task.getQuest()) && !data.isCompleted(task)) {
                    long toTake = Math.min(maxDrain, data.getProgress(task));
                    if (fluidAction.execute()) {
                        data.addProgress(task, -toTake);
                    }
                    FluidStack result = new FluidStack(task.getFluid(), (int) toTake);
                    if (task.getFluidNBT() != null) result.setTag(task.getFluidNBT().copy());
                    return result;
                }
            }
            return FluidStack.EMPTY;
        }
    }

    private class TaskEnergyHandler implements IEnergyStorage {
        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            if (getTask() instanceof ForgeEnergyTask energyTask) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(energyTask.getQuest()) && !data.isCompleted(energyTask)) {
                    long space = energyTask.getMaxProgress() - data.getProgress(energyTask);
                    long toAdd = Math.min(energyTask.getMaxInput(), Math.min(amount, space));
                    if (!simulate) {
                        data.addProgress(energyTask, toAdd);
                    }
                    return (int) toAdd;
                }
            }
            return 0;
        }

        @Override
        public int extractEnergy(int amount, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return getTask() instanceof ForgeEnergyTask energyTask ? (int) getCachedTeamData().getProgress(energyTask) : 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return getTask() instanceof ForgeEnergyTask energyTask ? (int) energyTask.getValue() : 0;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
