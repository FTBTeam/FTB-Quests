package dev.ftb.mods.ftbquests.quest;

import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import net.minecraft.ChatFormatting;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public enum QuestObjectType implements Predicate<QuestObjectBase> {
	NULL("null", ChatFormatting.BLACK),
	FILE("file", ChatFormatting.RED),
	CHAPTER("chapter", ChatFormatting.GOLD),
	QUEST("quest", ChatFormatting.GREEN),
	TASK("task", ChatFormatting.BLUE),
	REWARD("reward", ChatFormatting.LIGHT_PURPLE),
	REWARD_TABLE("reward_table", ChatFormatting.YELLOW),
	CHAPTER_GROUP("chapter_group", ChatFormatting.YELLOW),

	;

	public static final NameMap<QuestObjectType> NAME_MAP = NameMap.of(NULL, values()).id(v -> v.id).nameKey(v -> v.translationKey).create();
	public static final Predicate<QuestObjectBase> ALL_PROGRESSING = object -> object instanceof QuestObject;
	public static final Predicate<QuestObjectBase> ALL_PROGRESSING_OR_NULL = object -> object == null || object instanceof QuestObject;

	public final String id;
	public final String translationKey;
	private final ChatFormatting color;

	QuestObjectType(String i, ChatFormatting c) {
		id = i;
		translationKey = "ftbquests." + id;
		color = c;
	}

	public ChatFormatting getColor() {
		return color;
	}

	@Override
	public boolean test(QuestObjectBase object) {
		return (object == null ? NULL : object.getObjectType()) == this;
	}
}