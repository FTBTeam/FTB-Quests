package dev.ftb.mods.ftbquests.block.entity;

import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class DetectorBlockEntity extends BlockEntity {
	private long objectId = 0L;
	private int radius = 8;

	public DetectorBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(FTBQuestsBlockEntities.DETECTOR.get(), blockPos, blockState);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		objectId = QuestObjectBase.parseCodeString(tag.getString("Object"));
		radius = tag.getInt("Radius");
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		tag.putString("Object", QuestObjectBase.getCodeString(objectId));
		tag.putInt("Radius", radius);
	}

	public void update(String idStr) {
		objectId = ServerQuestFile.INSTANCE.getID(idStr);
	}

	private static boolean isRealPlayer(ServerPlayer player) {
		return !PlayerHooks.isFake(player);
	}

	public void onPowered(Level level, BlockPos pos) {
		QuestObjectBase qo = ServerQuestFile.INSTANCE.getBase(objectId);
		if (qo != null) {
			AABB box = new AABB(pos).inflate(radius);
			for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, box, DetectorBlockEntity::isRealPlayer)) {
				TeamData data = ServerQuestFile.INSTANCE.getData(player);
				qo.forceProgressRaw(data, new ProgressChange(ServerQuestFile.INSTANCE, qo, player.getUUID()).setReset(false).withNotifications());
			}
		}
	}
}
