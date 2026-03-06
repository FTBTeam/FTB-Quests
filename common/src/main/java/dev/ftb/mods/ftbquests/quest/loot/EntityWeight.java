package dev.ftb.mods.ftbquests.quest.loot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;

public class EntityWeight {
	public int passive = 0;
	public int monster = 0;
	public int boss = 0;

	public int getWeight(Entity entity) {
		if (!entity.canUsePortal(false)) {
			return boss;
		} else if (entity instanceof Enemy) {
			return monster;
		}

		return passive;
	}

	public void writeData(CompoundTag nbt) {
		nbt.putInt("passive", passive);
		nbt.putInt("monster", monster);
		nbt.putInt("boss", boss);
	}

	public void readData(CompoundTag nbt) {
		passive = nbt.getInt("passive").orElseThrow();
		monster = nbt.getInt("monster").orElseThrow();
		boss = nbt.getInt("boss").orElseThrow();
	}

	public void writeNetData(FriendlyByteBuf data) {
		data.writeVarInt(passive);
		data.writeVarInt(monster);
		data.writeVarInt(boss);
	}

	public void readNetData(FriendlyByteBuf data) {
		passive = data.readVarInt();
		monster = data.readVarInt();
		boss = data.readVarInt();
	}
}
