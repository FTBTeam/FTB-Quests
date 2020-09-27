package com.feed_the_beast.ftbquests.util;

import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.INBTType;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.io.DataOutput;
import java.io.IOException;

/**
 * @author LatvianModder
 */
public class BooleanSNBT extends NumberNBT
{
	public static final BooleanSNBT TRUE = new BooleanSNBT(true);
	public static final BooleanSNBT FALSE = new BooleanSNBT(false);

	public final boolean value;

	private BooleanSNBT(boolean v)
	{
		value = v;
	}

	@Override
	public void write(DataOutput output) throws IOException
	{
		output.writeByte(value ? 1 : 0);
	}

	@Override
	public byte getId()
	{
		return 1;
	}

	@Override
	public INBTType<?> getType()
	{
		return ByteNBT.TYPE;
	}

	@Override
	public String toString()
	{
		return value ? "true" : "false";
	}

	@Override
	public INBT copy()
	{
		return this;
	}

	@Override
	public ITextComponent toFormattedComponent(String indentation, int indentDepth)
	{
		return new StringTextComponent(toString()).applyTextStyle(SYNTAX_HIGHLIGHTING_NUMBER);
	}

	@Override
	public long getLong()
	{
		return value ? 1L : 0L;
	}

	@Override
	public int getInt()
	{
		return value ? 1 : 0;
	}

	@Override
	public short getShort()
	{
		return value ? (short) 1 : (short) 0;
	}

	@Override
	public byte getByte()
	{
		return value ? (byte) 1 : (byte) 0;
	}

	@Override
	public double getDouble()
	{
		return value ? 1D : 0D;
	}

	@Override
	public float getFloat()
	{
		return value ? 1F : 0F;
	}

	@Override
	public Number getAsNumber()
	{
		return value ? (byte) 1 : (byte) 0;
	}
}
