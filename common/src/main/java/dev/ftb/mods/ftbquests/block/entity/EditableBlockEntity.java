package dev.ftb.mods.ftbquests.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class EditableBlockEntity extends BlockEntity implements IEditable {
    public EditableBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public void readPayload(CompoundTag tag, RegistryAccess registryAccess) {
        loadAdditional(tag, registryAccess);
    }
}
