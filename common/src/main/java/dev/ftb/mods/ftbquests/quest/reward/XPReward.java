package dev.ftb.mods.ftbquests.quest.reward;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.net.NotifyRewardMessage;
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

public class XPReward extends Reward {
	private int xp;

	public XPReward(long id, Quest quest, int xp) {
		super(id, quest);
		this.xp = xp;
	}

	public XPReward(long id, Quest quest) {
		this(id, quest, 100);
	}

	@Override
	public RewardType getType() {
		return RewardTypes.XP;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		nbt.putInt("xp", xp);
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		xp = nbt.getInt("xp");
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeVarInt(xp);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		xp = buffer.readVarInt();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addInt("xp", xp, v -> xp = v, 100, 1, Integer.MAX_VALUE).setNameKey("ftbquests.reward.ftbquests.xp");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		player.giveExperiencePoints(xp);

		if (notify) {
			Component text = Component.translatable("ftbquests.reward.ftbquests.xp").append(": ")
					.append(Component.literal("+" + xp).withStyle(ChatFormatting.GREEN));
			NetworkManager.sendToPlayer(player, new NotifyRewardMessage(id, text, Color4I.empty(), disableRewardScreenBlur));
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.reward.ftbquests.xp").append(": ").append(Component.literal("+" + xp).withStyle(ChatFormatting.GREEN));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public String getButtonText() {
		return "+" + xp;
	}
}
