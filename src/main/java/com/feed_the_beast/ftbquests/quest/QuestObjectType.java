package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import net.minecraft.util.text.TextFormatting;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public enum QuestObjectType implements Predicate<QuestObjectBase>
{
	NULL("null", TextFormatting.BLACK),
	FILE("file", TextFormatting.RED),
	CHAPTER("chapter", TextFormatting.GOLD),
	QUEST("quest", TextFormatting.GREEN),
	TASK("task", TextFormatting.BLUE),
	REWARD("reward", TextFormatting.LIGHT_PURPLE),
	REWARD_TABLE("reward_table", TextFormatting.YELLOW);

	public static final NameMap<QuestObjectType> NAME_MAP = NameMap.of(NULL, values()).id(v -> v.id).nameKey(v -> v.translationKey).create();
	public static final Predicate<QuestObjectBase> ALL_PROGRESSING = object -> object instanceof QuestObject;
	public static final Predicate<QuestObjectBase> ALL_PROGRESSING_OR_NULL = object -> object == null || object instanceof QuestObject;

	public final String id;
	public final String translationKey;
	private final TextFormatting color;

	QuestObjectType(String i, TextFormatting c)
	{
		id = i;
		translationKey = "ftbquests." + id;
		color = c;
	}

	public TextFormatting getColor()
	{
		return color;
	}

	@Override
	public boolean test(QuestObjectBase object)
	{
		return (object == null ? NULL : object.getObjectType()) == this;
	}
}