package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftbquests.command.CommandFTBQuests;
import com.feed_the_beast.ftbquests.gui.FTBQuestsGuiHandler;
import com.feed_the_beast.ftbquests.integration.botania.BotaniaIntegration;
import com.feed_the_beast.ftbquests.integration.buildcraft.BuildCraftIntegration;
import com.feed_the_beast.ftbquests.integration.ftbmoney.FTBMoneyIntegration;
import com.feed_the_beast.ftbquests.integration.ic2.IC2Integration;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.item.ItemChoiceLootcrate;
import com.feed_the_beast.ftbquests.item.ItemRandomLootcrate;
import com.feed_the_beast.ftbquests.item.LootRarity;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
		modid = FTBQuests.MOD_ID,
		name = FTBQuests.MOD_NAME,
		version = FTBQuests.VERSION,
		dependencies = FTBLib.THIS_DEP + ";after:ic2"
)
public class FTBQuests
{
	public static final String MOD_ID = "ftbquests";
	public static final String MOD_NAME = "FTB Quests";
	public static final String VERSION = "0.0.0.ftbquests";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	@Mod.Instance(MOD_ID)
	public static FTBQuests MOD;

	@SidedProxy(serverSide = "com.feed_the_beast.ftbquests.FTBQuestsCommon", clientSide = "com.feed_the_beast.ftbquests.client.FTBQuestsClient")
	public static FTBQuestsCommon PROXY;

	public static final CreativeTabs TAB = new CreativeTabs(FTBQuests.MOD_ID)
	{
		@Override
		public ItemStack createIcon()
		{
			return new ItemStack(FTBQuestsItems.LEGENDARY_LOOTCRATE);
		}
	};

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event)
	{
		FTBQuestsNetHandler.init();

		if (Loader.isModLoaded("ic2"))
		{
			IC2Integration.preInit();
		}

		if (Loader.isModLoaded("botania"))
		{
			BotaniaIntegration.preInit();
		}

		if (Loader.isModLoaded("buildcraftcore"))
		{
			BuildCraftIntegration.preInit();
		}

		if (Loader.isModLoaded("ftbmoney"))
		{
			FTBMoneyIntegration.preInit();
		}

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new FTBQuestsGuiHandler());

		for (LootRarity lootRarity : LootRarity.VALUES)
		{
			LootTableList.register(lootRarity.getLootTable());
		}

		CapabilityManager.INSTANCE.register(ItemChoiceLootcrate.Data.class, new Capability.IStorage<ItemChoiceLootcrate.Data>()
		{
			@Override
			public NBTBase writeNBT(Capability<ItemChoiceLootcrate.Data> capability, ItemChoiceLootcrate.Data instance, EnumFacing side)
			{
				return instance.serializeNBT();
			}

			@Override
			public void readNBT(Capability<ItemChoiceLootcrate.Data> capability, ItemChoiceLootcrate.Data instance, EnumFacing side, NBTBase nbt)
			{
				if (nbt instanceof NBTTagCompound)
				{
					instance.deserializeNBT((NBTTagCompound) nbt);
				}
			}
		}, ItemChoiceLootcrate.Data::new);

		CapabilityManager.INSTANCE.register(ItemRandomLootcrate.Data.class, new Capability.IStorage<ItemRandomLootcrate.Data>()
		{
			@Override
			public NBTBase writeNBT(Capability<ItemRandomLootcrate.Data> capability, ItemRandomLootcrate.Data instance, EnumFacing side)
			{
				return instance.serializeNBT();
			}

			@Override
			public void readNBT(Capability<ItemRandomLootcrate.Data> capability, ItemRandomLootcrate.Data instance, EnumFacing side, NBTBase nbt)
			{
				if (nbt instanceof NBTTagCompound)
				{
					instance.deserializeNBT((NBTTagCompound) nbt);
				}
			}
		}, ItemRandomLootcrate.Data::new);

		PROXY.preInit();
	}

	@Mod.EventHandler
	public void onPostInit(FMLPostInitializationEvent event)
	{
		QuestTaskType.createRegistry();
		QuestRewardType.createRegistry();
	}

	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandFTBQuests());
	}

	public static boolean canEdit(EntityPlayerMP player)
	{
		return player.getEntityData().getBoolean("ftbquests_editing_mode");
	}
}