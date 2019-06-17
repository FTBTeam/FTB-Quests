package com.feed_the_beast.ftbquests.integration.gamestages;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class GameStageReward extends QuestReward
{
	public String stage = "";
	public boolean silent = false;

	public GameStageReward(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestRewardType getType()
	{
		return FTBQuestsRewards.GAMESTAGE;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setString("stage", stage);

		if (silent)
		{
			nbt.setBoolean("silent", silent);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		stage = nbt.getString("stage");
		silent = nbt.getBoolean("silent");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeString(stage);
		data.writeBoolean(silent);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		stage = data.readString();
		silent = data.readBoolean();
	}

	@Override
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.addString("stage", () -> stage, v -> stage = v, "").setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.gamestage"));
		config.addBool("silent", () -> silent, v -> silent = v, false);
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		GameStageHelper.addStage(player, stage);

		if (!silent)
		{
			player.sendMessage(new TextComponentTranslation("commands.gamestage.add.target", stage));
		}
	}

	@Override
	public String getAltTitle()
	{
		return I18n.format("ftbquests.reward.ftbquests.gamestage") + ": " + TextFormatting.YELLOW + stage;
	}
}