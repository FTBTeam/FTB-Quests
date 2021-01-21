package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * @author LatvianModder
 */
public class ToastReward extends Reward
{
	public String description;

	public ToastReward(Quest quest)
	{
		super(quest);
		description = "";
	}

	@Override
	public RewardType getType()
	{
		return FTBQuestsRewards.TOAST;
	}

	@Override
	public void writeData(CompoundTag nbt)
	{
		super.writeData(nbt);
		nbt.putString("description", description);
	}

	@Override
	public void readData(CompoundTag nbt)
	{
		super.readData(nbt);
		description = nbt.getString("description");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer)
	{
		super.writeNetData(buffer);
		buffer.writeUtf(description);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer)
	{
		super.readNetData(buffer);
		description = buffer.readUtf();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("description", description, v -> description = v, "");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify)
	{
	}
}