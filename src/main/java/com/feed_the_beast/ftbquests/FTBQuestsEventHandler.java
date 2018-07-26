package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftblib.events.FTBLibPreInitRegistryEvent;
import com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedInEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamCreatedEvent;
import com.feed_the_beast.ftblib.events.universe.UniverseLoadedEvent;
import com.feed_the_beast.ftblib.lib.data.AdminPanelAction;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftbquests.net.MessageResetProgress;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID)
public class FTBQuestsEventHandler
{
	@SubscribeEvent
	public static void onFTBLibPreInitRegistry(FTBLibPreInitRegistryEvent event)
	{
		FTBLibPreInitRegistryEvent.Registry registry = event.getRegistry();
		registry.registerServerReloadHandler(new ResourceLocation(FTBQuests.MOD_ID, "config"), reloadEvent -> FTBQuestsConfig.sync());

		registry.registerAdminPanelAction(new AdminPanelAction(FTBQuests.MOD_ID, "reset_progress", GuiIcons.BOOK_RED.combineWith(GuiIcons.CLOSE), 0)
		{
			@Override
			public Type getType(ForgePlayer player, NBTTagCompound data)
			{
				return FTBQuestsConfig.general.editing_mode ? Type.fromBoolean(player.hasPermission(FTBQuests.PERM_RESET_PROGRESS)) : Type.INVISIBLE;
			}

			@Override
			public void onAction(ForgePlayer player, NBTTagCompound data0)
			{
				ServerQuestList.INSTANCE.shouldSendUpdates = false;
				for (ForgeTeam team : player.team.universe.getTeams())
				{
					FTBQuestsTeamData.get(team).reset();
				}

				player.team.universe.clearCache();
				new MessageResetProgress(player.team.getName()).sendTo(player.getPlayer());
				ServerQuestList.INSTANCE.shouldSendUpdates = true;
			}
		});
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(ForgePlayerLoggedInEvent event)
	{
		ServerQuestList.INSTANCE.sync(event.getPlayer().getPlayer());
	}

	@SubscribeEvent
	public static void onUniverseLoaded(UniverseLoadedEvent.Pre event)
	{
		if (!ServerQuestList.load())
		{
			FTBQuests.LOGGER.error("Failed to load quests!");
		}
	}

	@SubscribeEvent
	public static void onTeamCreated(ForgeTeamCreatedEvent event)
	{
		FTBQuestsTeamData data = FTBQuestsTeamData.get(event.getTeam());

		for (QuestChapter chapter : ServerQuestList.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				for (QuestTask task : quest.tasks)
				{
					data.createTaskData(task);
				}
			}
		}
	}
}