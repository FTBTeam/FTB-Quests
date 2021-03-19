package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class QuestObjectArgument implements ArgumentType<QuestObjectBase> {

	private static final List<String> examples = ImmutableList.of(
			"1CF239D256879E6F",
			"#importantquests"
	);

	private static final DynamicCommandExceptionType NO_OBJECT = new DynamicCommandExceptionType(
			(object) -> new TranslatableComponent("commands.ftbquests.change_progress.no_object", object));

	private static final DynamicCommandExceptionType INVALID_ID = new DynamicCommandExceptionType(
			(id) -> new TranslatableComponent("commands.ftbquests.change_progress.invalid_id", id));

	@Override
	public QuestObjectBase parse(StringReader reader) throws CommandSyntaxException {
		String id = reader.readString();
		if (id.startsWith("#")) {
			for (QuestObjectBase object : ServerQuestFile.INSTANCE.getAllObjects()) {
				if (object.hasTag(id.substring(1))) {
					return object;
				}
			}
			throw NO_OBJECT.createWithContext(reader, id);
		} else {
			try {
				long num = Long.decode(id);
				QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(num);
				if (object == null) {
					throw NO_OBJECT.createWithContext(reader, id);
				}
				return object;
			} catch (NumberFormatException e) {
				throw INVALID_ID.createWithContext(reader, id);
			}
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(
				ServerQuestFile.INSTANCE
						.getAllObjects()
						.stream()
						.map(QuestFile::getCodeString),
				builder
		);
	}

	@Override
	public Collection<String> getExamples() {
		return examples;
	}

	public static QuestObjectArgument questObject() {
		return new QuestObjectArgument();
	}

}
