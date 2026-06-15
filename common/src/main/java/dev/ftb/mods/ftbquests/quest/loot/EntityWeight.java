package dev.ftb.mods.ftbquests.quest.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;

public class EntityWeight {
	public static final Codec<EntityWeight> CODEC = RecordCodecBuilder.create(builder -> builder.group(
		Codec.INT.fieldOf("passive").forGetter(e -> e.passive),
		Codec.INT.fieldOf("monster").forGetter(e -> e.monster),
		Codec.INT.fieldOf("boss").forGetter(e -> e.boss)
	).apply(builder, EntityWeight::new));
	public static final StreamCodec<FriendlyByteBuf, EntityWeight> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, e -> e.passive,
			ByteBufCodecs.VAR_INT, e -> e.monster,
			ByteBufCodecs.VAR_INT, e -> e.boss,
			EntityWeight::new
	);

	public int passive;
	public int monster;
	public int boss;

	public EntityWeight(int passive, int monster, int boss) {
        this.passive = passive;
        this.monster = monster;
        this.boss = boss;
    }

    public static EntityWeight zero() {
        return new EntityWeight(0, 0, 0);
    }

	public int getWeight(Entity entity) {
		if (!entity.canUsePortal(false)) {
			return boss;
		} else if (entity instanceof Enemy) {
			return monster;
		}

		return passive;
	}

//	public void writeData(Json5Object json) {
//		json.addProperty("passive", passive);
//		json.addProperty("monster", monster);
//		json.addProperty("boss", boss);
//	}
//
//	public void readData(Json5Object json) {
//		passive = Json5Util.getInt(json, "passive").orElseThrow();
//		monster = Json5Util.getInt(json, "monster").orElseThrow();
//		boss = Json5Util.getInt(json,"boss").orElseThrow();
//	}
//
//	public void writeNetData(FriendlyByteBuf data) {
//		data.writeVarInt(passive);
//		data.writeVarInt(monster);
//		data.writeVarInt(boss);
//	}
//
//	public void readNetData(FriendlyByteBuf data) {
//		passive = data.readVarInt();
//		monster = data.readVarInt();
//		boss = data.readVarInt();
//	}
}
