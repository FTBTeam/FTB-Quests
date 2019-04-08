package com.feed_the_beast.ftbquests.integration.ftbutilities;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbutilities.data.Leaderboard;
import com.feed_the_beast.ftbutilities.events.LeaderboardRegistryEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Comparator;

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
				player -> {
					ITeamData data = ServerQuestFile.INSTANCE.getData(player.team.getUID());

					if (data == null)
					{
						return new TextComponentString("0%");
					}

					return new TextComponentString(ServerQuestFile.INSTANCE.getRelativeProgress(data) + "%");
				},
				Comparator.comparingLong(player -> {
					ITeamData data = ServerQuestFile.INSTANCE.getData(player.team.getUID());
					return data == null ? 0L : -ServerQuestFile.INSTANCE.getRelativeProgress(data);
				}),
				player -> {
					ITeamData data = ServerQuestFile.INSTANCE.getData(player.team.getUID());
					return data != null && ServerQuestFile.INSTANCE.getRelativeProgress(data) > 0L;
				})
		);
	}
}