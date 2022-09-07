package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class FTBQuestsKubeJSWrapper {
	public static final FTBQuestsKubeJSWrapper INSTANCE = new FTBQuestsKubeJSWrapper();

	public Map<String, QuestShape> getQuestShapes() {
		return QuestShape.MAP;
	}

	public Map<String, QuestObjectType> getQuestObjectTypes() {
		return QuestObjectType.NAME_MAP.map;
	}

	public QuestFile getFile(Level level) {
		return FTBQuests.PROXY.getQuestFile(level.isClientSide);
	}

	@Nullable
	public TeamData getData(Level level, UUID uuid) {
		return getFile(level).getData(FTBTeamsAPI.getPlayerTeamID(uuid));
	}

	@Nullable
	public TeamData getData(Player player) {
		return getFile(player.getLevel()).getData(player);
	}

	@Nullable
	public QuestObjectBase getObject(Level level, Object id) {
		QuestFile file = getFile(level);
		return file.getBase(file.getID(id));
	}

	@Nullable
	public FTBQuestsKubeJSPlayerData getServerDataFromPlayer(Player player) {
		try {
			return ((FTBQuestsKubeJSPlayerData) player.kjs$getData().get("ftbquests"));
		} catch (Throwable e) {
			return null;
		}
	}

	@Nullable
	public FTBQuestsKubeJSPlayerData getServerDataFromSource(CommandSourceStack source) {
		try {
			return getServerDataFromPlayer(source.getPlayerOrException());
		} catch (Throwable e) {
			return null;
		}
	}
}
