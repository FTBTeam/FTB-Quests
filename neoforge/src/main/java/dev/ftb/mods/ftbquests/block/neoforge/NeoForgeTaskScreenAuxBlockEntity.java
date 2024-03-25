package dev.ftb.mods.ftbquests.block.neoforge;

import dev.ftb.mods.ftbquests.block.entity.TaskScreenAuxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public class NeoForgeTaskScreenAuxBlockEntity extends TaskScreenAuxBlockEntity {
    public NeoForgeTaskScreenAuxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    public IItemHandler getItemHandler() {
        return getCoreScreen().map(s -> ((NeoForgeTaskScreenBlockEntity) s).getItemHandler()).orElse(null);
    }

    public IFluidHandler getFluidHandler() {
        return getCoreScreen().map(s -> ((NeoForgeTaskScreenBlockEntity) s).getFluidHandler()).orElse(null);
    }

    public IEnergyStorage getEnergyHandler() {
        return getCoreScreen().map(s -> ((NeoForgeTaskScreenBlockEntity) s).getEnergyHandler()).orElse(null);
    }
}
