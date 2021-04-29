package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftbquests.item.MissingItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class NBTUtils {
	public static ItemStack read(CompoundTag nbt, String key) {
		Tag nbt1 = nbt.get(key);

		if (nbt1 instanceof CompoundTag) {
			return MissingItem.readItem((CompoundTag) nbt1);
		} else if (nbt1 instanceof StringTag) {
			CompoundTag nbt2 = new CompoundTag();
			nbt2.putString("id", nbt1.getAsString());
			nbt2.putByte("Count", (byte) 1);
			return MissingItem.readItem(nbt2);
		}

		return ItemStack.EMPTY;
	}

	public static void write(CompoundTag nbt, String key, ItemStack stack) {
		if (!stack.isEmpty()) {
			CompoundTag nbt1 = MissingItem.writeItem(stack);

			if (nbt1.size() == 2 && nbt1.getInt("Count") == 1) {
				nbt.putString(key, nbt1.getString("id"));
			} else {
				nbt.put(key, nbt1);
			}
		}
	}
}