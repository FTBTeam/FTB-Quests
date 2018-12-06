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
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author LatvianModder
 */
public class MoneyReward extends QuestReward
{
	public long value = 1L;
	public int randomBonus = 0;

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

		if (randomBonus > 0)
		{
			nbt.setInteger("random_bonus", randomBonus);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		value = nbt.getLong("ftb_money");
		randomBonus = nbt.getInteger("random_bonus");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarLong(value);
		data.writeVarInt(randomBonus);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		value = data.readVarLong();
		randomBonus = data.readVarInt();
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addLong("value", () -> value, v -> value = v, 1L, 1L, Long.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.ftb_money"));
		config.addInt("random_bonus", () -> randomBonus, v -> randomBonus = v, 0, 0, Integer.MAX_VALUE);
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		long added = value + player.world.rand.nextInt(randomBonus + 1);
		FTBMoney.setMoney(player, FTBMoney.getMoney(player) + added);
		player.sendStatusMessage(FTBMoney.moneyComponent(added), true);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		if (randomBonus > 0)
		{
			return FTBMoney.moneyComponent(value).appendText(" - ").appendSibling(FTBMoney.moneyComponent(value + randomBonus));
		}

		return FTBMoney.moneyComponent(value);
	}
}