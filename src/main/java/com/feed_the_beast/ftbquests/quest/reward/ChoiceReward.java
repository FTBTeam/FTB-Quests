package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftbquests.gui.GuiSelectChoiceReward;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ChoiceReward extends RandomReward
{
	public ChoiceReward(Quest quest)
	{
		super(quest);
	}

	@Override
	public RewardType getType()
	{
		return FTBQuestsRewards.CHOICE;
	}

	@Override
	public void claim(EntityPlayerMP player, boolean notify)
	{
	}

	@Override
	public boolean automatedClaimPre(TileEntity tileEntity, List<ItemStack> items, Random random, UUID playerId, @Nullable EntityPlayerMP player)
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list)
	{
		if (getTable() != null)
		{
			getTable().addMouseOverText(list, false, false);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onButtonClicked(boolean canClick)
	{
		if (canClick)
		{
			GuiHelper.playClickSound();
			new GuiSelectChoiceReward(this).openGui();
		}
	}

	@Override
	public boolean getExcludeFromClaimAll()
	{
		return true;
	}
}