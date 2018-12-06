package com.feed_the_beast.ftbquests.quest.task.filter;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class ItemFilterRegistry
{
	public static class MatcherEntry
	{
		public final Predicate<NBTTagCompound> predicate;
		public final Supplier<ItemFilter> supplier;

		private MatcherEntry(Predicate<NBTTagCompound> p, Supplier<ItemFilter> s)
		{
			predicate = p;
			supplier = s;
		}
	}

	private static final ArrayList<MatcherEntry> ENTRIES = new ArrayList<>();
	public static final List<MatcherEntry> ENTRY_LIST = Collections.unmodifiableList(ENTRIES);

	public static void register(Predicate<NBTTagCompound> predicate, Supplier<ItemFilter> supplier)
	{
		ENTRIES.add(new MatcherEntry(predicate, supplier));
	}

	static
	{
		register(nbt -> nbt.hasKey("ore"), OreNameFilter::new);
		register(nbt -> nbt.hasKey("and"), AndFilter::new);
		register(nbt -> nbt.hasKey("or"), OrFilter::new);
	}

	public static ItemFilter createFilter(@Nullable NBTBase nbt)
	{
		if (nbt == null || nbt.isEmpty())
		{
			return new AndFilter();
		}

		if (nbt instanceof NBTTagCompound)
		{
			NBTTagCompound nbt1 = (NBTTagCompound) nbt;

			for (MatcherEntry entry : ENTRIES)
			{
				if (entry.predicate.test(nbt1))
				{
					ItemFilter matcher = entry.supplier.get();
					matcher.fromNBT(nbt);

					if (matcher.isValid())
					{
						return matcher;
					}
				}
			}
		}

		ItemStackFilter matcher = new ItemStackFilter();
		matcher.fromNBT(nbt);
		return matcher.isValid() ? matcher : new AndFilter();
	}
}