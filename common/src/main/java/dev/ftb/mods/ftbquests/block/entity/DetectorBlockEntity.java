package dev.ftb.mods.ftbquests.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import dev.architectury.hooks.level.entity.PlayerHooks;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbquests.util.ProgressChange;

public class DetectorBlockEntity extends BlockEntity {
	private long objectId = 0L;
	private int radius = 8;

	public DetectorBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ModBlockEntityTypes.DETECTOR.get(), blockPos, blockState);
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		objectId = QuestObjectBase.parseCodeString(input.getString("Object").orElseThrow());
		radius = input.getIntOr("Radius", 0);
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		output.putString("Object", QuestObjectBase.getCodeString(objectId));
		output.putInt("Radius", radius);
	}

	public void update(String idStr) {
		objectId = ServerQuestFile.getInstance().getID(idStr);
	}

	private static boolean isRealPlayer(ServerPlayer player) {
		return !PlayerHooks.isFake(player);
	}

	public void onPowered(Level level, BlockPos pos) {
		QuestObjectBase qo = ServerQuestFile.getInstance().getBase(objectId);
		if (qo != null) {
			AABB box = new AABB(pos).inflate(radius);
			for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, box, DetectorBlockEntity::isRealPlayer)) {
				ServerQuestFile.getInstance().getTeamData(player).ifPresent(data ->
						qo.forceProgressRaw(data, new ProgressChange(qo, player.getUUID()).setReset(false).withNotifications()));
			}
		}
	}
}
