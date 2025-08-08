package dev.ftb.mods.ftbquests.client.neoforge;

import dev.ftb.mods.ftbquests.registry.ModBlocks;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.function.Function;

public class ModelBakeEventHandler {
    private ModelBakeEventHandler() {}

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        override(event, ModBlocks.BARRIER.get(), CamouflagingModel::new);
        override(event, ModBlocks.STAGE_BARRIER.get(), CamouflagingModel::new);
    }

    private static void override(ModelEvent.ModifyBakingResult event, Block block, Function<BakedModel, CamouflagingModel> f) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            ModelResourceLocation loc = BlockModelShaper.stateToModelLocation(state);
            BakedModel model = event.getModels().get(loc);
            if (model != null) {
                event.getModels().put(loc, f.apply(model));
            }
        }
    }
}
