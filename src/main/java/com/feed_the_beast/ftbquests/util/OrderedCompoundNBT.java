package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftbquests.core.ByteNBTFTBQ;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;

import java.util.LinkedHashMap;

/**
 * @author LatvianModder
 */
public class OrderedCompoundNBT extends CompoundNBT
{
	public OrderedCompoundNBT()
	{
		super(new LinkedHashMap<>());
	}

	@Override
	public void putBoolean(String key, boolean value)
	{
		ByteNBT byteNBT = ByteNBT.valueOf(value);

		if (byteNBT instanceof ByteNBTFTBQ)
		{
			((ByteNBTFTBQ) byteNBT).setBooleanFTBQ();
		}

		put(key, byteNBT);
	}
}
