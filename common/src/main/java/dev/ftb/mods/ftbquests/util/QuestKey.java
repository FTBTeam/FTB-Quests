package dev.ftb.mods.ftbquests.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.util.UndashedUuid;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record QuestKey(UUID uuid, long id) implements Comparable<QuestKey> {
	public static final Codec<QuestKey> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(QuestKey::uuid),
			Codec.LONG.fieldOf("id").forGetter(QuestKey::id)
	).apply(builder, QuestKey::new));
	public static final StreamCodec<FriendlyByteBuf, QuestKey> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, QuestKey::uuid,
			ByteBufCodecs.LONG, QuestKey::id,
			QuestKey::new
	);

	public static QuestKey create(UUID uuid, long questObjectId) {
		return new QuestKey(uuid, questObjectId);
	}

	public static QuestKey forReward(UUID uuid, Reward reward) {
		return create(reward.isTeamReward() ? Util.NIL_UUID : uuid, reward.id);
	}

	public static QuestKey fromNetwork(FriendlyByteBuf buf) {
		return create(buf.readUUID(), buf.readLong());
	}

	public static QuestKey fromString(String string) {
		return create(UndashedUuid.fromString(string.substring(0, 32)), QuestObjectBase.parseCodeString(string.substring(33)));
	}

	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		return UndashedUuid.toString(uuid) + ":" + QuestObjectBase.getCodeString(id);
	}

	@Override
	public int compareTo(@NotNull QuestKey key) {
		int i = uuid.compareTo(key.uuid);
		return i == 0 ? Long.compareUnsigned(id, key.id) : i;
	}

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeUUID(uuid);
		buf.writeLong(id);
	}
}
