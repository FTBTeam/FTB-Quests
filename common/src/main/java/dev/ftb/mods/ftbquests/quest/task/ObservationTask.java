package dev.ftb.mods.ftbquests.quest.task;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import dev.ftb.mods.ftbquests.quest.PlayerData;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;

/**
 * @author LatvianModder
 */
public class ObservationTask extends Task {
	@FunctionalInterface
	public interface Check {
		boolean check(Player player, HitResult lookingAt);
	}

	public final Check matcher;
	public long ticks;

	public ObservationTask(Quest quest) {
		super(quest);
		matcher = (player, ray) -> false;
		ticks = 0L;
	}

	@Override
	public TaskType getType() {
		return TaskTypes.OBSERVATION;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putLong("ticks", ticks);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		ticks = nbt.getLong("ticks");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeVarLong(ticks);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		ticks = buffer.readVarLong();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addLong("ticks", ticks, v -> ticks = v, 0L, 0L, 1200L);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
	}

	@Override
	public TaskData createData(PlayerData data) {
		return new BooleanTaskData<>(this, data);
	}
}