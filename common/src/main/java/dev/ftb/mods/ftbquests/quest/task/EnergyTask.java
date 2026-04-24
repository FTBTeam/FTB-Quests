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
import org.jetbrains.annotations.UnknownNullability;

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
	public void writeData(Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);

		json.addProperty("value", value);
		if (maxInput > 0L) json.addProperty("max_input", maxInput);
	}

	@Override
	public void readData(Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);

		value = Math.max(1L, Json5Util.getLong(json, "value").orElseThrow());
		maxInput = Json5Util.getLong(json, "max_input").orElse(1000L);
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
