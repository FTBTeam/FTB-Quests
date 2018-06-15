package com.feed_the_beast.ftbquests.handlers;

import com.feed_the_beast.ftblib.events.FTBLibPreInitRegistryEvent;
import com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedInEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamDataEvent;
import com.feed_the_beast.ftblib.lib.data.AdminPanelAction;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsConfig;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
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
		registry.registerServerReloadHandler(new ResourceLocation(FTBQuests.MOD_ID, "quests"), ServerQuestList::reload);

		registry.registerAdminPanelAction(new AdminPanelAction(FTBQuests.MOD_ID, "edit_quests", GuiIcons.BOOK_RED, 0)
		{
			@Override
			public Type getType(ForgePlayer player, NBTTagCompound data)
			{
				return Type.INVISIBLE;
			}

			@Override
			public void onAction(ForgePlayer player, NBTTagCompound data)
			{
			}
		}.setTitle(new TextComponentTranslation("ftbquests.general.editing_mode.button")));
	}

	@SubscribeEvent
	public static void playerLoggedIn(ForgePlayerLoggedInEvent event)
	{
		if (ServerQuestList.INSTANCE != null)
		{
			ServerQuestList.INSTANCE.sendTo(event.getPlayer().getPlayer());
		}
	}

	@SubscribeEvent
	public static void registerTeamData(ForgeTeamDataEvent event)
	{
		event.register(FTBQuests.MOD_ID, new FTBQuestsTeamData(event.getTeam()));
	}

	/* TODO: Display quest book notification
	@SubscribeEvent
	public static void onPlayerLoggedIn(ForgePlayerLoggedInEvent event)
	{
		if (event.getPlayer().isFake())
		{
			return;
		}

		EntityPlayerMP player = event.getPlayer().getPlayer();

		if (event.isFirstLogin())
		{
		}
	}*/

	/*
	@SubscribeEvent
	public static void getTeamSettings(ForgeTeamConfigEvent event)
	{
		FTBGuidesTeamData.get(event.getTeam()).addConfig(event);
	}*/
}