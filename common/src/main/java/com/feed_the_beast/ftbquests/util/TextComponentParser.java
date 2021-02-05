package com.feed_the_beast.ftbquests.util;

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * @author LatvianModder
 */
public class TextComponentParser
{
	public static final Char2ObjectOpenHashMap<ChatFormatting> CODE_TO_FORMATTING = new Char2ObjectOpenHashMap<>();

	static
	{
		CODE_TO_FORMATTING.put('0', ChatFormatting.BLACK);
		CODE_TO_FORMATTING.put('1', ChatFormatting.DARK_BLUE);
		CODE_TO_FORMATTING.put('2', ChatFormatting.DARK_GREEN);
		CODE_TO_FORMATTING.put('3', ChatFormatting.DARK_AQUA);
		CODE_TO_FORMATTING.put('4', ChatFormatting.DARK_RED);
		CODE_TO_FORMATTING.put('5', ChatFormatting.DARK_PURPLE);
		CODE_TO_FORMATTING.put('6', ChatFormatting.GOLD);
		CODE_TO_FORMATTING.put('7', ChatFormatting.GRAY);
		CODE_TO_FORMATTING.put('8', ChatFormatting.DARK_GRAY);
		CODE_TO_FORMATTING.put('9', ChatFormatting.BLUE);
		CODE_TO_FORMATTING.put('a', ChatFormatting.GREEN);
		CODE_TO_FORMATTING.put('b', ChatFormatting.AQUA);
		CODE_TO_FORMATTING.put('c', ChatFormatting.RED);
		CODE_TO_FORMATTING.put('d', ChatFormatting.LIGHT_PURPLE);
		CODE_TO_FORMATTING.put('e', ChatFormatting.YELLOW);
		CODE_TO_FORMATTING.put('f', ChatFormatting.WHITE);
		CODE_TO_FORMATTING.put('k', ChatFormatting.OBFUSCATED);
		CODE_TO_FORMATTING.put('l', ChatFormatting.BOLD);
		CODE_TO_FORMATTING.put('m', ChatFormatting.STRIKETHROUGH);
		CODE_TO_FORMATTING.put('n', ChatFormatting.UNDERLINE);
		CODE_TO_FORMATTING.put('o', ChatFormatting.ITALIC);
		CODE_TO_FORMATTING.put('r', ChatFormatting.RESET);
	}

	public static Component parse(String text, @Nullable Function<String, Component> substitutes)
	{
		return new TextComponentParser(text, substitutes).parse();
	}

	private final String text;
	private final Function<String, Component> substitutes;

	private TextComponent component;
	private StringBuilder builder;
	private Style style;

	private TextComponentParser(String txt, @Nullable Function<String, Component> sub)
	{
		text = txt;
		substitutes = sub;
	}

	private Component parse()
	{
		if (text.isEmpty())
		{
			return TextComponent.EMPTY;
		}

		char[] c = text.toCharArray();
		boolean hasSpecialCodes = false;

		for (char c1 : c)
		{
			if (c1 == '{' || c1 == '&' || c1 == '\u00a7')
			{
				hasSpecialCodes = true;
				break;
			}
		}

		if (!hasSpecialCodes)
		{
			return new TextComponent(text);
		}

		component = new TextComponent("");
		style = Style.EMPTY;
		builder = new StringBuilder();
		boolean sub = false;

		for (int i = 0; i < c.length; i++)
		{
			boolean escape = i > 0 && c[i - 1] == '\\';
			boolean end = i == c.length - 1;

			if (sub && (end || c[i] == '{' || c[i] == '}'))
			{
				if (c[i] == '{')
				{
					throw new IllegalArgumentException("Invalid formatting! Can't nest multiple substitutes!");
				}

				finishPart();
				sub = false;
				continue;
			}

			if (!escape)
			{
				if (c[i] == '\u00a7')
				{
					c[i] = '&';
				}

				if (c[i] == '&')
				{
					finishPart();

					if (end)
					{
						throw new IllegalArgumentException("Invalid formatting! Can't end string with & or \u00a7!");
					}

					i++;

					if (c[i] == '#')
					{
						char[] rrggbb = new char[7];
						rrggbb[0] = '#';
						System.arraycopy(c, i + 1, rrggbb, 1, 6);
						i += 6;
						style = style.withColor(TextColor.parseColor(new String(rrggbb)));
					}
					else
					{
						ChatFormatting formatting = CODE_TO_FORMATTING.get(c[i]);

						if (formatting == null)
						{
							throw new IllegalArgumentException("Illegal formatting! Unknown color code character: " + c[i] + "!");
						}

						style = style.applyFormat(formatting);
					}

					continue;
				}
				else if (c[i] == '{')
				{
					finishPart();

					if (end)
					{
						throw new IllegalArgumentException("Invalid formatting! Can't end string with {!");
					}

					sub = true;
				}
			}

			if (c[i] != '\\' || escape)
			{
				builder.append(c[i]);
			}
		}

		finishPart();
		return component;
	}

	private void finishPart()
	{
		String string = builder.toString();
		builder.setLength(0);

		if (string.isEmpty())
		{
			return;
		}
		else if (string.length() < 2 || string.charAt(0) != '{')
		{
			TextComponent component1 = new TextComponent(string);
			component1.setStyle(style);
			component.append(component1);
			return;
		}

		Component component1 = substitutes.apply(string.substring(1));

		if (component1 != null)
		{
			Style style0 = component1.getStyle();
			Style style1 = style;
			style1 = style1.withHoverEvent(style0.getHoverEvent());
			style1 = style1.withClickEvent(style0.getClickEvent());
			style1 = style1.withInsertion(style0.getInsertion());
			component1 = new TextComponent("").append(component1).withStyle(style1);
		}
		else
		{
			throw new IllegalArgumentException("Invalid formatting! Unknown substitute: " + string.substring(1));
		}

		component.append(component1);
	}
}