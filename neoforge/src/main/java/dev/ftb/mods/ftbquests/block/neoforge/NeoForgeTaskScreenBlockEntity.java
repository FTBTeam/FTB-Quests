package dev.ftb.mods.ftbquests.block.neoforge;

import dev.ftb.mods.ftbquests.block.TaskScreenBlock;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.integration.item_filtering.ItemMatchingSystem;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.EnergyTask;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;

public class NeoForgeTaskScreenBlockEntity extends TaskScreenBlockEntity {
    private AABB cachedRenderAABB = null;

    private final ResourceHandler<ItemResource> itemHandler = new TaskItemHandler();
    private final ResourceHandler<FluidResource> fluidHandler = new TaskFluidHandler();
    private final EnergyHandler energyHandler = new TaskEnergyHandler();

    public NeoForgeTaskScreenBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @Override
    public Task getTask() {
        return super.getTask();
    }

    @Override
    public void setTask(Task task) {
        super.setTask(task);
    }

    public ResourceHandler<ItemResource> getItemHandler() {
        return itemHandler;
    }

    public ResourceHandler<FluidResource> getFluidHandler() {
        return fluidHandler;
    }

    public EnergyHandler getEnergyHandler() {
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

    private class TaskItemHandler implements ResourceHandler<ItemResource> {
        private long progress;
        private final ProgressSnapshot snapshot = new ProgressSnapshot(this.progress);

        @Override
        public int size() {
            return 2;
        }

        @Override
        public ItemResource getResource(int slot) {
            return getTask() instanceof ItemTask itemTask && slot == 0 ?
                    ItemResource.of(itemTask.getItemStack()) :
                    ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int slot) {
            if (getTask() instanceof ItemTask itemTask) {
                return Math.min(getCachedTeamData().getProgress(itemTask), itemTask.getItemStack().getMaxStackSize());
            }

            return 0;
        }

        @Override
        public long getCapacityAsLong(int slot, ItemResource resource) {
            return resource.getMaxStackSize();
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return getTask() instanceof ItemTask itemTask && itemTask.test(resource.toStack());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, @NonNull TransactionContext transaction) {
            TeamData data = getCachedTeamData();
            ItemStack stack = resource.toStack();
            if (getTask() instanceof ItemTask itemTask && data.canStartTasks(itemTask.getQuest())) {
                // task.insert() handles testing the item is valid and the task isn't already completed
                ItemStack res = itemTask.insert(data, stack, true);
                int nAdded = stack.getCount() - res.getCount();
                if (nAdded > 0) {
                    this.snapshot.updateSnapshots(transaction);
                    progress = getCachedTeamData().getProgress(itemTask) + nAdded;
                }
                return nAdded;
            }
            return 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (getTask() instanceof ItemTask itemTask && !isInputOnly() && !ItemMatchingSystem.INSTANCE.isItemFilter(itemTask.getItemStack())) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(itemTask.getQuest()) && !data.isCompleted(itemTask)) {
                    int nRemoved = (int) Math.min(data.getProgress(itemTask), amount);
                    if (nRemoved > 0) {
                        this.snapshot.updateSnapshots(transaction);
                        progress = getCachedTeamData().getProgress(itemTask) - nRemoved;
                    }
                    return nRemoved;
                }
            }
            return 0;
        }
    }

    private class TaskFluidHandler implements ResourceHandler<FluidResource> {
        private long progress;
        private final ProgressSnapshot snapshot = new ProgressSnapshot(this.progress);

        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int i) {
            return getTask() instanceof FluidTask fluidTask ?
                    FluidResource.of(fluidTask.getFluid(), fluidTask.getFluidDataComponentPatch()) :
                    FluidResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int i) {
            return getTask() instanceof FluidTask fluidTask && getCachedTeamData() != null ? getCachedTeamData().getProgress(fluidTask) : 0L;
        }

        @Override
        public long getCapacityAsLong(int i, FluidResource resource) {
            return getTask() instanceof FluidTask t ? t.getMaxProgress() : 0L;
        }

        @Override
        public boolean isValid(int i, FluidResource resource) {
            return false;
        }

        @Override
        public int insert(int i, FluidResource resource, int amount, TransactionContext transaction) {
            if (getTask() instanceof FluidTask fluidTask) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(fluidTask.getQuest()) && !data.isCompleted(fluidTask) && fluidTask.getFluid() == resource.getFluid()) {
                    long curProgress = data.getProgress(fluidTask);
                    long space = fluidTask.getMaxProgress() - curProgress;
                    long toAdd = Math.min(amount, space);
                    if (toAdd > 0L) {
                        this.snapshot.updateSnapshots(transaction);
                        progress = curProgress + toAdd;
                    }
                    return Math.toIntExact(toAdd);
                }
            }

            return 0;
        }

        @Override
        public int extract(int i, FluidResource resource, int amount, TransactionContext transaction) {
            if (getTask() instanceof FluidTask fluidTask) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(fluidTask.getQuest()) && !data.isCompleted(fluidTask)) {
                    long curProgress = data.getProgress(fluidTask);
                    long toTake = Math.min(amount, curProgress);
                    if (toTake > 0L) {
                        this.snapshot.updateSnapshots(transaction);
                        progress = curProgress - toTake;
                    }
                    return Math.toIntExact(toTake);
                }
            }

            return 0;
        }
    }

    private class TaskEnergyHandler implements EnergyHandler {
        private long progress;
        private final ProgressSnapshot snapshot = new ProgressSnapshot(this.progress);

        @Override
        public long getAmountAsLong() {
            return getTask() instanceof EnergyTask energyTask && getCachedTeamData() != null ? (int) getCachedTeamData().getProgress(energyTask) : 0L;
        }

        @Override
        public long getCapacityAsLong() {
            return getTask() instanceof EnergyTask energyTask ? energyTask.getValue() : 0L;
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            if (getTask() instanceof EnergyTask energyTask) {
                TeamData data = getCachedTeamData();
                if (data != null && data.canStartTasks(energyTask.getQuest()) && !data.isCompleted(energyTask)) {
                    long space = energyTask.getMaxProgress() - data.getProgress(energyTask);
                    long toAdd = Math.min(energyTask.getMaxInput(), Math.min(amount, space));
                    if (toAdd > 0L) {
                        this.snapshot.updateSnapshots(transaction);
                        progress = data.getProgress(energyTask) + toAdd;
                    }
                    return Math.toIntExact(toAdd);
                }
            }
            return 0;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return 0;
        }
    }

    private class ProgressSnapshot extends SnapshotJournal<Long> {
        private long parentProgressRef;

        public ProgressSnapshot(long parentProgress) {
            this.parentProgressRef = parentProgress;
        }

        @Override
        protected Long createSnapshot() {
            var teamData = getCachedTeamData();
            if (teamData == null) {
                return 0L;
            }

            return getCachedTeamData().getProgress(getTask());
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            parentProgressRef = snapshot;
        }

        @Override
        protected void onRootCommit(Long originalState) {
            TeamData data = getCachedTeamData();
            if (data == null) {
                return;
            }

            long newState = parentProgressRef;
            if (!originalState.equals(newState)) {
                data.setProgress(getTask(), newState);
            }
        }
    }
}
