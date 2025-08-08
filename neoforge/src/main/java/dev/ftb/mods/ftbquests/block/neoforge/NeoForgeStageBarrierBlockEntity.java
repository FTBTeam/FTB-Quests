package dev.ftb.mods.ftbquests.block.neoforge;

import dev.ftb.mods.ftbquests.block.entity.StageBarrierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class NeoForgeStageBarrierBlockEntity extends StageBarrierBlockEntity {
    public NeoForgeStageBarrierBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @Override
    public void forceAppearanceUpdate() {
        super.forceAppearanceUpdate();
        requestModelDataUpdate();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        requestModelDataUpdate();
        level.setBlocksDirty(worldPosition, Blocks.AIR.defaultBlockState(), getBlockState());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        requestModelDataUpdate();
        level.setBlocksDirty(worldPosition, Blocks.AIR.defaultBlockState(), getBlockState());
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder()
                .with(NeoForgeQuestBarrierBlockEntity.CAMOUFLAGE_STATE, getClientAppearance())
                .build();
    }
}
