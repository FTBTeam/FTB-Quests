package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.gui.GuiSelectChoiceReward;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
	public void claim(ServerPlayerEntity player, boolean notify)
	{
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addMouseOverText(TooltipList list)
	{
		if (getTable() != null)
		{
			getTable().addMouseOverText(list, false, false);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void onButtonClicked(Button button, boolean canClick)
	{
		if (canClick)
		{
			button.playClickSound();
			new GuiSelectChoiceReward(this).openGui();
		}
	}

	@Override
	public boolean getExcludeFromClaimAll()
	{
		return true;
	}

	@Override
	public boolean automatedClaimPre(TileEntity tileEntity, List<ItemStack> items, Random random, UUID playerId, @Nullable ServerPlayerEntity player)
	{
		return false;
	}
}