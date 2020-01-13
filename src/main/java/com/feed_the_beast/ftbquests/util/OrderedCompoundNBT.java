package com.feed_the_beast.ftbquests.util;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.INBTType;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class OrderedCompoundNBT extends CompoundNBT
{
	//TODO: public-f net.minecraft.nbt.CompoundNBT field_74784_a #tagMap
	public final LinkedHashMap<String, INBT> tagMap;

	public OrderedCompoundNBT()
	{
		tagMap = new LinkedHashMap<>();
	}

	@Override
	public Set<String> keySet()
	{
		return tagMap.keySet();
	}

	@Override
	public byte getId()
	{
		return 10;
	}

	@Override
	public int size()
	{
		return tagMap.size();
	}

	@Override
	@Nullable
	public INBT put(String key, INBT value)
	{
		if (value == null)
		{
			throw new IllegalArgumentException("Invalid null NBT value with key " + key);
		}
		return tagMap.put(key, value);
	}

	@Override
	public void putByte(String key, byte value)
	{
		tagMap.put(key, ByteNBT.of(value));
	}

	@Override
	public void putShort(String key, short value)
	{
		tagMap.put(key, ShortNBT.of(value));
	}

	@Override
	public void putInt(String key, int value)
	{
		tagMap.put(key, IntNBT.of(value));
	}

	@Override
	public void putLong(String key, long value)
	{
		tagMap.put(key, LongNBT.of(value));
	}

	@Override
	public void putUniqueId(String key, UUID value)
	{
		putLong(key + "Most", value.getMostSignificantBits());
		putLong(key + "Least", value.getLeastSignificantBits());
	}

	@Override
	public UUID getUniqueId(String key)
	{
		return new UUID(getLong(key + "Most"), getLong(key + "Least"));
	}

	@Override
	public boolean hasUniqueId(String key)
	{
		return contains(key + "Most", 99) && contains(key + "Least", 99);
	}

	@Override
	public void putFloat(String key, float value)
	{
		tagMap.put(key, FloatNBT.of(value));
	}

	@Override
	public void putDouble(String key, double value)
	{
		tagMap.put(key, DoubleNBT.of(value));
	}

	@Override
	public void putString(String key, String value)
	{
		tagMap.put(key, StringNBT.of(value));
	}

	@Override
	public void putByteArray(String key, byte[] value)
	{
		tagMap.put(key, new ByteArrayNBT(value));
	}

	@Override
	public void putIntArray(String key, int[] value)
	{
		tagMap.put(key, new IntArrayNBT(value));
	}

	@Override
	public void putIntArray(String key, List<Integer> value)
	{
		tagMap.put(key, new IntArrayNBT(value));
	}

	@Override
	public void putLongArray(String key, long[] value)
	{
		tagMap.put(key, new LongArrayNBT(value));
	}

	@Override
	public void putLongArray(String key, List<Long> value)
	{
		tagMap.put(key, new LongArrayNBT(value));
	}

	@Override
	public void putBoolean(String key, boolean value)
	{
		putByte(key, (byte) (value ? 1 : 0));
	}

	@Override
	@Nullable
	public INBT get(String key)
	{
		return tagMap.get(key);
	}

	@Override
	public byte getTagId(String key)
	{
		INBT inbt = tagMap.get(key);
		return inbt == null ? 0 : inbt.getId();
	}

	@Override
	public boolean contains(String key)
	{
		return tagMap.containsKey(key);
	}

	@Override
	public boolean contains(String key, int type)
	{
		int i = getTagId(key);
		if (i == type)
		{
			return true;
		}
		else if (type != 99)
		{
			return false;
		}
		else
		{
			return i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6;
		}
	}

	@Override
	public byte getByte(String key)
	{
		try
		{
			if (contains(key, 99))
			{
				return ((NumberNBT) tagMap.get(key)).getByte();
			}
		}
		catch (ClassCastException var3)
		{
		}

		return 0;
	}

	@Override
	public short getShort(String key)
	{
		try
		{
			if (contains(key, 99))
			{
				return ((NumberNBT) tagMap.get(key)).getShort();
			}
		}
		catch (ClassCastException var3)
		{
		}

		return 0;
	}

	@Override
	public int getInt(String key)
	{
		try
		{
			if (contains(key, 99))
			{
				return ((NumberNBT) tagMap.get(key)).getInt();
			}
		}
		catch (ClassCastException var3)
		{
		}

		return 0;
	}

	@Override
	public long getLong(String key)
	{
		try
		{
			if (contains(key, 99))
			{
				return ((NumberNBT) tagMap.get(key)).getLong();
			}
		}
		catch (ClassCastException var3)
		{
		}

		return 0L;
	}

	@Override
	public float getFloat(String key)
	{
		try
		{
			if (contains(key, 99))
			{
				return ((NumberNBT) tagMap.get(key)).getFloat();
			}
		}
		catch (ClassCastException var3)
		{
		}

		return 0.0F;
	}

	@Override
	public double getDouble(String key)
	{
		try
		{
			if (contains(key, 99))
			{
				return ((NumberNBT) tagMap.get(key)).getDouble();
			}
		}
		catch (ClassCastException var3)
		{
		}

		return 0.0D;
	}

	@Override
	public String getString(String key)
	{
		try
		{
			if (contains(key, 8))
			{
				return tagMap.get(key).getString();
			}
		}
		catch (ClassCastException var3)
		{
		}

		return "";
	}

	@Override
	public byte[] getByteArray(String key)
	{
		try
		{
			if (contains(key, 7))
			{
				return ((ByteArrayNBT) tagMap.get(key)).getByteArray();
			}
		}
		catch (ClassCastException classcastexception)
		{
			throw new ReportedException(createCrashReport(key, ByteArrayNBT.READER, classcastexception));
		}

		return new byte[0];
	}

	@Override
	public int[] getIntArray(String key)
	{
		try
		{
			if (contains(key, 11))
			{
				return ((IntArrayNBT) tagMap.get(key)).getIntArray();
			}
		}
		catch (ClassCastException classcastexception)
		{
			throw new ReportedException(createCrashReport(key, IntArrayNBT.READER, classcastexception));
		}

		return new int[0];
	}

	@Override
	public long[] getLongArray(String key)
	{
		try
		{
			if (contains(key, 12))
			{
				return ((LongArrayNBT) tagMap.get(key)).getAsLongArray();
			}
		}
		catch (ClassCastException classcastexception)
		{
			throw new ReportedException(createCrashReport(key, LongArrayNBT.READER, classcastexception));
		}

		return new long[0];
	}

	@Override
	public CompoundNBT getCompound(String key)
	{
		try
		{
			if (contains(key, 10))
			{
				return (CompoundNBT) tagMap.get(key);
			}
		}
		catch (ClassCastException classcastexception)
		{
			throw new ReportedException(createCrashReport(key, READER, classcastexception));
		}

		return new CompoundNBT();
	}

	@Override
	public ListNBT getList(String key, int type)
	{
		try
		{
			if (getTagId(key) == 9)
			{
				ListNBT listnbt = (ListNBT) tagMap.get(key);
				if (!listnbt.isEmpty() && listnbt.getTagType() != type)
				{
					return new ListNBT();
				}

				return listnbt;
			}
		}
		catch (ClassCastException classcastexception)
		{
			throw new ReportedException(createCrashReport(key, ListNBT.READER, classcastexception));
		}

		return new ListNBT();
	}

	@Override
	public boolean getBoolean(String key)
	{
		return getByte(key) != 0;
	}

	@Override
	public void remove(String key)
	{
		tagMap.remove(key);
	}

	@Override
	public boolean isEmpty()
	{
		return tagMap.isEmpty();
	}

	private CrashReport createCrashReport(String key, INBTType<?> expectedType, ClassCastException ex)
	{
		CrashReport crashreport = CrashReport.makeCrashReport(ex, "Reading NBT data");
		CrashReportCategory crashreportcategory = crashreport.makeCategoryDepth("Corrupt NBT tag", 1);
		crashreportcategory.addDetail("Tag type found", () -> {
			return this.tagMap.get(key).getReader().getCrashReportName();
		});
		crashreportcategory.addDetail("Tag type expected", expectedType::getCrashReportName);
		crashreportcategory.addDetail("Tag name", key);
		return crashreport;
	}

	@Override
	public CompoundNBT copy()
	{
		CompoundNBT compoundnbt = new CompoundNBT();

		for (String s : tagMap.keySet())
		{
			compoundnbt.put(s, tagMap.get(s).copy());
		}

		return compoundnbt;
	}

	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}

		return o instanceof OrderedCompoundNBT && Objects.equals(tagMap, ((OrderedCompoundNBT) o).tagMap);
	}

	public int hashCode()
	{
		return tagMap.hashCode();
	}
}
