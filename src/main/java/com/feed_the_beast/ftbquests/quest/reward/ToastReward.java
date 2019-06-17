package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class ToastReward extends QuestReward
{
	public String description;

	public ToastReward(Quest quest)
	{
		super(quest);
		description = "";
	}

	@Override
	public QuestRewardType getType()
	{
		return FTBQuestsRewards.TOAST;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);

		if (!description.isEmpty())
		{
			nbt.setString("description", description);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		description = nbt.getString("description");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeString(description);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		description = data.readString();
	}

	@Override
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.addString("description", () -> description, v -> description = v, "");
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
	}
}