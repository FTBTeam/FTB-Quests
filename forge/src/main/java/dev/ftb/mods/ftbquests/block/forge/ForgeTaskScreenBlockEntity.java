package dev.ftb.mods.ftbquests.block.forge;

import dev.ftb.mods.ftbquests.block.TaskScreenBlock;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.forge.ForgeEnergyTask;
import dev.latvian.mods.itemfilters.api.ItemFiltersAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgeTaskScreenBlockEntity extends TaskScreenBlockEntity {
    private AABB cachedRenderAABB = null;
    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(TaskItemHandler::new);
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(TaskFluidHandler::new);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(TaskForgeEnergyHandler::new);

    public ForgeTaskScreenBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (cachedRenderAABB == null) {
            AABB box = super.getRenderBoundingBox();
            if (!(getBlockState().getBlock() instanceof TaskScreenBlock tsb) || tsb.getSize() == 1) {
                cachedRenderAABB = box;
            } else {
                cachedRenderAABB = box.inflate(tsb.getSize());
            }
        }
        return cachedRenderAABB;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        itemCap.invalidate();
        fluidCap.invalidate();
        energyCap.invalidate();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemCap.cast();
        } else if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidCap.cast();
        } else if (cap == CapabilityEnergy.ENERGY) {
            return energyCap.cast();
        }
        return super.getCapability(cap, side);
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
            return getTask() instanceof ItemTask t && slot == 0 ?
                    ItemHandlerHelper.copyStackWithSize(t.item, (int) Math.min(getCachedTeamData().getProgress(t), t.item.getMaxStackSize())) :
                    ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            TeamData data = getCachedTeamData();
            if (getTask() instanceof ItemTask task && data.canStartTasks(task.quest)) {
                // task.insert() handles testing the item is valid and the task isn't already completed
                return task.insert(data, stack, simulate);
            }
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int count, boolean simulate) {
            if (!isInputOnly() && getTask() instanceof ItemTask task && !ItemFiltersAPI.isFilter(task.item)) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(task.quest) && !data.isCompleted(task)) {
                    int itemsRemoved = (int) Math.min(data.getProgress(task), count);
                    if (!simulate) {
                        data.addProgress(task, -itemsRemoved);
                    }
                    return ItemHandlerHelper.copyStackWithSize(task.item, itemsRemoved);
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return getTask() instanceof ItemTask itemTask ? itemTask.item.getMaxStackSize() : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return getTask() instanceof ItemTask t && t.test(stack);
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
            return tank == 0 && getTask() instanceof FluidTask task ? new FluidStack(task.fluid, (int) getCachedTeamData().getProgress(task)) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 && getTask() instanceof FluidTask task ? (int) task.getMaxProgress() : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack fluidStack) {
            return tank == 0 && getTask() instanceof FluidTask task
                    && task.fluid == fluidStack.getFluid()
                    && (task.fluidNBT == null || task.fluidNBT.equals(fluidStack.getTag()));
        }

        @Override
        public int fill(FluidStack fluidStack, FluidAction fluidAction) {
            if (getTask() instanceof FluidTask task) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(task.quest) && !data.isCompleted(task)) {
                    long space = task.getMaxProgress() - data.getProgress(task);
                    long toAdd = Math.min(fluidStack.getAmount(), space);
                    if (fluidAction.execute()) {
                        data.addProgress(task, Math.min(fluidStack.getAmount(), toAdd));
                    }
                    return (int) toAdd;
                }
            }
            return 0;
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
            return getTask() instanceof FluidTask task && fluidStack.getFluid() == task.fluid ?
                    drain(fluidStack.getAmount(), fluidAction) :
                    FluidStack.EMPTY;
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction fluidAction) {
            if (getTask() instanceof FluidTask task) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(task.quest) && !data.isCompleted(task)) {
                    long toTake = Math.min(maxDrain, data.getProgress(task));
                    if (fluidAction.execute()) {
                        data.addProgress(task, -toTake);
                    }
                    FluidStack result = new FluidStack(task.fluid, (int) toTake);
                    if (task.fluidNBT != null) result.setTag(task.fluidNBT.copy());
                    return result;
                }
            }
            return FluidStack.EMPTY;
        }
    }

    private class TaskForgeEnergyHandler implements IEnergyStorage {
        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            if (getTask() instanceof ForgeEnergyTask task) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(task.quest) && !data.isCompleted(task)) {
                    long space = task.getMaxProgress() - data.getProgress(task);
                    long toAdd = Math.min(task.maxInput, Math.min(amount, space));
                    if (!simulate) {
                        data.addProgress(task, toAdd);
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
            return getTask() instanceof ForgeEnergyTask task ? (int) getCachedTeamData().getProgress(task) : 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return getTask() instanceof ForgeEnergyTask task ? (int) task.value : 0;
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
