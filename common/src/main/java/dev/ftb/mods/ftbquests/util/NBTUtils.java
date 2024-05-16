package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftbquests.item.MissingItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class NBTUtils {
	public static ItemStack read(CompoundTag nbt, String key, HolderLookup.Provider provider) {
		Tag nbt1 = nbt.get(key);

		if (nbt1 instanceof CompoundTag) {
			return MissingItem.readItem((CompoundTag) nbt1, provider);
		} else if (nbt1 instanceof StringTag) {
			CompoundTag nbt2 = new CompoundTag();
			nbt2.putString("id", nbt1.getAsString());
			nbt2.putByte("Count", (byte) 1);
			return MissingItem.readItem(nbt2, provider);
		}

		return ItemStack.EMPTY;
	}

	public static void write(CompoundTag nbt, String key, ItemStack stack, HolderLookup.Provider provider) {
		if (!stack.isEmpty()) {
			CompoundTag nbt1 = MissingItem.writeItem(stack, provider);

			if (nbt1.size() == 2 && nbt1.getInt("Count") == 1) {
				nbt.putString(key, nbt1.getString("id"));
			} else {
				nbt.put(key, nbt1);
			}
		}
	}
}