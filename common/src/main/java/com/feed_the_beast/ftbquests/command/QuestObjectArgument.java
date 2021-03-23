package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class QuestObjectArgument implements ArgumentType<QuestObjectBase> {

	private static final List<String> examples = ImmutableList.of(
			"1CF239D256879E6F",
			"#importantquests"
	);

	private static final SimpleCommandExceptionType NO_FILE = new SimpleCommandExceptionType(
			new TranslatableComponent("commands.ftbquests.change_progress.no_file"));

	private static final DynamicCommandExceptionType NO_OBJECT = new DynamicCommandExceptionType(
			(object) -> new TranslatableComponent("commands.ftbquests.change_progress.no_object", object));

	private static final DynamicCommandExceptionType INVALID_ID = new DynamicCommandExceptionType(
			(id) -> new TranslatableComponent("commands.ftbquests.change_progress.invalid_id", id));

	@Override
	public QuestObjectBase parse(StringReader reader) throws CommandSyntaxException {
		String id = reader.readString();
		QuestFile file = findQuestFile();
		if (file != null) {
			if (id.startsWith("#")) {
				for (QuestObjectBase object : file.getAllObjects()) {
					if (object.hasTag(id.substring(1))) {
						return object;
					}
				}
				throw NO_OBJECT.createWithContext(reader, id);
			} else {
				try {
					long num = file.getID(id);
					QuestObjectBase object = file.getBase(num);
					if (object == null) {
						throw NO_OBJECT.createWithContext(reader, id);
					}
					return object;
				} catch (NumberFormatException e) {
					throw INVALID_ID.createWithContext(reader, id);
				}
			}
		}
		throw NO_FILE.create();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		QuestFile file = findQuestFile();
		if (file != null) {
			return SharedSuggestionProvider.suggest(
					file.getAllObjects()
							.stream()
							.map(QuestFile::getCodeString),
					builder
			);
		}
		return Suggestions.empty();
	}

	@Override
	public Collection<String> getExamples() {
		return examples;
	}

	public static QuestObjectArgument questObject() {
		return new QuestObjectArgument();
	}

	@Nullable
	private static QuestFile findQuestFile() {
		if (!QuestObjectBase.isNull(ServerQuestFile.INSTANCE)) {
			return ServerQuestFile.INSTANCE;
		} else if (!QuestObjectBase.isNull(ClientQuestFile.INSTANCE)) {
			return ClientQuestFile.INSTANCE;
		}

		return null;
	}

}
