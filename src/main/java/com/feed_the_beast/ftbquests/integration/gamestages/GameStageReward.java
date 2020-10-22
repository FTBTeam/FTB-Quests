package com.feed_the_beast.ftbquests.integration.gamestages;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardAutoClaim;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public class GameStageReward extends Reward
{
	public String stage = "";
	public boolean remove = false;

	public GameStageReward(Quest quest)
	{
		super(quest);
		autoclaim = RewardAutoClaim.INVISIBLE;
	}

	@Override
	public RewardType getType()
	{
		return GameStagesIntegration.GAMESTAGE_REWARD;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putString("stage", stage);

		if (remove)
		{
			nbt.putBoolean("remove", true);
		}
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		stage = nbt.getString("stage");
		remove = nbt.getBoolean("remove");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeString(stage);
		buffer.writeBoolean(remove);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		stage = buffer.readString();
		remove = buffer.readBoolean();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("stage", stage, v -> stage = v, "").setNameKey("ftbquests.reward.ftbquests.gamestage");
		config.addBool("remove", remove, v -> remove = v, false);
	}

	@Override
	public void claim(ServerPlayerEntity player, boolean notify)
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

		if (notify)
		{
			if (remove)
			{
				player.sendMessage(new TranslationTextComponent("commands.gamestage.remove.target", stage), Util.DUMMY_UUID);
			}
			else
			{
				player.sendMessage(new TranslationTextComponent("commands.gamestage.add.target", stage), Util.DUMMY_UUID);
			}
		}
	}

	@Override
	public IFormattableTextComponent getAltTitle()
	{
		return new TranslationTextComponent("ftbquests.reward.ftbquests.gamestage").appendString(": ").append(new StringTextComponent(stage).mergeStyle(TextFormatting.YELLOW));
	}
}