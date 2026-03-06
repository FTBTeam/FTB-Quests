package dev.ftb.mods.ftbquests.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

public class NetUtils {
	public static boolean canEdit(NetworkManager.PacketContext context) {
		return canEdit(context.getPlayer());
	}

	public static boolean canEdit(@Nullable Player player) {
		return player != null &&
				FTBQuestsAPI.api().getQuestFile(player.level().isClientSide()).getTeamData(player)
						.map(d -> d.getCanEdit(player))
						.orElse(false);
	}

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
}
