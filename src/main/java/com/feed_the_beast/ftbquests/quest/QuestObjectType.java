package com.feed_the_beast.ftbquests.quest;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.List;

/**
 * @author LatvianModder
 */
public enum QuestObjectType implements IStringSerializable
{
	FILE("file", 1, TextFormatting.RED),
	CHAPTER("chapter", 2, TextFormatting.GOLD),
	QUEST("quest", 4, TextFormatting.GREEN),
	TASK("task", 8, TextFormatting.BLUE),
	VARIABLE("variable", 16, TextFormatting.DARK_PURPLE);

	public static final QuestObjectType[] VALUES = values();
	public static final List<QuestObjectType> ALL = Arrays.asList(VALUES);

	private final String name;
	private final String translationKey;
	private final int flag;
	private final TextFormatting color;

	QuestObjectType(String n, int f, TextFormatting c)
	{
		name = n;
		translationKey = "ftbquests." + name;
		flag = f;
		color = c;
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

	public TextFormatting getColor()
	{
		return color;
	}
}