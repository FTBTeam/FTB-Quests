package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftbquests.quest.ChangeProgress;
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
import java.util.concurrent.CompletableFuture;

public class ChangeProgressArgument implements ArgumentType<ChangeProgress> {

	private static final DynamicCommandExceptionType INVALID_TYPE = new DynamicCommandExceptionType(
			(type) -> new TranslatableComponent("commands.ftbquests.change_progress.invalid_type", type));

	@Override
	public ChangeProgress parse(StringReader reader) throws CommandSyntaxException {
		String name = reader.readUnquotedString();
		ChangeProgress type = ChangeProgress.NAME_MAP.getNullable(name);
		if (type == null) {
			throw INVALID_TYPE.createWithContext(reader, name);
		}
		return type;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(ChangeProgress.NAME_MAP.keys, builder);
	}

	@Override
	public Collection<String> getExamples() {
		return ChangeProgress.NAME_MAP.keys;
	}

	public static ChangeProgressArgument changeProgress() {
		return new ChangeProgressArgument();
	}

}
