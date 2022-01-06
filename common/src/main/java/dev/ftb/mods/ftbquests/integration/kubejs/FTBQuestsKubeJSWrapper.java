package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.latvian.mods.kubejs.level.world.LevelJS;
import dev.latvian.mods.kubejs.player.PlayerJS;
import dev.latvian.mods.kubejs.server.ServerJS;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
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

	public QuestFile getFile(LevelJS world) {
		return FTBQuests.PROXY.getQuestFile(world.minecraftLevel.isClientSide());
	}

	@Nullable
	public TeamData getData(LevelJS world, UUID uuid) {
		return getFile(world).getData(FTBTeamsAPI.getPlayerTeamID(uuid));
	}

	@Nullable
	public TeamData getData(PlayerJS player) {
		return getFile(player.getLevel()).getData(player.minecraftPlayer);
	}

	@Nullable
	public QuestObjectBase getObject(LevelJS world, Object id) {
		QuestFile file = getFile(world);
		return file.getBase(file.getID(id));
	}

	@Nullable
	public FTBQuestsKubeJSPlayerData getServerDataFromPlayer(Player player) {
		try {
			return (FTBQuestsKubeJSPlayerData) ServerJS.instance.getPlayer(player).getData().get("ftbquests");
		} catch (Throwable e) {
			return null;
		}
	}

	@Nullable
	public FTBQuestsKubeJSPlayerData getServerDataFromSource(CommandSourceStack source) {
		try {
			return (FTBQuestsKubeJSPlayerData) ServerJS.instance.getPlayer(source.getPlayerOrException()).getData().get("ftbquests");
		} catch (Throwable e) {
			return null;
		}
	}
}