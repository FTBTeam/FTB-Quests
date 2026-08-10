package dev.ftb.mods.ftbquests.util;

import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;
import java.util.Optional;
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

	public static Json5Object jsonObjectFromOptionalElement(Optional<Json5Element> element) {
		return element
				.filter(Json5Element::isJson5Object)
				.map(Json5Element::getAsJson5Object)
				.orElseGet(Json5Object::new);
	}

    public static void sendToQuestBookEditors(MinecraftServer server, CustomPacketPayload packet) {
        var editors = server.getPlayerList().getPlayers().stream()
                .filter(PermissionsHelper::canPlayerEdit)
                .toList();
        Server2PlayNetworking.send(editors, packet);
    }
}
