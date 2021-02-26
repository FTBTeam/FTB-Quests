package com.feed_the_beast.ftbquests.util;

import net.minecraft.nbt.CompoundTag;

import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * @author LatvianModder
 */
public class OrderedCompoundTag extends CompoundTag {
	public HashSet<String> booleanKeys;

	public OrderedCompoundTag() {
		super(new LinkedHashMap<>());
	}

	@Override
	public void putBoolean(String key, boolean value) {
		if (booleanKeys == null) {
			booleanKeys = new HashSet<>();
		}

		booleanKeys.add(key);
		super.putBoolean(key, value);
	}
}
