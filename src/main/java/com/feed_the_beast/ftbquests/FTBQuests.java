package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftblib.lib.OtherMods;
import com.feed_the_beast.ftbquests.block.QuestBlockData;
import com.feed_the_beast.ftbquests.integration.IC2Integration;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.quest.rewards.QuestRewards;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
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

	private static final String PERM_EDIT = "ftbquests.edit";
	public static final String PERM_RESET_PROGRESS = "ftbquests.reset_progress";

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event)
	{
		FTBQuestsConfig.sync();
		FTBQuestsNetHandler.init();

		CapabilityManager.INSTANCE.register(QuestBlockData.class, new Capability.IStorage<QuestBlockData>()
		{
			@Override
			public NBTBase writeNBT(Capability<QuestBlockData> capability, QuestBlockData instance, EnumFacing side)
			{
				return instance.serializeNBT();
			}

			@Override
			public void readNBT(Capability<QuestBlockData> capability, QuestBlockData instance, EnumFacing side, NBTBase nbt)
			{
				if (nbt instanceof NBTTagCompound)
				{
					instance.deserializeNBT((NBTTagCompound) nbt);
				}
			}
		}, () -> new QuestBlockData(null));

		QuestTasks.init();
		QuestRewards.init();

		if (Loader.isModLoaded(OtherMods.IC2))
		{
			IC2Integration.preInit();
		}
	}

	@Mod.EventHandler
	public void onPostInit(FMLPostInitializationEvent event)
	{
		PermissionAPI.registerNode(PERM_EDIT, DefaultPermissionLevel.OP, "Permission for editing quests");
		PermissionAPI.registerNode(PERM_RESET_PROGRESS, DefaultPermissionLevel.OP, "Permission for resetting quest progress");
	}

	public static boolean canEdit(EntityPlayer player)
	{
		return FTBQuestsConfig.general.editing_mode && PermissionAPI.hasPermission(player, PERM_EDIT);
	}
}