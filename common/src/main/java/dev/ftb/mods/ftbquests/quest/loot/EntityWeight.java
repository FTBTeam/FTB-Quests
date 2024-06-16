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
		// NOTE: (mikey | porting to 1.21): This looks right but it could be wrong. This is what the nether portal changed to using for the same check
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
		passive = nbt.getInt("passive");
		monster = nbt.getInt("monster");
		boss = nbt.getInt("boss");
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
