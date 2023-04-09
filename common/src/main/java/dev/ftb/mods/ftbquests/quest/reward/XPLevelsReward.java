package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.net.DisplayRewardToastMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class XPLevelsReward extends Reward {
	public int xpLevels;

	public XPLevelsReward(Quest quest, int x) {
		super(quest);
		xpLevels = x;
	}

	public XPLevelsReward(Quest quest) {
		this(quest, 5);
	}

	@Override
	public RewardType getType() {
		return RewardTypes.XP_LEVELS;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putInt("xp_levels", xpLevels);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		xpLevels = nbt.getInt("xp_levels");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeVarInt(xpLevels);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		xpLevels = buffer.readVarInt();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addInt("xp_levels", xpLevels, v -> xpLevels = v, 1, 1, Integer.MAX_VALUE).setNameKey("ftbquests.reward.ftbquests.xp_levels");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		player.giveExperienceLevels(xpLevels);

		if (notify) {
			new DisplayRewardToastMessage(id, Component.translatable("ftbquests.reward.ftbquests.xp_levels").append(": ").append(Component.literal("+" + xpLevels).withStyle(ChatFormatting.GREEN)), Color4I.EMPTY).sendTo(player);
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
