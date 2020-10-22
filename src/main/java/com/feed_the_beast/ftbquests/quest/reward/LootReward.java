package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.gui.GuiRewardNotifications;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
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
public class LootReward extends RandomReward
{
	public LootReward(Quest quest)
	{
		super(quest);
	}

	@Override
	public RewardType getType()
	{
		return FTBQuestsRewards.LOOT;
	}

	@Override
	public void claim(ServerPlayerEntity player, boolean notify)
	{
		if (getTable() == null)
		{
			return;
		}

		int totalWeight = getTable().getTotalWeight(true);

		if (totalWeight <= 0)
		{
			return;
		}

		for (int i = 0; i < getTable().lootSize; i++)
		{
			int number = player.world.rand.nextInt(totalWeight) + 1;
			int currentWeight = getTable().emptyWeight;

			if (currentWeight < number)
			{
				for (WeightedReward reward : getTable().rewards)
				{
					currentWeight += reward.weight;

					if (currentWeight >= number)
					{
						reward.reward.claim(player, notify);
						break;
					}
				}
			}
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addMouseOverText(TooltipList list)
	{
		if (getTable() != null)
		{
			getTable().addMouseOverText(list, true, true);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void onButtonClicked(Button button, boolean canClick)
	{
		if (canClick)
		{
			new GuiRewardNotifications().openGui();
		}

		super.onButtonClicked(button, canClick);
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

	@Override
	public void automatedClaimPost(TileEntity tileEntity, UUID playerId, @Nullable ServerPlayerEntity player)
	{
	}
}