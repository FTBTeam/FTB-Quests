package dev.ftb.mods.ftbquests.block.entity;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.block.StageBarrierBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static dev.ftb.mods.ftbquests.block.QuestBarrierBlock.OPEN;

/**
 * @author LatvianModder
 */
public class StageBarrierBlockEntity extends BlockEntity implements TickableBlockEntity, BarrierBlockEntity {
	public String stage = "";

	public StageBarrierBlockEntity() {
		super(FTBQuestsBlockEntities.STAGE_BARRIER.get());
	}

	public void readBarrier(CompoundTag tag) {
		stage = tag.getString("Stage");
	}

	public CompoundTag writeBarrier(CompoundTag tag) {
		tag.putString("Stage", stage);
		return tag;
	}

	@Override
	public void load(BlockState state, CompoundTag tag) {
		super.load(state, tag);
		readBarrier(tag);
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		return writeBarrier(super.save(tag));
	}

	@Override
	public void tick() {
		if (level != null && level.isClientSide() && FTBQuests.PROXY.isClientDataLoaded() && level.getGameTime() % 5L == 0L) {
			boolean open = isOpen(FTBQuests.PROXY.getClientPlayer());

			if (open != getBlockState().getValue(OPEN)) {
				level.setBlock(getBlockPos(), getBlockState().setValue(OPEN, open), 2 | 8);
				clearCache();
			}
		}
	}

	@Override
	public void update(String s) {
		stage = s;
		syncData();
	}

	public void syncData() {
	}

	@Override
	public boolean isOpen(Player player) {
		return !stage.isEmpty() && StageBarrierBlock.hasStage(player, stage);
	}
}
