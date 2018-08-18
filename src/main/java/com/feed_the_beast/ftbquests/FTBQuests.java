package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftblib.lib.OtherMods;
import com.feed_the_beast.ftbquests.integration.ic2.IC2Integration;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.quest.rewards.QuestRewardType;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskType;
import com.feed_the_beast.ftbquests.util.FTBQuestsWorldData;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
		modid = FTBQuests.MOD_ID,
		name = FTBQuests.MOD_NAME,
		version = FTBQuests.VERSION,
		dependencies = "required-after:ftblib;after:ic2"
)
public class FTBQuests
{
	public static final String MOD_ID = "ftbquests";
	public static final String MOD_NAME = "FTB Quests";
	public static final String VERSION = "@VERSION@";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	@SidedProxy(serverSide = "com.feed_the_beast.ftbquests.FTBQuestsCommon", clientSide = "com.feed_the_beast.ftbquests.client.FTBQuestsClient")
	public static FTBQuestsCommon PROXY;

	private static final String PERM_EDIT_QUESTS = "ftbquests.edit_quests";
	public static final String PERM_EDIT_SETTINGS = "ftbquests.edit_settings";

	public static final CreativeTabs TAB = new CreativeTabs(FTBQuests.MOD_ID)
	{
		@Override
		public ItemStack getTabIconItem()
		{
			return new ItemStack(FTBQuestsItems.SCREEN);
		}
	};

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event)
	{
		FTBQuestsNetHandler.init();

		if (Loader.isModLoaded(OtherMods.IC2))
		{
			IC2Integration.preInit();
		}

		PROXY.preInit();
	}

	@Mod.EventHandler
	public void onPostInit(FMLPostInitializationEvent event)
	{
		QuestTaskType.createRegistry();
		QuestRewardType.createRegistry();

		PermissionAPI.registerNode(PERM_EDIT_QUESTS, DefaultPermissionLevel.OP, "Permission for editing quests and resetting progress");
		PermissionAPI.registerNode(PERM_EDIT_SETTINGS, DefaultPermissionLevel.OP, "Permission for editing FTBQuests server settings");
	}

	public static boolean canEdit(EntityPlayerMP player)
	{
		return FTBQuestsWorldData.INSTANCE.editingMode.getBoolean() && (player.server.isSinglePlayer() || PermissionAPI.hasPermission(player, PERM_EDIT_QUESTS));
	}
}