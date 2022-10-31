package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftbquests.client.EnergyTaskClientData;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

/**
 * @author LatvianModder
 */
public abstract class EnergyTask extends Task implements ISingleLongValueTask {
	public long value = 1000L;
	public long maxInput = 1000L;

	public EnergyTask(Quest quest) {
		super(quest);
	}

	@Override
	public long getMaxProgress() {
		return value;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putLong("value", value);

		if (maxInput > 0L) {
			nbt.putLong("max_input", maxInput);
		}
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		value = nbt.getLong("value");

		if (value < 1L) {
			value = 1L;
		}

		maxInput = nbt.getLong("max_input");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeVarLong(value);
		buffer.writeVarLong(maxInput);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		value = buffer.readVarLong();
		maxInput = buffer.readVarLong();
	}

	@Override
	public void setValue(long v) {
		value = v;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return new TextComponent(StringUtils.formatDouble(value, true));
	}

	@Override
	public boolean consumesResources() {
		return true;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addLong("value", value, v -> value = v, 1000L, 1L, Long.MAX_VALUE);
		config.addLong("max_input", maxInput, v -> maxInput = v, 1000L, 0L, Integer.MAX_VALUE).setNameKey("ftbquests.task.max_input");
	}

	public abstract EnergyTaskClientData getClientData();
}