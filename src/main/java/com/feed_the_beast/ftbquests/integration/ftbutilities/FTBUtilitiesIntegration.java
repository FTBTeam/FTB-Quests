package com.feed_the_beast.ftbquests.integration.ftbutilities;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbutilities.data.Leaderboard;
import com.feed_the_beast.ftbutilities.events.LeaderboardRegistryEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Comparator;
import java.util.Objects;

/**
 * @author LatvianModder
 */
public class FTBUtilitiesIntegration
{
	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(FTBUtilitiesIntegration.class);
	}

	@SubscribeEvent
	public static void registerLeaderboards(LeaderboardRegistryEvent event)
	{
		event.register(new Leaderboard(
				new ResourceLocation(FTBQuests.MOD_ID, "progress"),
				new TextComponentTranslation("ftbquests.leaderboard_progress"),
				player -> new TextComponentString(ServerQuestFile.INSTANCE.getRelativeProgress(Objects.requireNonNull(ServerQuestFile.INSTANCE.getData(player.team.getUID()))) + "%"),
				Comparator.comparingLong(player -> ServerQuestFile.INSTANCE.getProgress(Objects.requireNonNull(ServerQuestFile.INSTANCE.getData(player.team.getUID())))),
				player -> ServerQuestFile.INSTANCE.getProgress(Objects.requireNonNull(ServerQuestFile.INSTANCE.getData(player.team.getUID()))) > 0L));
	}
}