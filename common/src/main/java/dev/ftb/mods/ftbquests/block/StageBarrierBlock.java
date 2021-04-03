package dev.ftb.mods.ftbquests.block;

import dev.ftb.mods.ftbquests.block.entity.FTBQuestsBlockEntities;
import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class StageBarrierBlock extends QuestBarrierBlock {
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockGetter bg) {
		return FTBQuestsBlockEntities.createStageBarrierEntity();
	}

	public static boolean hasStage(Player player, String stage) {
		return hasStage0(player, stage);
	}

	@ExpectPlatform
	private static boolean hasStage0(Player player, String stage) {
		throw new AssertionError();
	}
}
