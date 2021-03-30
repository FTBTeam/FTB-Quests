package dev.ftb.mods.ftbquests.quest.reward;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class ToastReward extends Reward {
	public String description;

	public ToastReward(Quest quest) {
		super(quest);
		description = "";
	}

	@Override
	public RewardType getType() {
		return RewardTypes.TOAST;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("description", description);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		description = nbt.getString("description");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(description, Short.MAX_VALUE);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		description = buffer.readUtf(Short.MAX_VALUE);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addString("description", description, v -> description = v, "");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
	}
}