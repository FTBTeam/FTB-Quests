package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.ChangeProgress;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.latvian.kubejs.player.PlayerJS;
import dev.latvian.kubejs.server.ServerJS;
import dev.latvian.kubejs.world.WorldJS;
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

	public Map<String, ChangeProgress> getChangeProgressTypes() {
		return ChangeProgress.NAME_MAP.map;
	}

	public QuestFile getFile(WorldJS world) {
		return FTBQuests.PROXY.getQuestFile(world.minecraftWorld.isClientSide());
	}

	@Nullable
	public TeamData getData(WorldJS world, UUID uuid) {
		return getFile(world).getData(FTBTeamsAPI.getPlayerTeamID(uuid));
	}

	@Nullable
	public TeamData getData(PlayerJS player) {
		return getFile(player.getWorld()).getData(player.minecraftPlayer);
	}

	@Nullable
	public QuestObjectBase getObject(WorldJS world, Object id) {
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