package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

import java.util.Collections;
import java.util.Map;

/**
 * @author LatvianModder
 */
public final class QuestObjectText
{
	public static final QuestObjectText NONE = new QuestObjectText(Collections.emptyMap());

	private final Map<String, String[]> text;

	public QuestObjectText(Map<String, String[]> t)
	{
		text = t;
	}

	public String[] getStringArray(String key)
	{
		String[] array = text.get(key);
		return array == null ? StringUtils.EMPTY_ARRAY : array;
	}

	public String getString(String key)
	{
		String[] array = getStringArray(key);
		return array.length == 0 ? "" : array[0];
	}

	public MutableComponent getComponent(String key)
	{
		String s = getString(key);
		return s.isEmpty() ? (MutableComponent) TextComponent.EMPTY : new TextComponent(s);
	}

	public MutableComponent[] getComponentArray(String key)
	{
		// FIXME
		String[] s = getStringArray(key);
		MutableComponent[] a = new MutableComponent[s.length];

		for (int i = 0; i < s.length; i++)
		{
			a[i] = new TextComponent(s[i]);
		}

		return a;
	}
}