package com.feed_the_beast.ftbquests.integration.ftbmoney;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
public class FTBMoneyIntegration
{
	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(FTBMoneyIntegration.class);
	}

	@SubscribeEvent
	public static void registerTasks(RegistryEvent.Register<QuestTaskType> event)
	{
		event.getRegistry().register(FTBQuestsTasks.FTB_MONEY = new QuestTaskType(MoneyTask::new).setRegistryName("ftb_money").setIcon(Icon.getIcon("ftbmoney:textures/beastcoin.png")));
	}

	@SubscribeEvent
	public static void registerRewards(RegistryEvent.Register<QuestRewardType> event)
	{
		event.getRegistry().register(FTBQuestsRewards.FTB_MONEY = new QuestRewardType(MoneyReward::new).setRegistryName("ftb_money").setIcon(Icon.getIcon("ftbmoney:textures/beastcoin.png")));
	}
}