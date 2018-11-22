package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class XPReward extends QuestReward
{
	public int xp = 100;

	public XPReward(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestRewardType getType()
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
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("xp", () -> xp, v -> xp = v, 100, 1, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.xp"));
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		player.addExperience(xp);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		ITextComponent text = new TextComponentString("+" + xp);
		text.getStyle().setColor(TextFormatting.GREEN);
		return new TextComponentTranslation("ftbquests.reward.ftbquests.xp.text", text);
	}
}