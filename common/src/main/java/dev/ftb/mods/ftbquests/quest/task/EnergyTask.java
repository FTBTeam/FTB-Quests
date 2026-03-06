package dev.ftb.mods.ftbquests.quest.task;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftbquests.client.EnergyTaskClientData;
import dev.ftb.mods.ftbquests.quest.Quest;

public abstract class EnergyTask extends Task implements ISingleLongValueTask {
	private long value = 1000L;
	private long maxInput = 1000L;

	public EnergyTask(long id, Quest quest) {
		super(id, quest);
	}

	@Override
	public long getMaxProgress() {
		return value;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		nbt.putLong("value", value);

		if (maxInput > 0L) {
			nbt.putLong("max_input", maxInput);
		}
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		value = nbt.getLong("value").orElseThrow();

		if (value < 1L) {
			value = 1L;
		}

		maxInput = nbt.getLongOr("max_input", 1000L);
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeVarLong(value);
		buffer.writeVarLong(maxInput);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		value = buffer.readVarLong();
		maxInput = buffer.readVarLong();
	}

	public long getValue() {
		return value;
	}

	@Override
	public void setValue(long v) {
		value = v;
	}

	@Override
	public MutableComponent getAltTitle() {
		return Component.literal(StringUtils.formatDouble(value, true));
	}

	@Override
	public boolean consumesResources() {
		return true;
	}

	@Override
	public void fillConfigGroup(EditableConfigGroup config) {
		super.fillConfigGroup(config);
		config.addLong("value", value, v -> value = v, 1000L, 1L, Long.MAX_VALUE);
		config.addLong("max_input", maxInput, v -> maxInput = v, 1000L, 0L, Integer.MAX_VALUE).setNameKey("ftbquests.task.max_input");
	}

	public abstract EnergyTaskClientData getClientData();

	public long getMaxInput() {
		return maxInput;
	}
}
