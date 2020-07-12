package com.feed_the_beast.ftbquests.util;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * @author LatvianModder
 */
public class SNBTTagCompound extends NBTTagCompound
{
	public static final NBTTagByte TRUE = new NBTTagByte((byte) 1);
	public static final NBTTagByte FALSE = new NBTTagByte((byte) 0);

	private static Optional<Field> tagMapField;

	public SNBTTagCompound()
	{
		if (tagMapField == null)
		{
			tagMapField = Optional.empty();

			try
			{
				Field field = ObfuscationReflectionHelper.findField(NBTTagCompound.class, "field_74784_a");
				field.setAccessible(true);
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
				tagMapField = Optional.of(field);
			}
			catch (Throwable ex)
			{
			}
		}

		if (tagMapField.isPresent())
		{
			try
			{
				tagMapField.get().set(this, new LinkedHashMap<>());
			}
			catch (Throwable ex)
			{
			}
		}
	}

	@Override
	public void setBoolean(String key, boolean value)
	{
		super.setTag(key, value ? TRUE : FALSE);
	}
}