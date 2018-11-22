package com.feed_the_beast.ftbquests.integration.ftbmoney;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import com.feed_the_beast.mods.money.FTBMoney;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;

/**
 * @author LatvianModder
 */
public class MoneyReward extends QuestReward
{
	public long value = 1L;

	public MoneyReward(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestRewardType getType()
	{
		return FTBQuestsRewards.FTB_MONEY;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setLong("ftb_money", value);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		value = nbt.getLong("ftb_money");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarLong(value);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		value = data.readVarLong();
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addLong("value", () -> value, v -> value = v, 1, 1, Long.MAX_VALUE);
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		FTBMoney.setMoney(player, FTBMoney.getMoney(player) + value);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return FTBMoney.moneyComponent(value);
	}
}