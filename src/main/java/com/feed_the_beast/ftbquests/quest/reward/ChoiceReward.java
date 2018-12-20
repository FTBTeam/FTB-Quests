package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.gui.GuiSelectChoiceReward;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

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
	public QuestRewardType getType()
	{
		return FTBQuestsRewards.CHOICE;
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list)
	{
		getTable().addMouseOverText(list, false, false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onButtonClicked()
	{
		new GuiSelectChoiceReward(this).openGui();
	}

	@Override
	public boolean getExcludeFromClaimAll()
	{
		return true;
	}
}