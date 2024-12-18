package dev.ftb.mods.ftbquests.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * Avoid using this in commands, since it breaks MC functions; functions are parsed by the server very early on,
 * long before the server quest file can possibly be loaded.
 * Will be removed sometime after 1.21.1
 */
@Deprecated(forRemoval = true)
public class QuestObjectArgument implements ArgumentType<QuestObjectBase> {
	private static final List<String> examples = ImmutableList.of(
			"1CF239D256879E6F",
			"#importantquests"
	);

	private final Predicate<QuestObjectBase> filter;

	public QuestObjectArgument() {
		this(qo -> true);
	}

	public QuestObjectArgument(Predicate<QuestObjectBase> filter) {
		this.filter = filter;
	}

	@Override
	public QuestObjectBase parse(StringReader reader) throws CommandSyntaxException {
		String id = reader.readString();
		BaseQuestFile file = findQuestFile();
		if (file != null) {
			if (id.startsWith("#")) {
				String val = id.substring(1);
				for (QuestObjectBase object : file.getAllObjects()) {
					if (object.hasTag(val) && filter.test(object)) {
						return object;
					}
				}
				throw FTBQuestsCommands.NO_OBJECT.createWithContext(reader, id);
			} else {
				try {
					long num = file.getID(id);
					QuestObjectBase object = file.getBase(num);
					if (object == null || !filter.test(object)) {
						throw FTBQuestsCommands.NO_OBJECT.createWithContext(reader, id);
					}
					return object;
				} catch (NumberFormatException e) {
					throw FTBQuestsCommands.INVALID_ID.createWithContext(reader, id);
				}
			}
		}
		throw FTBQuestsCommands.NO_FILE.create();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		BaseQuestFile file = findQuestFile();
		if (file != null) {
			return SharedSuggestionProvider.suggest(
					file.getAllObjects()
							.stream()
							.filter(filter)
							.map(BaseQuestFile::getCodeString),
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

	public static QuestObjectArgument questObject(Predicate<QuestObjectBase> filter) {
		return new QuestObjectArgument(filter);
	}

	@Nullable
	private static BaseQuestFile findQuestFile() {
		if (!QuestObjectBase.isNull(ServerQuestFile.INSTANCE)) {
			return ServerQuestFile.INSTANCE;
		} else if (!QuestObjectBase.isNull(ClientQuestFile.INSTANCE)) {
			return ClientQuestFile.INSTANCE;
		}

		return null;
	}

}
