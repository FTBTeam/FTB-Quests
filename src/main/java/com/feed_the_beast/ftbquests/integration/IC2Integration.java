package com.feed_the_beast.ftbquests.integration;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTasks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * @author LatvianModder
 */
public class IC2Integration
{
	public static void preInit()
	{
		QuestTasks.add(IC2EnergyTask.ID, IC2EnergyTask::new);
		GameRegistry.registerTileEntity(TileQuestIC2.class, new ResourceLocation(FTBQuests.MOD_ID, "quest_block_ic2"));
	}
}