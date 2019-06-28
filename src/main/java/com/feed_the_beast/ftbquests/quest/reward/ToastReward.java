package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("description", () -> description, v -> description = v, "");
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
	}
}