package dev.ftb.mods.ftbquests.quest.task;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftbquests.client.EnergyTaskClientData;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class EnergyTask extends Task implements ISingleLongValueTask {
	public static final long DEFAULT_VALUE = 1000L;
	public static final long DEFAULT_MAX_INPUT = 1000L;

	private long value = DEFAULT_VALUE;
	private long maxInput = DEFAULT_MAX_INPUT;

	public EnergyTask(long id, Quest quest) {
		super(id, quest);
	}

	@Override
	public long getMaxProgress() {
		return value;
	}

	@Override
	public void writeData(Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);

		if (value != DEFAULT_VALUE) json.addProperty("value", value);
		if (maxInput != DEFAULT_MAX_INPUT) json.addProperty("max_input", maxInput);
	}

	@Override
	public void readData(Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);

		value = Math.max(Json5Util.getLong(json, "value").orElse(DEFAULT_VALUE), 1L);
		maxInput = Math.max(Json5Util.getLong(json, "max_input").orElse(DEFAULT_MAX_INPUT), 0L);
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
		value = Math.max(buffer.readVarLong(), 1L);
		maxInput = Math.max(buffer.readVarLong(), 0L);
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
		config.addLong("value", value, v -> value = v, DEFAULT_VALUE, 1L, Long.MAX_VALUE);
		config.addLong("max_input", maxInput, v -> maxInput = v, DEFAULT_MAX_INPUT, 0L, Integer.MAX_VALUE)
				.setNameKey("ftbquests.task.max_input");
	}

	public abstract EnergyTaskClientData getClientData();

	public long getMaxInput() {
		return maxInput;
	}
}
