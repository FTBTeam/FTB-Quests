package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.net.MessageDisplayRewardToast;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		return FTBQuestsRewards.XP_LEVELS;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putInt("xp_levels", xpLevels);
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		xpLevels = nbt.getInt("xp_levels");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeVarInt(xpLevels);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		xpLevels = buffer.readVarInt();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("xp_levels", xpLevels, v -> xpLevels = v, 1, 1, Integer.MAX_VALUE).setNameKey("ftbquests.reward.ftbquests.xp_levels");
	}

	@Override
	public void claim(ServerPlayerEntity player, boolean notify)
	{
		player.addExperienceLevel(xpLevels);

		if (notify)
		{
			ITextComponent text = new StringTextComponent("+" + xpLevels);
			text.getStyle().setColor(TextFormatting.GREEN);
			new MessageDisplayRewardToast(id, new TranslationTextComponent("ftbquests.reward.ftbquests.xp_levels").appendText(": ").appendSibling(text), Icon.EMPTY).sendTo(player);
		}
	}

	@Override
	public String getAltTitle()
	{
		return I18n.format("ftbquests.reward.ftbquests.xp_levels") + ": " + TextFormatting.GREEN + "+" + xpLevels;
	}

	@Override
	public String getButtonText()
	{
		return "+" + xpLevels;
	}
}