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
	NULL("null", 1, TextFormatting.BLACK),
	FILE("file", 2, TextFormatting.RED),
	CHAPTER("chapter", 4, TextFormatting.GOLD),
	QUEST("quest", 8, TextFormatting.GREEN),
	TASK("task", 16, TextFormatting.BLUE),
	VARIABLE("variable", 32, TextFormatting.DARK_PURPLE),
	REWARD("reward", 64, TextFormatting.LIGHT_PURPLE),
	REWARD_TABLE("reward_table", 128, TextFormatting.YELLOW);

	public static final List<QuestObjectType> ALL = Arrays.asList(values());
	public static final List<QuestObjectType> ALL_PROGRESSING = Arrays.asList(FILE, CHAPTER, FILE, TASK, VARIABLE);

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