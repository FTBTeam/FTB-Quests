package dev.ftb.mods.ftbquests.block.fabric;

import dev.ftb.mods.ftbquests.block.entity.TaskScreenAuxBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;

@SuppressWarnings("UnstableApiUsage")
public class FabricTaskScreenAuxBlockEntity extends TaskScreenAuxBlockEntity {
    public FabricTaskScreenAuxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    public Storage<ItemVariant> getItemStorage() {
        return getCoreScreen().map(s -> ((FabricTaskScreenBlockEntity) s).getItemStorage()).orElse(null);
    }

    public Storage<FluidVariant> getFluidStorage() {
        return getCoreScreen().map(s -> ((FabricTaskScreenBlockEntity) s).getFluidStorage()).orElse(null);
    }

    public EnergyStorage getEnergyStorage() {
        return getCoreScreen().map(s -> ((FabricTaskScreenBlockEntity) s).getEnergyStorage()).orElse(null);
    }
}
