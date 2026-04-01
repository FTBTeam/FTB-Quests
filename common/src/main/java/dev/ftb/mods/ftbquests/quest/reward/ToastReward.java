package dev.ftb.mods.ftbquests.quest.reward;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.net.CustomToastMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.UnknownNullability;

public class ToastReward extends Reward {
	private String description;

	public ToastReward(long id, Quest quest) {
		super(id, quest);
		description = "";
	}

	public String getDescription() {
		return description;
	}

	@Override
	public RewardType getType() {
		return RewardTypes.TOAST;
	}

	@Override
	public void writeData(@UnknownNullability Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);
		json.addProperty("description", description);
	}

	@Override
	public void readData(@UnknownNullability Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);
		description = Json5Util.getString(json, "description").orElseThrow();
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(description, Short.MAX_VALUE);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		description = buffer.readUtf(Short.MAX_VALUE);
	}

	@Override
	public void fillConfigGroup(EditableConfigGroup config) {
		super.fillConfigGroup(config);
		config.addString("description", description, v -> description = v, "");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		Server2PlayNetworking.send(player, new CustomToastMessage(getId()));
	}
}
