package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.net.MessageDisplayRewardToast;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class XPLevelsReward extends Reward
{
	public int xpLevels;

	public XPLevelsReward(Quest quest)
	{
		super(quest);
		xpLevels = 1;
	}

	@Override
	public RewardType getType()
	{
		return FTBQuestsRewards.XP_LEVELS;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setInteger("xp_levels", xpLevels);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		xpLevels = nbt.getInteger("xp_levels");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(xpLevels);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		xpLevels = data.readVarInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("xp_levels", () -> xpLevels, v -> xpLevels = v, 1, 1, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.xp_levels"));
	}

	@Override
	public void claim(EntityPlayerMP player, boolean notify)
	{
		player.addExperienceLevel(xpLevels);

		if (notify)
		{
			ITextComponent text = new TextComponentString("+" + xpLevels);
			text.getStyle().setColor(TextFormatting.GREEN);
			new MessageDisplayRewardToast(new TextComponentTranslation("ftbquests.reward.ftbquests.xp_levels").appendText(": ").appendSibling(text), getIcon()).sendTo(player);
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