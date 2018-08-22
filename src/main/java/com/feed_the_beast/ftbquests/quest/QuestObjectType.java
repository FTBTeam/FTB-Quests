package com.feed_the_beast.ftbquests.quest;

import net.minecraft.util.IStringSerializable;

/**
 * @author LatvianModder
 */
public enum QuestObjectType implements IStringSerializable
{
	FILE("file", 1),
	CHAPTER("chapter", 2),
	QUEST("quest", 4),
	TASK("task", 8);

	public static final QuestObjectType[] VALUES = values();

	private final String name;
	private final String translationKey;
	private final int flag;

	QuestObjectType(String n, int f)
	{
		name = n;
		translationKey = "ftbquests." + name;
		flag = f;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public String getTranslationKey()
	{
		return translationKey;
	}

	public int getFlag()
	{
		return flag;
	}
}