package com.feed_the_beast.ftbquests.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CollectionNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.EndNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.StringNBT;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class NBTUtils
{
	private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");

	public static String handleEscape(String string)
	{
		return SIMPLE_VALUE.matcher(string).matches() ? string : StringNBT.quoteAndEscape(string);
	}

	public static ItemStack read(CompoundNBT nbt, String key)
	{
		INBT nbt1 = nbt.get(key);

		if (nbt1 instanceof CompoundNBT)
		{
			return ItemStack.read((CompoundNBT) nbt1);
		}
		else if (nbt1 instanceof StringNBT)
		{
			CompoundNBT nbt2 = new CompoundNBT();
			nbt2.putString("id", nbt1.getString());
			nbt2.putByte("Count", (byte) 1);
			return ItemStack.read(nbt2);
		}

		return ItemStack.EMPTY;
	}

	public static void write(CompoundNBT nbt, String key, ItemStack stack)
	{
		if (!stack.isEmpty())
		{
			CompoundNBT nbt1 = stack.serializeNBT();

			if (nbt1.size() == 2 && nbt1.getInt("Count") == 1)
			{
				nbt.putString(key, nbt1.getString("id"));
			}
			else
			{
				nbt.put(key, nbt1);
			}
		}
	}

	private static class SNBTBuilder
	{
		private String indent = "";
		private final List<String> lines = new ArrayList<>();
		private final StringBuilder line = new StringBuilder();

		private void print(Object string)
		{
			line.append(string);
		}

		private void println()
		{
			line.insert(0, indent);
			lines.add(line.toString());
			line.setLength(0);
		}

		private void push()
		{
			indent += "\t";
		}

		private void pop()
		{
			indent = indent.substring(1);
		}
	}

	@Nullable
	public static CompoundNBT readSNBT(Path path)
	{
		if (Files.notExists(path))
		{
			return null;
		}

		StringBuilder s = new StringBuilder();

		try
		{
			for (String line : Files.readAllLines(path, StandardCharsets.UTF_8))
			{
				s.append(line.trim());
			}

			return JsonToNBT.getTagFromJson(s.toString());
		}
		catch (Exception ex)
		{
		}

		return null;
	}

	public static void writeSNBT(Path path, CompoundNBT nbt)
	{
		try
		{
			if (Files.notExists(path.getParent()))
			{
				Files.createDirectories(path.getParent());
			}

			SNBTBuilder builder = new SNBTBuilder();
			append(builder, nbt);
			builder.println();
			Files.write(path, builder.lines);
		}
		catch (Exception ex)
		{
		}
	}

	private static void append(SNBTBuilder builder, @Nullable INBT nbt)
	{
		if (nbt == null || nbt instanceof EndNBT)
		{
			builder.print("null");
		}
		else if (nbt instanceof CompoundNBT)
		{
			CompoundNBT compound = (CompoundNBT) nbt;

			if (compound.isEmpty())
			{
				builder.print("{}");
				return;
			}

			builder.print("{");
			builder.println();
			builder.push();
			int index = 0;

			for (String key : compound.keySet())
			{
				index++;
				builder.print(handleEscape(key));
				builder.print(": ");
				append(builder, compound.get(key));

				if (index != compound.size())
				{
					builder.print(",");
				}

				builder.println();
			}

			builder.pop();
			builder.print("}");
		}
		else if (nbt instanceof CollectionNBT)
		{
			if (nbt instanceof ByteArrayNBT)
			{
				appendCollection(builder, (CollectionNBT<?>) nbt, "B;");
			}
			else if (nbt instanceof IntArrayNBT)
			{
				appendCollection(builder, (CollectionNBT<?>) nbt, "I;");
			}
			else if (nbt instanceof LongArrayNBT)
			{
				appendCollection(builder, (CollectionNBT<?>) nbt, "L;");
			}
			else
			{
				appendCollection(builder, (CollectionNBT<?>) nbt, "");
			}
		}
		else
		{
			builder.print(nbt.toString());
		}
	}

	private static void appendCollection(SNBTBuilder builder, CollectionNBT<? extends INBT> nbt, String opening)
	{
		if (nbt.isEmpty())
		{
			builder.print("[");
			builder.print(opening);
			builder.print("]");
			return;
		}
		else if (nbt.size() == 1)
		{
			builder.print("[");
			builder.print(opening);
			append(builder, nbt.get(0));
			builder.print("]");
			return;
		}

		builder.print("[");
		builder.print(opening);
		builder.println();
		builder.push();
		int index = 0;

		for (INBT value : nbt)
		{
			index++;
			append(builder, value);

			if (index != nbt.size())
			{
				builder.print(",");
			}

			builder.println();
		}

		builder.pop();
		builder.print("]");
	}
}