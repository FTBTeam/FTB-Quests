package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import dev.latvian.kubejs.documentation.DocClass;
import dev.latvian.kubejs.documentation.DocField;
import dev.latvian.kubejs.player.EntityArrayList;
import dev.latvian.kubejs.server.ServerEventJS;
import dev.latvian.kubejs.server.ServerJS;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;

/**
 * @author LatvianModder
 */
@DocClass("Event that gets fired when an object is completed. It can be a file, quest, chapter, task")
public class QuestObjectCompletedEventJS extends ServerEventJS
{
	@DocField
	public final QuestData data;

	@DocField
	public final QuestObject object;

	@DocField("List of notified players. It isn't always the list of online members of that team, for example, this list is empty when invisible quest was completed")
	public final EntityArrayList notifiedPlayers;

	public QuestObjectCompletedEventJS(QuestData d, QuestObject o, List<EntityPlayerMP> n)
	{
		super(ServerJS.instance);
		data = d;
		object = o;
		notifiedPlayers = server.entities(n);
	}
}