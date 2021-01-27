package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftbquests.core.ByteNBTFTBQ;
import java.util.LinkedHashMap;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;

/**
 * @author LatvianModder
 */
public class OrderedCompoundNBT extends CompoundTag
{
	public OrderedCompoundNBT()
	{
		super(new LinkedHashMap<>());
	}

	@Override
	public void putBoolean(String key, boolean value)
	{
		ByteTag byteNBT = ByteTag.valueOf(value);

		if (byteNBT instanceof ByteNBTFTBQ)
		{
			((ByteNBTFTBQ) byteNBT).setBooleanFTBQ();
		}

		put(key, byteNBT);
	}
}
