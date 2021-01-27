package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.QuestShape;
import dev.latvian.kubejs.player.PlayerJS;
import dev.latvian.kubejs.world.WorldJS;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class FTBQuestsKubeJSWrapper
{
	public Map<String, QuestShape> getQuestShapes()
	{
		return QuestShape.MAP;
	}

	public Map<String, QuestObjectType> getQuestObjectTypes()
	{
		return QuestObjectType.NAME_MAP.map;
	}

	public Map<String, ChangeProgress> getChangeProgressTypes()
	{
		return ChangeProgress.NAME_MAP.map;
	}

	public QuestFile getFile(WorldJS world)
	{
		return FTBQuests.PROXY.getQuestFile(world.minecraftWorld.isClientSide());
	}

	@Nullable
	public PlayerData getData(WorldJS world, UUID uuid)
	{
		return getFile(world).getData(uuid);
	}

	@Nullable
	public PlayerData getData(PlayerJS player)
	{
		return getFile(player.getWorld()).getData(player.minecraftPlayer);
	}

	@Nullable
	public QuestObjectBase getObject(WorldJS world, Object id)
	{
		QuestFile file = getFile(world);
		return file.getBase(file.getID(id));
	}
}