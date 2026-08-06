package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class NetUtils {
	public static <T> void write(FriendlyByteBuf buffer, Collection<T> list, BiConsumer<FriendlyByteBuf, T> writer) {
		buffer.writeCollection(list, writer::accept);
	}

	public static <T> void read(FriendlyByteBuf buffer, Collection<T> list, Function<FriendlyByteBuf, T> reader) {
		list.clear();
		list.addAll(buffer.readList(reader::apply));
	}

	public static void writeStrings(FriendlyByteBuf buffer, Collection<String> list) {
		write(buffer, list, FriendlyByteBuf::writeUtf);
	}

	public static void readStrings(FriendlyByteBuf buffer, Collection<String> list) {
		read(buffer, list, b -> b.readUtf(Short.MAX_VALUE));
	}

	public static void writeIcon(FriendlyByteBuf buffer, Icon icon) {
		buffer.writeUtf(icon.toString());
	}

	public static Icon readIcon(FriendlyByteBuf buffer) {
		return Icon.getIcon(buffer.readUtf());
	}
}