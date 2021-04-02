package dev.ftb.mods.ftbquests.quest.task;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import me.shedaniel.architectury.registry.Registries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * @author LatvianModder
 */
public class ObservationTask extends BooleanTask {
	public long timer;
	public int type;
	public String observeId;

	public ObservationTask(Quest quest) {
		super(quest);
		timer = 0L;
		type = 1;
		observeId = "minecraft:dirt";
	}

	@Override
	public TaskType getType() {
		return TaskTypes.OBSERVATION;
	}

	private String toStringO() {
		if (type <= 0 || observeId.isEmpty()) {
			return "";
		}

		if (type == 1) {
			return "block:" + observeId;
		} else if (type == 2) {
			return "block_entity:" + observeId;
		} else if (type == 3) {
			return "block_entity_class:" + observeId;
		}

		return "";
	}

	private void fromStringO(String string) {
		type = 0;
		observeId = "";

		try {
			if (string.startsWith("block:")) {
				observeId = string.substring(6);
				type = 1;
			} else if (string.startsWith("block_entity:")) {
				observeId = string.substring(6);
				type = 1;
			} else if (string.startsWith("block_entity_class:")) {
				observeId = string.substring(6);
				type = 1;
			}
		} catch (Exception ex) {
		}
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putLong("timer", timer);
		nbt.putString("observe", toStringO());
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		timer = nbt.getLong("timer");
		fromStringO(nbt.getString("observe"));
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeVarLong(timer);
		buffer.writeUtf(toStringO(), Short.MAX_VALUE);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		timer = buffer.readVarLong();
		fromStringO(buffer.readUtf(Short.MAX_VALUE));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addLong("timer", timer, v -> timer = v, 0L, 0L, 1200L);
		config.addString("observe", toStringO(), this::fromStringO, "block:minecraft:dirt");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		return true;
	}

	public boolean observe(Player player, HitResult result) {
		if (observeId.isEmpty() || type <= 0) {
			return false;
		}

		if (result instanceof BlockHitResult) {
			if (type == 1) {
				return observeId.equals(String.valueOf(Registries.getId(player.level.getBlockState(((BlockHitResult) result).getBlockPos()).getBlock(), Registry.BLOCK_REGISTRY)));
			} else if (type == 2) {
				BlockEntity blockEntity = player.level.getBlockEntity(((BlockHitResult) result).getBlockPos());
				return blockEntity != null && observeId.equals(String.valueOf(Registries.getId(blockEntity.getType(), Registry.BLOCK_ENTITY_TYPE_REGISTRY)));
			} else if (type == 3) {
				BlockEntity blockEntity = player.level.getBlockEntity(((BlockHitResult) result).getBlockPos());
				return blockEntity != null && observeId.equals(blockEntity.getClass().getName());
			}
		}

		return false;
	}
}