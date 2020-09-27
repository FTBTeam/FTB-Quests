package com.feed_the_beast.ftbquests.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CollectionNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.EndNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
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
			ItemStack stack = ItemStack.read((CompoundNBT) nbt1);

			if (!stack.isEmpty())
			{
				return stack;
			}
		}
		else if (nbt1 instanceof StringNBT)
		{
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt1.getString()));

			if (item != null && item != Items.AIR)
			{
				return new ItemStack(item);
			}
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
	public static CompoundNBT readSNBT(Path base, @Nullable String path)
	{
		if (path == null || path.isEmpty())
		{
			return null;
		}

		return readSNBT(base.resolve(path + ".snbt"));
	}

	@Nullable
	public static CompoundNBT readSNBT(Path path)
	{
		File file = path.toFile();

		if (!file.exists())
		{
			return null;
		}

		StringBuilder s = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String line;

			while ((line = reader.readLine()) != null)
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

	public static void writeSNBT(Path base, String path, CompoundNBT out)
	{
		File file = base.resolve(path + ".snbt").toFile();

		if (!file.exists())
		{
			File p = file.getParentFile();

			if (!p.exists())
			{
				p.mkdirs();
			}

			try
			{
				file.createNewFile();
			}
			catch (Exception ex)
			{
			}
		}

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file))))
		{
			SNBTBuilder builder = new SNBTBuilder();
			append(builder, out);
			builder.println();

			for (String s : builder.lines)
			{
				writer.println(s);
			}
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

	public static void putVarLong(CompoundNBT nbt, String key, long value)
	{
		if (value <= Byte.MAX_VALUE)
		{
			nbt.putByte(key, (byte) value);
		}
		else if (value <= Short.MAX_VALUE)
		{
			nbt.putShort(key, (short) value);
		}
		else if (value <= Integer.MAX_VALUE)
		{
			nbt.putInt(key, (int) value);
		}
		else
		{
			nbt.putLong(key, value);
		}
	}
}