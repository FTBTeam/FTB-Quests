package dev.ftb.mods.ftbquests.block.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import dev.ftb.mods.ftbquests.block.entity.TaskScreenAuxBlockEntity;

public class NeoForgeTaskScreenAuxBlockEntity extends TaskScreenAuxBlockEntity {
    public NeoForgeTaskScreenAuxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    public ResourceHandler<ItemResource> getItemHandler() {
        return getCoreScreen().map(s -> ((NeoForgeTaskScreenBlockEntity) s).getItemHandler()).orElse(null);
    }

    public ResourceHandler<FluidResource> getFluidHandler() {
        return getCoreScreen().map(s -> ((NeoForgeTaskScreenBlockEntity) s).getFluidHandler()).orElse(null);
    }

    public EnergyHandler getEnergyHandler() {
        return getCoreScreen().map(s -> ((NeoForgeTaskScreenBlockEntity) s).getEnergyHandler()).orElse(null);
    }
}
