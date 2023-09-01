package dev.ftb.mods.ftbquests.util;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author LatvianModder
 */
public class NetUtils {
	public static boolean canEdit(NetworkManager.PacketContext context) {
		Player player = context.getPlayer();
		return player != null && ServerQuestFile.INSTANCE.getOrCreateTeamData(player).getCanEdit(player);
	}

	public static <T> void write(FriendlyByteBuf buffer, Collection<T> list, BiConsumer<FriendlyByteBuf, T> writer) {
		buffer.writeVarInt(list.size());

		for (T value : list) {
			writer.accept(buffer, value);
		}
	}

	public static <K, V> void write(FriendlyByteBuf buffer, Map<K, V> map, BiConsumer<FriendlyByteBuf, K> keyWriter, BiConsumer<FriendlyByteBuf, V> valueWriter) {
		buffer.writeVarInt(map.size());

		for (Map.Entry<K, V> entry : map.entrySet()) {
			keyWriter.accept(buffer, entry.getKey());
			valueWriter.accept(buffer, entry.getValue());
		}
	}

	public static void writeStrings(FriendlyByteBuf buffer, Collection<String> list) {
		write(buffer, list, (b, s) -> b.writeUtf(s, Short.MAX_VALUE));
	}

	public static <T> void read(FriendlyByteBuf buffer, Collection<T> list, Function<FriendlyByteBuf, T> reader) {
		list.clear();

		int s = buffer.readVarInt();

		for (int i = 0; i < s; i++) {
			list.add(reader.apply(buffer));
		}
	}

	public static <K, V> void read(FriendlyByteBuf buffer, Map<K, V> map, Function<FriendlyByteBuf, K> keyReader, BiFunction<K, FriendlyByteBuf, V> valueReader) {
		map.clear();

		int s = buffer.readVarInt();

		for (int i = 0; i < s; i++) {
			K key = keyReader.apply(buffer);
			map.put(key, valueReader.apply(key, buffer));
		}
	}

	public static void readStrings(FriendlyByteBuf buffer, Collection<String> list) {
		read(buffer, list, b -> b.readUtf(Short.MAX_VALUE));
	}

	public static void writeIcon(FriendlyByteBuf buffer, Icon icon) {
		buffer.writeUtf(icon.toString(), Short.MAX_VALUE);
	}

	public static Icon readIcon(FriendlyByteBuf buffer) {
		return Icon.getIcon(buffer.readUtf(Short.MAX_VALUE));
	}
}