package dev.ftb.mods.ftbquests.block.entity;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;

public class TaskScreenAuxBlockEntity extends BlockEntity implements ITaskScreen, Nameable {
    @NotNull
    private WeakReference<TaskScreenBlockEntity> coreScreen = new WeakReference<>(null);
    private BlockPos corePosPending;  // non-null after NBT load & before querying/resolving

    public TaskScreenAuxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(FTBQuestsBlockEntities.AUX_TASK_SCREEN.get(), blockPos, blockState);
    }

    @Override
    public Component getName() {
        return getCoreScreen().map(s -> s.getBlockState().getBlock().getName()).orElse(Component.literal("Task Screen"));
    }

    @Override
    public Optional<TaskScreenBlockEntity> getCoreScreen() {
        if (corePosPending != null) {
            // first core screen query since loaded from NBT
            TaskScreenBlockEntity core = level.getBlockEntity(corePosPending, FTBQuestsBlockEntities.CORE_TASK_SCREEN.get()).orElse(null);
            if (core != null) {
                coreScreen = new WeakReference<>(core);
                corePosPending = null;
            } else {
                // something's gone wrong & the core no longer exists?
                level.destroyBlock(getBlockPos(), false, null);
            }
        }
        return Optional.ofNullable(coreScreen.get());
    }

    public void setCoreScreen(@NotNull TaskScreenBlockEntity coreScreen) {
        // this must ONLY be called from TaskScreenBlock#onPlacedBy() !
        if (this.coreScreen.get() != null) throw new IllegalStateException("coreScreen is already set and can't be changed!");

        this.coreScreen = new WeakReference<>(coreScreen);
        setChanged();
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        corePosPending = compoundTag.contains("CorePos") ? NbtUtils.readBlockPos(compoundTag.getCompound("CorePos")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        if (corePosPending != null) {
            compoundTag.put("CorePos", NbtUtils.writeBlockPos(corePosPending));
        } else {
            TaskScreenBlockEntity cs = coreScreen.get();
            if (cs != null) {
                compoundTag.put("CorePos", NbtUtils.writeBlockPos(cs.getBlockPos()));
            }
        }
    }

    @Override
    public boolean isIndestructible() {
        return getCoreScreen().map(TaskScreenBlockEntity::isIndestructible).orElse(false);
    }

    @Override
    public ItemStack getSkin() {
        return getCoreScreen().map(TaskScreenBlockEntity::getSkin).orElse(ItemStack.EMPTY);
    }

    @NotNull
    @Override
    public UUID getTeamId() {
        return getCoreScreen().map(TaskScreenBlockEntity::getTeamId).orElse(Util.NIL_UUID);
    }

    @Override
    public boolean isInputOnly() {
        return getCoreScreen().map(TaskScreenBlockEntity::isInputOnly).orElse(false);
    }
}
