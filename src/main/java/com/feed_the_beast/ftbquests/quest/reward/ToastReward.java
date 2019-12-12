package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putString("description", description);
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		description = nbt.getString("description");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeString(description);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		description = buffer.readString();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("description", description, v -> description = v, "");
	}

	@Override
	public void claim(ServerPlayerEntity player, boolean notify)
	{
	}
}