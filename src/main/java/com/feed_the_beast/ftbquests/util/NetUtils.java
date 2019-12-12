package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author LatvianModder
 */
public class NetUtils
{
	public static boolean canEdit(NetworkEvent.Context context)
	{
		ServerPlayerEntity playerEntity = context.getSender();
		return playerEntity != null && ServerQuestFile.INSTANCE.getData(playerEntity).getCanEdit();
	}

	public static <T> void write(PacketBuffer buffer, Collection<T> list, BiConsumer<PacketBuffer, T> writer)
	{
		buffer.writeVarInt(list.size());

		for (T value : list)
		{
			writer.accept(buffer, value);
		}
	}

	public static <K, V> void write(PacketBuffer buffer, Map<K, V> map, BiConsumer<PacketBuffer, K> keyWriter, BiConsumer<PacketBuffer, V> valueWriter)
	{
		buffer.writeVarInt(map.size());

		for (Map.Entry<K, V> entry : map.entrySet())
		{
			keyWriter.accept(buffer, entry.getKey());
			valueWriter.accept(buffer, entry.getValue());
		}
	}

	public static void writeStrings(PacketBuffer buffer, Collection<String> list)
	{
		write(buffer, list, (b, s) -> b.writeString(s, Short.MAX_VALUE));
	}

	public static <T> void read(PacketBuffer buffer, Collection<T> list, Function<PacketBuffer, T> reader)
	{
		list.clear();

		int s = buffer.readVarInt();

		for (int i = 0; i < s; i++)
		{
			list.add(reader.apply(buffer));
		}
	}

	public static <K, V> void read(PacketBuffer buffer, Map<K, V> map, Function<PacketBuffer, K> keyReader, BiFunction<K, PacketBuffer, V> valueReader)
	{
		map.clear();

		int s = buffer.readVarInt();

		for (int i = 0; i < s; i++)
		{
			K key = keyReader.apply(buffer);
			map.put(key, valueReader.apply(key, buffer));
		}
	}

	public static void readStrings(PacketBuffer buffer, Collection<String> list)
	{
		read(buffer, list, b -> b.readString(Short.MAX_VALUE));
	}

	public static void writeUUID(PacketBuffer buffer, UUID uuid)
	{
		buffer.writeLong(uuid.getMostSignificantBits());
		buffer.writeLong(uuid.getLeastSignificantBits());
	}

	public static UUID readUUID(PacketBuffer buffer)
	{
		long most = buffer.readLong();
		long least = buffer.readLong();
		return new UUID(most, least);
	}

	public static void writeIcon(PacketBuffer buffer, Icon icon)
	{
		buffer.writeString(icon.toString(), Short.MAX_VALUE);
	}

	public static Icon readIcon(PacketBuffer buffer)
	{
		return Icon.getIcon(buffer.readString(Short.MAX_VALUE));
	}
}