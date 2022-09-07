package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.world.entity.player.Player;

/**
 * @author LatvianModder
 */
public class FTBQuestsKubeJSPlayerData extends FTBQuestsKubeJSTeamData {
	private final Player player;

	public FTBQuestsKubeJSPlayerData(Player p) {
		player = p;
	}

	public QuestFile getFile() {
		return ServerQuestFile.INSTANCE;
	}

	public TeamData getData() {
		return ServerQuestFile.INSTANCE.getData(player);
	}
}