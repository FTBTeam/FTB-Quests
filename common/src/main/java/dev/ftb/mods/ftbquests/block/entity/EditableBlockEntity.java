package dev.ftb.mods.ftbquests.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;

public abstract class EditableBlockEntity extends BlockEntity implements IEditable {
    public EditableBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public void readPayload(CompoundTag tag, RegistryAccess registryAccess) throws Exception {
        ProblemReporter.Collector reporter = new ProblemReporter.Collector();

        loadAdditional(TagValueInput.create(reporter, registryAccess, tag));

        if (!reporter.isEmpty()) {
            throw new Exception("Failed to read EditableBlockEntity payload: " + reporter.getReport());
        }
    }
}
