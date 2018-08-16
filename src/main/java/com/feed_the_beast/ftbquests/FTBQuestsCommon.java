package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;

public class FTBQuestsCommon
{
	public void preInit()
	{
	}

	public final QuestFile getQuestFile(@Nullable World world)
	{
		return getQuestFile(world == null ? FMLCommonHandler.instance().getEffectiveSide().isClient() : world.isRemote);
	}

	public QuestFile getQuestFile(boolean clientSide)
	{
		return ServerQuestFile.INSTANCE;
	}
}