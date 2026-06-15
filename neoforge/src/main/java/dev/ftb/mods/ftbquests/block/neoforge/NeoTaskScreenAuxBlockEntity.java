package dev.ftb.mods.ftbquests.block.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import dev.ftb.mods.ftbquests.block.entity.TaskScreenAuxBlockEntity;

public class NeoTaskScreenAuxBlockEntity extends TaskScreenAuxBlockEntity {
    public NeoTaskScreenAuxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    public ResourceHandler<ItemResource> getItemHandler() {
        return getCoreScreen().map(s -> ((NeoTaskScreenBlockEntity) s).getItemHandler()).orElse(null);
    }

    public ResourceHandler<FluidResource> getFluidHandler() {
        return getCoreScreen().map(s -> ((NeoTaskScreenBlockEntity) s).getFluidHandler()).orElse(null);
    }

    public EnergyHandler getEnergyHandler() {
        return getCoreScreen().map(s -> ((NeoTaskScreenBlockEntity) s).getEnergyHandler()).orElse(null);
    }
}
