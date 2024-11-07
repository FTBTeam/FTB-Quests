package dev.ftb.mods.ftbquests.quest.reward;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.net.DisplayRewardToastMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class XPLevelsReward extends Reward {
	private int xpLevels;

	public XPLevelsReward(long id, Quest quest, int x) {
		super(id, quest);
		xpLevels = x;
	}

	public XPLevelsReward(long id, Quest quest) {
		this(id, quest, 5);
	}

	@Override
	public RewardType getType() {
		return RewardTypes.XP_LEVELS;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		nbt.putInt("xp_levels", xpLevels);
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		xpLevels = nbt.getInt("xp_levels");
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeVarInt(xpLevels);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		xpLevels = buffer.readVarInt();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addInt("xp_levels", xpLevels, v -> xpLevels = v, 1, 1, Integer.MAX_VALUE).setNameKey("ftbquests.reward.ftbquests.xp_levels");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		player.giveExperienceLevels(xpLevels);

		if (notify) {
			Component text = Component.translatable("ftbquests.reward.ftbquests.xp_levels").append(": ")
					.append(Component.literal("+" + xpLevels).withStyle(ChatFormatting.GREEN));
			NetworkManager.sendToPlayer(player, new DisplayRewardToastMessage(id, text, Color4I.empty(), disableRewardScreenBlur));
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.reward.ftbquests.xp_levels").append(": ").append(Component.literal("+" + xpLevels).withStyle(ChatFormatting.GREEN));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public String getButtonText() {
		return "+" + xpLevels;
	}
}
