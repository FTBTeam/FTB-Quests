package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import net.minecraft.util.ResourceLocation;

/**
 * @author LatvianModder
 */
public class QuestTaskKey
{
	public static final DataOut.Serializer<QuestTaskKey> SERIALIZER = (data, object) -> {
		data.writeResourceLocation(object.quest);
		data.writeByte(object.index);
	};

	public static final DataIn.Deserializer<QuestTaskKey> DESERIALIZER = data -> new QuestTaskKey(data.readResourceLocation(), data.readUnsignedByte());

	public final ResourceLocation quest;
	public final int index;

	public QuestTaskKey(ResourceLocation q, int i)
	{
		quest = q;
		index = i;
	}

	public QuestTaskKey(String s)
	{
		String[] s1 = s.split(":", 3);
		quest = new ResourceLocation(s1[0], s1[1]);
		index = Integer.parseInt(s1[2]);
	}

	public String toString()
	{
		return quest.toString() + ':' + index;
	}

	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		else if (o instanceof QuestTaskKey)
		{
			QuestTaskKey key = (QuestTaskKey) o;
			return index == key.index && quest.equals(key.quest);
		}

		return false;
	}

	public int hashCode()
	{
		return quest.hashCode();
	}
}