package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;

/**
 * @author LatvianModder
 */
public class FTBQuestsKubeJSTeamDataWrapper extends FTBQuestsKubeJSTeamData {
	private final TeamData teamData;

	public FTBQuestsKubeJSTeamDataWrapper(TeamData d) {
		teamData = d;
	}

	public QuestFile getFile() {
		return teamData.file;
	}

	public TeamData getData() {
		return teamData;
	}
}