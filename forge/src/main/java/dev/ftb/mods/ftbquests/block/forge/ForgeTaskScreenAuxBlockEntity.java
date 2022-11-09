package dev.ftb.mods.ftbquests.block.forge;

import dev.ftb.mods.ftbquests.block.entity.TaskScreenAuxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgeTaskScreenAuxBlockEntity extends TaskScreenAuxBlockEntity {
    public ForgeTaskScreenAuxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return getCoreScreen().map(s -> s.getCapability(cap, side)).orElse(LazyOptional.empty());
    }
}
