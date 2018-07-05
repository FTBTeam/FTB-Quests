package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftbquests.block.ItemBlockQuest;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

import javax.annotation.Nullable;

public class FTBQuestsCommon
{
	public static final String PERM_EDIT = "admin_panel.ftbquests.edit";
	public static final String PERM_RESET_PROGRESS = "admin_panel.ftbquests.reset_progress";

	public void preInit()
	{
		FTBQuestsConfig.sync();
		FTBQuestsNetHandler.init();

		CapabilityManager.INSTANCE.register(ItemBlockQuest.Data.class, new Capability.IStorage<ItemBlockQuest.Data>()
		{
			@Override
			public NBTBase writeNBT(Capability<ItemBlockQuest.Data> capability, ItemBlockQuest.Data instance, EnumFacing side)
			{
				return instance.serializeNBT();
			}

			@Override
			public void readNBT(Capability<ItemBlockQuest.Data> capability, ItemBlockQuest.Data instance, EnumFacing side, NBTBase nbt)
			{
				if (nbt instanceof NBTTagCompound)
				{
					instance.deserializeNBT((NBTTagCompound) nbt);
				}
			}
		}, ItemBlockQuest.Data::new);
	}

	public void postInit()
	{
		PermissionAPI.registerNode(PERM_EDIT, DefaultPermissionLevel.OP, "Permission for editing quests");
		PermissionAPI.registerNode(PERM_RESET_PROGRESS, DefaultPermissionLevel.OP, "Permission for resetting quest progress");
	}

	@Nullable
	public IProgressData getOwner(String owner, boolean clientSide)
	{
		if (clientSide || !Universe.loaded())
		{
			return null;
		}

		ForgeTeam team = Universe.get().getTeam(owner);
		return team.isValid() ? FTBQuestsTeamData.get(team) : null;
	}
}