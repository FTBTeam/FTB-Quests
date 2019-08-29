package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.QuestShape;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import dev.latvian.kubejs.documentation.DocClass;
import dev.latvian.kubejs.documentation.DocField;
import dev.latvian.kubejs.documentation.DocMethod;
import dev.latvian.kubejs.documentation.Param;
import dev.latvian.kubejs.player.PlayerJS;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author LatvianModder
 */
@DocClass(displayName = "FTB Quests Integration")
public class FTBQuestsKubeJSWrapper
{
	@DocField
	public final Map<String, QuestShape> questShapes = QuestShape.NAME_MAP.map;

	@DocField
	public final Map<String, QuestObjectType> questObjectTypes = QuestObjectType.NAME_MAP.map;

	@DocField
	public final Map<String, ChangeProgress> changeProgressTypes = ChangeProgress.NAME_MAP.map;

	@Nullable
	@DocMethod("Currently loaded quest file. Can be null")
	public ServerQuestFile file()
	{
		return ServerQuestFile.INSTANCE;
	}

	@Nullable
	@DocMethod(value = "Quest data from team UID", params = @Param("team"))
	public QuestData data(int team)
	{
		return ServerQuestFile.INSTANCE.getData((short) team);
	}

	@Nullable
	@DocMethod(value = "Quest data from team ID", params = @Param("team"))
	public QuestData data(String team)
	{
		return ServerQuestFile.INSTANCE.getData(team);
	}

	@Nullable
	@DocMethod(value = "Quest data from player")
	public QuestData data(PlayerJS player)
	{
		return ServerQuestFile.INSTANCE.getData(player.player);
	}

	@Nullable
	@DocMethod(value = "Quest object from object UID", params = @Param("id"))
	public QuestObjectBase object(Object id)
	{
		if (id instanceof Number)
		{
			return ServerQuestFile.INSTANCE.getBase(((Number) id).intValue());
		}

		return object(QuestFile.getID(id.toString()));
	}
}