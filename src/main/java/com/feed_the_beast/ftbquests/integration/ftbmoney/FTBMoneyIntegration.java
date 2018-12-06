package com.feed_the_beast.ftbquests.integration.ftbmoney;

import com.feed_the_beast.ftblib.lib.config.ConfigLong;
import com.feed_the_beast.ftblib.lib.gui.IOpenableGui;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

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

		FTBQuestsRewards.FTB_MONEY.setGuiProvider(new QuestRewardType.GuiProvider()
		{
			@Override
			@SideOnly(Side.CLIENT)
			public void openCreationGui(IOpenableGui gui, Quest quest, Consumer<QuestReward> callback)
			{
				new GuiEditConfigValue("ftb_money", new ConfigLong(1, 1, Long.MAX_VALUE), (value, set) -> {
					gui.openGui();
					if (set)
					{
						MoneyReward reward = new MoneyReward(quest);
						reward.value = value.getLong();
						callback.accept(reward);
					}
				}).openGui();
			}
		});
	}
}