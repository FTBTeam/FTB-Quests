package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.net.MessageDisplayRewardToast;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class XPLevelsReward extends Reward
{
	public int xpLevels;

	public XPLevelsReward(Quest quest, int x)
	{
		super(quest);
		xpLevels = x;
	}

	public XPLevelsReward(Quest quest)
	{
		this(quest, 5);
	}

	@Override
	public RewardType getType()
	{
		return RewardTypes.XP_LEVELS;
	}

	@Override
	public void writeData(CompoundTag nbt)
	{
		super.writeData(nbt);
		nbt.putInt("xp_levels", xpLevels);
	}

	@Override
	public void readData(CompoundTag nbt)
	{
		super.readData(nbt);
		xpLevels = nbt.getInt("xp_levels");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer)
	{
		super.writeNetData(buffer);
		buffer.writeVarInt(xpLevels);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer)
	{
		super.readNetData(buffer);
		xpLevels = buffer.readVarInt();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("xp_levels", xpLevels, v -> xpLevels = v, 1, 1, Integer.MAX_VALUE).setNameKey("ftbquests.reward.ftbquests.xp_levels");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify)
	{
		player.giveExperienceLevels(xpLevels);

		if (notify)
		{
			new MessageDisplayRewardToast(id, new TranslatableComponent("ftbquests.reward.ftbquests.xp_levels").append(": ").append(new TextComponent("+" + xpLevels).withStyle(ChatFormatting.GREEN)), Icon.EMPTY).sendTo(player);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle()
	{
		return new TranslatableComponent("ftbquests.reward.ftbquests.xp_levels").append(": ").append(new TextComponent("+" + xpLevels).withStyle(ChatFormatting.GREEN));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public String getButtonText()
	{
		return "+" + xpLevels;
	}
}