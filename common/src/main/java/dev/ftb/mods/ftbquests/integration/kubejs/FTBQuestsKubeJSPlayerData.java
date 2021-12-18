package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.latvian.mods.kubejs.player.PlayerDataJS;

/**
 * @author LatvianModder
 */
public class FTBQuestsKubeJSPlayerData extends FTBQuestsKubeJSTeamData {
	private final PlayerDataJS playerData;

	public FTBQuestsKubeJSPlayerData(PlayerDataJS p) {
		playerData = p;
	}

	public QuestFile getFile() {
		return ServerQuestFile.INSTANCE;
	}

	public TeamData getData() {
		return ServerQuestFile.INSTANCE.getData(FTBTeamsAPI.getPlayerTeamID(playerData.getId()));
	}
}