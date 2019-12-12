package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.QuestShape;
import dev.latvian.kubejs.documentation.DisplayName;
import dev.latvian.kubejs.documentation.Info;
import dev.latvian.kubejs.documentation.P;
import dev.latvian.kubejs.player.PlayerJS;
import dev.latvian.kubejs.world.WorldJS;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

/**
 * @author LatvianModder
 */
@DisplayName("FTB Quests Integration")
public class FTBQuestsKubeJSWrapper
{
	public Map<String, QuestShape> getQuestShapes()
	{
		return QuestShape.NAME_MAP.map;
	}

	public Map<String, QuestObjectType> getQuestObjectTypes()
	{
		return QuestObjectType.NAME_MAP.map;
	}

	public Map<String, ChangeProgress> getChangeProgressTypes()
	{
		return ChangeProgress.NAME_MAP.map;
	}

	@Info("Currently loaded quest file")
	public QuestFile getFile(@P("world") WorldJS world)
	{
		return FTBQuests.PROXY.getQuestFile(world.minecraftWorld);
	}

	@Nullable
	@Info("Quest data from UUID")
	public PlayerData getData(@P("world") WorldJS world, @P("uuid") UUID uuid)
	{
		return getFile(world).getData(uuid);
	}

	@Nullable
	@Info("Quest data from player")
	public PlayerData getData(@P("player") PlayerJS player)
	{
		return getFile(player.getWorld()).getData(player.minecraftPlayer);
	}

	@Nullable
	@Info("Quest object from object UID")
	public QuestObjectBase getObject(@P("world") WorldJS world, @P("id") Object id)
	{
		QuestFile file = getFile(world);
		return file.getBase(file.getID(id));
	}
}