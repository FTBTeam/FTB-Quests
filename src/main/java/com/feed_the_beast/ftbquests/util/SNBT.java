package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagString;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class SNBT
{
	private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
	private static final Pattern BYTE_ARRAY_MATCHER = Pattern.compile("\\[B;([\\s\\d,b]*)\\]", Pattern.CASE_INSENSITIVE);
	private static final Pattern LONG_ARRAY_MATCHER = Pattern.compile("\\[L;([\\s\\d,l]*)\\]", Pattern.CASE_INSENSITIVE);

	public static String handleEscape(String string)
	{
		return SIMPLE_VALUE.matcher(string).matches() ? string : NBTTagString.quoteAndEscape(string);
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
	public static NBTTagCompound readOrTransform(File dir, String filename)
	{
		File snbtFile = new File(dir, filename + ".snbt");
		File nbtFile = new File(dir, filename + ".nbt");

		NBTTagCompound nbt = read(snbtFile);

		if (nbt != null)
		{
			if (nbtFile.exists())
			{
				nbtFile.delete();
			}

			return nbt;
		}

		nbt = NBTUtils.readNBT(nbtFile);

		if (nbt != null && nbtFile.delete())
		{
			write(snbtFile, nbt);
		}

		return nbt;
	}

	@Nullable
	public static NBTTagCompound read(File file)
	{
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

			String s1 = s.toString();

			{
				StringBuffer sb = new StringBuffer(s1.length());
				Matcher matcher = BYTE_ARRAY_MATCHER.matcher(s1);

				while (matcher.find())
				{
					String s2 = matcher.group(1);

					if (!s2.isEmpty())
					{
						String[] s3 = s2.split(",");

						for (int i = 0; i < s3.length; i++)
						{
							if (!s3[i].endsWith("b") && !s3[i].endsWith("B"))
							{
								s3[i] += 'b';
							}
						}

						matcher.appendReplacement(sb, "[B;" + String.join(",", s3) + "]");
					}
					else
					{
						matcher.appendReplacement(sb, "[B;]");
					}
				}

				matcher.appendTail(sb);
				s1 = sb.toString();
			}

			{
				StringBuffer sb = new StringBuffer(s1.length());
				Matcher matcher = LONG_ARRAY_MATCHER.matcher(s1);

				while (matcher.find())
				{
					String s2 = matcher.group(1);

					if (!s2.isEmpty())
					{
						String[] s3 = s2.split(",");

						for (int i = 0; i < s3.length; i++)
						{
							if (!s3[i].endsWith("l") && !s3[i].endsWith("L"))
							{
								s3[i] += 'L';
							}
						}

						matcher.appendReplacement(sb, "[L;" + String.join(",", s3) + "]");
					}
					else
					{
						matcher.appendReplacement(sb, "[L;]");
					}
				}

				matcher.appendTail(sb);
				s1 = sb.toString();
			}

			return JsonToNBT.getTagFromJson(s1);
		}
		catch (NBTException ex)
		{
			FTBQuests.LOGGER.error("Failed to read " + file.getAbsolutePath() + ": " + ex);
		}
		catch (Exception ex)
		{
		}

		return null;
	}

	public static void write(File file, NBTTagCompound out)
	{
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
			append(builder, out, false);
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

	private static void append(SNBTBuilder builder, @Nullable NBTBase nbt, boolean inList)
	{
		if (nbt == null || nbt instanceof NBTTagEnd)
		{
			builder.print("null");
		}
		else if (nbt instanceof NBTTagCompound)
		{
			NBTTagCompound compound = (NBTTagCompound) nbt;

			if (compound.isEmpty())
			{
				builder.print("{}");
				return;
			}

			builder.print("{");
			builder.println();
			builder.push();
			int index = 0;

			for (String key : compound.getKeySet())
			{
				index++;
				builder.print(handleEscape(key));
				builder.print(": ");
				append(builder, compound.getTag(key), false);

				if (index != compound.getSize())
				{
					builder.print(",");
				}

				builder.println();
			}

			builder.pop();
			builder.print("}");
		}
		else if (nbt instanceof NBTTagList)
		{
			appendCollection(builder, (NBTTagList) nbt, "");
		}
		else if (nbt instanceof NBTTagByteArray)
		{
			NBTTagList list = new NBTTagList();

			for (byte i : ((NBTTagByteArray) nbt).getByteArray())
			{
				list.appendTag(new NBTTagByte(i));
			}

			appendCollection(builder, list, "B;");
		}
		else if (nbt instanceof NBTTagIntArray)
		{
			NBTTagList list = new NBTTagList();

			for (int i : ((NBTTagIntArray) nbt).getIntArray())
			{
				list.appendTag(new NBTTagInt(i));
			}

			appendCollection(builder, list, "I;");
		}
		else if (nbt instanceof NBTTagLongArray)
		{
			appendCollection(builder, new NBTTagList(), "L;");
		}
		else if (nbt instanceof NBTPrimitive && !inList)
		{
			if (nbt instanceof NBTTagFloat)
			{
				builder.print(nbt.toString());
			}
			else if (nbt instanceof NBTTagDouble)
			{
				builder.print(nbt.toString());
			}
			else
			{
				long v = ((NBTPrimitive) nbt).getLong();

				if (v <= Integer.MAX_VALUE && v >= Integer.MIN_VALUE)
				{
					builder.print(v);
				}
				else
				{
					builder.print(v);
					builder.print("L");
				}
			}
		}
		else
		{
			builder.print(nbt.toString());
		}
	}

	private static void appendCollection(SNBTBuilder builder, NBTTagList nbt, String opening)
	{
		if (nbt.isEmpty())
		{
			builder.print("[");
			builder.print(opening);
			builder.print("]");
			return;
		}
		else if (nbt.tagCount() == 1)
		{
			builder.print("[");
			builder.print(opening);
			append(builder, nbt.get(0), true);
			builder.print("]");
			return;
		}

		builder.print("[");
		builder.print(opening);
		builder.println();
		builder.push();
		int index = 0;

		for (NBTBase value : nbt)
		{
			index++;
			append(builder, value, true);

			if (index != nbt.tagCount())
			{
				builder.print(",");
			}

			builder.println();
		}

		builder.pop();
		builder.print("]");
	}
}
