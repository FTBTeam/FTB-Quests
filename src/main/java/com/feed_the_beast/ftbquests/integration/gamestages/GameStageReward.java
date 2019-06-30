package com.feed_the_beast.ftbquests.integration.gamestages;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class GameStageReward extends Reward
{
	public String stage = "";
	public boolean remove = false;
	public boolean silent = false;

	public GameStageReward(Quest quest)
	{
		super(quest);
	}

	@Override
	public RewardType getType()
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
			nbt.setBoolean("silent", true);
		}

		if (remove)
		{
			nbt.setBoolean("remove", true);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		stage = nbt.getString("stage");
		silent = nbt.getBoolean("silent");
		remove = nbt.getBoolean("remove");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeString(stage);
		data.writeBoolean(silent);
		data.writeBoolean(remove);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		stage = data.readString();
		silent = data.readBoolean();
		remove = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("stage", () -> stage, v -> stage = v, "").setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.gamestage"));
		config.addBool("silent", () -> silent, v -> silent = v, false);
		config.addBool("remove", () -> remove, v -> remove = v, false);
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		if (remove)
		{
			GameStageHelper.removeStage(player, stage);
		}
		else
		{
			GameStageHelper.addStage(player, stage);
		}

		GameStageHelper.syncPlayer(player);

		if (!silent)
		{
			if (remove)
			{
				player.sendMessage(new TextComponentTranslation("commands.gamestage.remove.target", stage));
			}
			else
			{
				player.sendMessage(new TextComponentTranslation("commands.gamestage.add.target", stage));
			}
		}
	}

	@Override
	public String getAltTitle()
	{
		return I18n.format("ftbquests.reward.ftbquests.gamestage") + ": " + TextFormatting.YELLOW + stage;
	}
}