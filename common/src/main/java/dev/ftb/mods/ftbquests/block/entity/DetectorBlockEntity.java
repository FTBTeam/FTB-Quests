package dev.ftb.mods.ftbquests.block.entity;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import me.shedaniel.architectury.hooks.PlayerHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * @author LatvianModder
 */
public class DetectorBlockEntity extends BlockEntity {
	public long object = 0L;
	public int radius = 8;

	public DetectorBlockEntity() {
		super(FTBQuestsBlockEntities.DETECTOR.get());
	}

	@Override
	public void load(BlockState state, CompoundTag tag) {
		super.load(state, tag);

		object = QuestObjectBase.parseCodeString(tag.getString("Object"));

		if (object == 0L) {
			object = tag.getLong("object");
		}

		radius = tag.getInt("Radius");
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		tag.putString("Object", QuestObjectBase.getCodeString(object));
		tag.putInt("Radius", radius);
		return super.save(tag);
	}

	public void update(String s) {
		object = ServerQuestFile.INSTANCE.getID(s);
	}

	private static boolean isReal(ServerPlayer p) {
		return !PlayerHooks.isFake(p);
	}

	public void powered(Level level, BlockPos p) {
		QuestObjectBase o = ServerQuestFile.INSTANCE.getBase(object);

		if (o == null) {
			return;
		}

		ProgressChange change = new ProgressChange(ServerQuestFile.INSTANCE);
		change.origin = o;
		change.reset = false;
		change.notifications = true;

		for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(p.getX() - radius, p.getY() - radius, p.getZ() - radius, p.getX() + 1D + radius, p.getY() + 1D + radius, p.getZ() + 1D + radius), DetectorBlockEntity::isReal)) {
			TeamData data = ServerQuestFile.INSTANCE.getData(player);

			change.player = player.getUUID();
			o.forceProgressRaw(data, change);
		}
	}
}
