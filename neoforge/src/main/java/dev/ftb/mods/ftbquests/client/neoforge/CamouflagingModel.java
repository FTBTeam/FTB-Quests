// TODO: @since 21.11: Come back to this
package dev.ftb.mods.ftbquests.client.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import dev.ftb.mods.ftbquests.block.QuestBarrierBlock;
import dev.ftb.mods.ftbquests.block.neoforge.NeoQuestBarrierBlockEntity;

import java.util.List;

// TODO only partially works, but good enough for simple solid block camo
public class CamouflagingModel extends DelegateBlockStateModel {
    protected CamouflagingModel(BlockStateModel delegate) {
        super(delegate);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        if (state == null || !(state.getBlock() instanceof QuestBarrierBlock)) {
            super.collectParts(level, pos, state, random, parts);
        }
        ModelData modelData = level.getModelData(pos);
        BlockState camoState = modelData.get(NeoQuestBarrierBlockEntity.CAMOUFLAGE_STATE);

//        if (renderType == null) {
//            renderType = RenderType.solid(); // workaround for when this isn't set (digging, etc.)
//        }

        if ((camoState == null || camoState.getBlock() instanceof QuestBarrierBlock) /*&& renderType == RenderType.solid()*/) {
            // No camo (or bad camo!)
            super.collectParts(level, pos, state, random, parts);
        } else if (camoState != null /*&& getRenderTypes(camoState, rand, modelData).contains(renderType)*/) {
            // Steal camo's model
            BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(camoState);
            model.collectParts(level, pos, state, random, parts);
        } else {
            // Not rendering in this layer
        }
    }
}
