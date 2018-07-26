package com.feed_the_beast.ftbquests.quest;

import net.minecraft.util.IStringSerializable;

/**
 * @author LatvianModder
 */
public enum QuestObjectType implements IStringSerializable
{
	LIST("list"),
	CHAPTER("chapter"),
	QUEST("quest"),
	TASK("task"),
	REWARD("reward");

	public static final QuestObjectType[] VALUES = values();

	private final String name;
	private final String translationKey;

	QuestObjectType(String n)
	{
		name = n;
		translationKey = "ftbquests." + name;
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
}