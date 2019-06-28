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
public class XPReward extends Reward
{
	public int xp;

	public XPReward(Quest quest)
	{
		super(quest);
		xp = 100;
	}

	@Override
	public RewardType getType()
	{
		return FTBQuestsRewards.XP;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setInteger("xp", xp);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		xp = nbt.getInteger("xp");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(xp);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		xp = data.readVarInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("xp", () -> xp, v -> xp = v, 100, 1, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.xp"));
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		player.addExperience(xp);

		if (MessageDisplayRewardToast.ENABLED)
		{
			ITextComponent text = new TextComponentString("+" + xp);
			text.getStyle().setColor(TextFormatting.GREEN);
			new MessageDisplayRewardToast(new TextComponentTranslation("ftbquests.reward.ftbquests.xp").appendText(": ").appendSibling(text), getIcon()).sendTo(player);
		}
	}

	@Override
	public String getAltTitle()
	{
		return I18n.format("ftbquests.reward.ftbquests.xp") + ": " + TextFormatting.GREEN + "+" + xp;
	}

	@Override
	public String getButtonText()
	{
		return "+" + xp;
	}
}