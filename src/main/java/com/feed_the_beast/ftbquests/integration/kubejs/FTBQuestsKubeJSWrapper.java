package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.QuestShape;
import dev.latvian.kubejs.documentation.DisplayName;
import dev.latvian.kubejs.documentation.Info;
import dev.latvian.kubejs.documentation.P;
import dev.latvian.kubejs.documentation.T;
import dev.latvian.kubejs.player.PlayerJS;
import dev.latvian.kubejs.world.WorldJS;

import javax.annotation.Nullable;
import java.util.Map;

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

	@Info("Currently loaded quest file. Can be null")
	public QuestFile getFile(@P("world") WorldJS world)
	{
		QuestFile f = FTBQuests.PROXY.getQuestFile(world.world);

		if (f == null)
		{
			throw new NullPointerException("Quest file isn't loaded!");
		}

		return f;
	}

	@Nullable
	@Info("Quest data from team UID")
	public QuestData getData(@P("world") WorldJS world, @P("team") @T(short.class) Number team)
	{
		return getFile(world).getData(team.shortValue());
	}

	@Nullable
	@Info("Quest data from team ID")
	public QuestData getData(@P("world") WorldJS world, @P("team") String team)
	{
		return getFile(world).getData(team);
	}

	@Nullable
	@Info("Quest data from player")
	public QuestData getData(@P("player") PlayerJS player)
	{
		return getFile(player.getWorld()).getData(player.getPlayerEntity());
	}

	@Nullable
	@Info("Quest object from object UID")
	public QuestObjectBase getObject(@P("world") WorldJS world, @P("id") Object id)
	{
		QuestFile file = getFile(world);
		return file.getBase(file.getID(id));
	}
}