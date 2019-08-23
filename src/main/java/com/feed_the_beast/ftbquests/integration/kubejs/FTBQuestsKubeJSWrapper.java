package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.QuestShape;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import dev.latvian.kubejs.player.PlayerJS;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class FTBQuestsKubeJSWrapper
{
	public final Map<String, QuestShape> questShapes = QuestShape.NAME_MAP.map;
	public final Map<String, QuestObjectType> questObjectTypes = QuestObjectType.NAME_MAP.map;
	public final Map<String, ChangeProgress> changeProgressTypes = ChangeProgress.NAME_MAP.map;

	@Nullable
	public ServerQuestFile file()
	{
		return ServerQuestFile.INSTANCE;
	}

	public boolean canEdit(PlayerJS player)
	{
		return FTBQuests.canEdit(player.player);
	}

	@Nullable
	public QuestData data(int team)
	{
		return ServerQuestFile.INSTANCE.getData((short) team);
	}

	@Nullable
	public QuestData data(String team)
	{
		return ServerQuestFile.INSTANCE.getData(team);
	}

	@Nullable
	public QuestData data(PlayerJS player)
	{
		return ServerQuestFile.INSTANCE.getData(player.player);
	}

	@Nullable
	public QuestObjectBase object(int id)
	{
		return ServerQuestFile.INSTANCE.getBase(id);
	}

	@Nullable
	public QuestObjectBase object(String id)
	{
		return object(QuestFile.getID(id));
	}
}