package dev.ftb.mods.ftbquests.client.neoforge;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

import dev.ftb.mods.ftbquests.registry.ModBlocks;

import java.util.function.Function;

public class ModelBakeEventHandler {
    private ModelBakeEventHandler() {}

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        override(event, ModBlocks.BARRIER.get(), CamouflagingModel::new);
        override(event, ModBlocks.STAGE_BARRIER.get(), CamouflagingModel::new);
    }

    private static void override(ModelEvent.ModifyBakingResult event, Block block, Function<BlockStateModel, CamouflagingModel> f) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            BlockStateModel model = event.getBakingResult().blockStateModels().get(state);
            if (model != null) {
                event.getBakingResult().blockStateModels().put(state, f.apply(model));
            }
        }
    }
}
