package dev.ftb.mods.ftbquests.quest.reward;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.net.CustomToastMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

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
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		nbt.putString("description", description);
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		description = nbt.getString("description").orElseThrow();
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
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addString("description", description, v -> description = v, "");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		NetworkManager.sendToPlayer(player, new CustomToastMessage(getId()));
	}
}
