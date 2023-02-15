package dev.ftb.mods.ftbquests.util;

import com.google.common.collect.Sets;
import dev.ftb.mods.ftbquests.item.MissingItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.IntStream;

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

	/**
	 * Quite like the vanilla {@link net.minecraft.nbt.NbtUtils#compareNbt(Tag, Tag, boolean)} but also has the option
	 * for fuzzy checking. Order of the tags matters! Supply the tag being checked against first (e.g. the filter),
	 * and the tag being checked (e.g. the actual item's NBT) second.
	 *
	 * @param tagA tag to compare against
	 * @param tagB tag being tested
	 * @param fuzzy if true, fuzzy match for compound tags: fields in compoundTagB but not in compoundTagA don't cause a
	 *                match failure
	 * @param compareLists if true, recursively deep-compare lists (otherwise simple .equals() check)
	 * @return true if there's a match, false otherwise
	 */
	public static boolean compareNbt(@Nullable Tag tagA, @Nullable Tag tagB, boolean fuzzy, boolean compareLists) {
		if (tagA == tagB) {
			return true;
		} else if (tagA == null) {
			return true;
		} else if (tagB == null) {
			return false;
		} else if (!tagA.getClass().equals(tagB.getClass())) {
			return false;
		} else if (tagA instanceof CompoundTag compoundA) {
			CompoundTag compoundB = (CompoundTag) tagB; // safe cast due to previous class comparison
			Set<String> keysA = compoundA.getAllKeys();
			Set<String> keysB = compoundB.getAllKeys();
			if (!fuzzy) {
				if (keysA.size() != keysB.size() || Sets.intersection(keysA, keysB).size() != keysA.size()) {
					return false;
				}
			}
			return keysA.stream().allMatch(key -> compareNbt(compoundA.get(key), compoundB.get(key), fuzzy, compareLists));
		} else if (tagA instanceof ListTag listA && compareLists) {
			ListTag listB = (ListTag) tagB;
			if (listA.isEmpty()) {
				return listB.isEmpty();
			} else if (listA.size() != listB.size()) {
				return false;
			} else {
				return IntStream.range(0, listA.size()).allMatch(i -> compareNbt(listA.get(i), listB.get(i), fuzzy, true));
			}
		} else {
			return tagA.equals(tagB);
		}
	}
}