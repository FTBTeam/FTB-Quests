package com.feed_the_beast.ftbquests.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * @author LatvianModder
 */
public class CompoundSNBT extends CompoundNBT
{
	private static Optional<Field> tagMapField;

	public CompoundSNBT()
	{
		if (tagMapField == null)
		{
			tagMapField = Optional.empty();

			try
			{
				Field field = ObfuscationReflectionHelper.findField(CompoundNBT.class, "field_74784_a");
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
	public void putBoolean(String key, boolean value)
	{
		super.put(key, value ? BooleanSNBT.TRUE : BooleanSNBT.FALSE);
	}
}
