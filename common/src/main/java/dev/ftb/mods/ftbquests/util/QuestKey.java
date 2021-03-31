package dev.ftb.mods.ftbquests.util;

import com.mojang.util.UUIDTypeAdapter;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class QuestKey implements Comparable<QuestKey> {
	public final UUID uuid;
	public final long id;

	public static QuestKey of(UUID uuid, long reward) {
		return new QuestKey(uuid, reward);
	}

	public static QuestKey of(FriendlyByteBuf buf) {
		return of(buf.readUUID(), buf.readLong());
	}

	private QuestKey(UUID i, long r) {
		uuid = i;
		id = r;
	}

	@Override
	public String toString() {
		return UUIDTypeAdapter.fromUUID(uuid) + "#" + QuestObjectBase.getCodeString(id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		QuestKey claimKey = (QuestKey) o;
		return id == claimKey.id && Objects.equals(uuid, claimKey.uuid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uuid, id);
	}

	@Override
	public int compareTo(@NotNull QuestKey key) {
		int i = uuid.compareTo(key.uuid);
		return i == 0 ? Long.compareUnsigned(id, key.id) : i;
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeUUID(uuid);
		buf.writeLong(id);
	}
}
