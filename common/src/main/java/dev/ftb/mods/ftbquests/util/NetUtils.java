package dev.ftb.mods.ftbquests.util;

import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class NetUtils {
	public static boolean canEdit(PacketContext context) {
		return canEdit(context.player());
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

	public static Json5Object jsonObjectFromOptionalElement(Optional<Json5Element> element) {
		return element
				.filter(Json5Element::isJson5Object)
				.map(Json5Element::getAsJson5Object)
				.orElseGet(Json5Object::new);
	}
}
