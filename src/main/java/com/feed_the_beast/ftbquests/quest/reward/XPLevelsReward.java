package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.net.MessageDisplayRewardToast;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class XPLevelsReward extends QuestReward
{
	public int xpLevels;

	public XPLevelsReward(QuestObjectBase parent)
	{
		super(parent);
		xpLevels = 1;
	}

	@Override
	public QuestRewardType getType()
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
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("xp_levels", () -> xpLevels, v -> xpLevels = v, 1, 1, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.xp_levels"));
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		player.addExperienceLevel(xpLevels);

		if (MessageDisplayRewardToast.ENABLED)
		{
			new MessageDisplayRewardToast(getAltDisplayName(), getIcon()).sendTo(player);
		}
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		ITextComponent text = new TextComponentString("+" + xpLevels);
		text.getStyle().setColor(TextFormatting.GREEN);
		return new TextComponentTranslation("ftbquests.reward.ftbquests.xp_levels").appendText(": ").appendSibling(text);
	}
}