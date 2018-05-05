package com.feed_the_beast.ftbquests.handlers;

import com.feed_the_beast.ftblib.events.RegisterAdminPanelActionsEvent;
import com.feed_the_beast.ftblib.events.RegisterOptionalServerModsEvent;
import com.feed_the_beast.ftblib.events.ServerReloadEvent;
import com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedInEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamDataEvent;
import com.feed_the_beast.ftblib.lib.EventHandler;
import com.feed_the_beast.ftblib.lib.data.Action;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.util.CommonUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsConfig;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
@EventHandler
public class FTBQuestsEventHandler
{
	public static final ResourceLocation RELOAD_CONFIG = new ResourceLocation(FTBQuests.MOD_ID, "config");
	public static final ResourceLocation RELOAD_QUESTS = new ResourceLocation(FTBQuests.MOD_ID, "quests");

	@SubscribeEvent
	public static void registerReloadIds(ServerReloadEvent.RegisterIds event)
	{
		event.register(RELOAD_CONFIG);
		event.register(RELOAD_QUESTS);
	}

	@SubscribeEvent
	public static void onServerReload(ServerReloadEvent event)
	{
		if (event.reload(RELOAD_CONFIG))
		{
			FTBQuestsConfig.sync();
		}

		if (event.reload(RELOAD_QUESTS))
		{
			ServerQuestList.INSTANCE = null;

			List<String> errored = new ArrayList<>();
			ServerQuestList.INSTANCE = new ServerQuestList(new File(CommonUtils.folderConfig, "ftbquests/quests"), errored);

			for (String s : errored)
			{
				event.failedToReload(new ResourceLocation(FTBQuests.MOD_ID, "quests/" + s));
			}

			ServerQuestList.INSTANCE.sendToAll();
		}
	}

	@SubscribeEvent
	public static void registerOptionalServerMod(RegisterOptionalServerModsEvent event)
	{
		event.register(FTBQuests.MOD_ID);
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

	@SubscribeEvent
	public static void registerAdminPanelActions(RegisterAdminPanelActionsEvent event)
	{
		event.register(new Action(new ResourceLocation(FTBQuests.MOD_ID, "edit_quests"), new TextComponentTranslation("ftbquests.general.editing_mode.button"), GuiIcons.BOOK_RED, 0)
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
		});
	}
}