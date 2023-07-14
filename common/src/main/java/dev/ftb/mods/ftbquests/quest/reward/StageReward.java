package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
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
public class StageReward extends Reward {
	public String stage = "";
	public boolean remove = false;

	public StageReward(Quest quest) {
		super(quest);
		autoclaim = RewardAutoClaim.INVISIBLE;
	}

	@Override
	public RewardType getType() {
		return RewardTypes.STAGE;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("stage", stage);

		if (remove) {
			nbt.putBoolean("remove", true);
		}
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		stage = nbt.getString("stage");
		remove = nbt.getBoolean("remove");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(stage, Short.MAX_VALUE);
		buffer.writeBoolean(remove);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		stage = buffer.readUtf(Short.MAX_VALUE);
		remove = buffer.readBoolean();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addString("stage", stage, v -> stage = v, "").setNameKey("ftbquests.reward.ftbquests.gamestage");
		config.addBool("remove", remove, v -> remove = v, false);
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		if (remove) {
			StageHelper.getInstance().getProvider().remove(player, stage);
		} else {
			StageHelper.getInstance().getProvider().add(player, stage);
		}

		if (notify) {
			if (remove) {
				player.sendSystemMessage(Component.translatable("commands.gamestage.remove.target", stage), true);
			} else {
				player.sendSystemMessage(Component.translatable("commands.gamestage.add.target", stage), true);
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.reward.ftbquests.gamestage").append(": ").append(Component.literal(stage).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public boolean ignoreRewardBlocking() {
		return true;
	}

	@Override
	protected boolean isIgnoreRewardBlockingHardcoded() {
		return true;
	}
}
