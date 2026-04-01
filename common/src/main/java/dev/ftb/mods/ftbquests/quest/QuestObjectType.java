package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.util.NameMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public enum QuestObjectType implements Predicate<QuestObjectBase> {
	NULL("null", ChatFormatting.BLACK),
	FILE("file", ChatFormatting.RED),
	CHAPTER("chapter", ChatFormatting.GOLD),
	QUEST("quest", ChatFormatting.GREEN),
	TASK("task", ChatFormatting.BLUE),
	REWARD("reward", ChatFormatting.LIGHT_PURPLE),
	REWARD_TABLE("reward_table", ChatFormatting.YELLOW),
	CHAPTER_GROUP("chapter_group", ChatFormatting.YELLOW),
	QUEST_LINK("quest_link", ChatFormatting.DARK_GREEN),
	IMAGE("chapter.image", ChatFormatting.AQUA),
	;

	public static final NameMap<QuestObjectType> NAME_MAP = NameMap.of(NULL, values()).id(v -> v.id).nameKey(v -> v.translationKey).create();
	public static StreamCodec<FriendlyByteBuf, QuestObjectType> STREAM_CODEC = StreamCodec.of(NAME_MAP::write, NAME_MAP::read);

	public static final Predicate<QuestObjectBase> ALL_PROGRESSING = object -> object instanceof QuestObject;
	public static final Predicate<@Nullable QuestObjectBase> ALL_PROGRESSING_OR_NULL = object -> object == null || object instanceof QuestObject;

	private final String id;
	private final ChatFormatting color;
	private final String translationKey;

	QuestObjectType(String id, ChatFormatting color) {
		this.id = id;
		this.color = color;
		translationKey = "ftbquests." + this.id;
	}

	public String getId() {
		return id;
	}

	public ChatFormatting getColor() {
		return color;
	}

	@Override
	public boolean test(@Nullable QuestObjectBase object) {
		return (object == null ? NULL : object.getObjectType()) == this;
	}

	public Component getCompletedMessage() {
		return Component.translatable(translationKey + ".completed");
	}
}