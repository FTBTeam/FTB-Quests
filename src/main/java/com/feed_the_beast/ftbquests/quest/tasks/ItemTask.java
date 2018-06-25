package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.item.ItemEntry;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class ItemTask extends QuestTask implements Predicate<ItemStack>
{
	public static abstract class QuestItem implements Predicate<ItemStack>
	{
		public static QuestItem EMPTY = new QuestItem()
		{
			@Override
			public boolean isEmpty()
			{
				return true;
			}

			@Override
			public boolean test(ItemStack stack)
			{
				return false;
			}

			@Override
			public JsonElement toJson()
			{
				return JsonNull.INSTANCE;
			}

			@Override
			public Icon getIcon()
			{
				return Icon.EMPTY;
			}

			@Override
			@SideOnly(Side.CLIENT)
			public String getDisplayName()
			{
				return "-";
			}
		};

		public static QuestItem fromJson(@Nullable JsonElement element)
		{
			if (JsonUtils.isNull(element))
			{
				return EMPTY;
			}
			else if (element.isJsonArray())
			{
				List<QuestItem> list = new ArrayList<>();

				for (JsonElement element1 : element.getAsJsonArray())
				{
					QuestItem item = fromJson(element1);

					if (!item.isEmpty())
					{
						list.add(item);
					}
				}

				if (!list.isEmpty())
				{
					return new QuestItemCombined(list);
				}
			}
			else if (element.isJsonObject())
			{
				JsonObject json = element.getAsJsonObject();

				if (json.has("item"))
				{
					ItemEntry entry = ItemEntry.fromJson(json);

					if (!entry.isEmpty())
					{
						return new QuestItemEntry(entry);
					}
				}
				else if (json.has("ore"))
				{
					QuestOreItem ore = new QuestOreItem(json.get("ore").getAsString());

					if (!ore.entries.isEmpty())
					{
						return ore;
					}
				}
			}

			return EMPTY;
		}

		public abstract boolean isEmpty();

		public abstract JsonElement toJson();

		public abstract Icon getIcon();

		@SideOnly(Side.CLIENT)
		public abstract String getDisplayName();
	}

	public static class QuestItemCombined extends QuestItem
	{
		public final List<QuestItem> items;
		public final Icon icon;

		public QuestItemCombined(List<QuestItem> c)
		{
			items = c;
			List<Icon> icons = new ArrayList<>();

			for (QuestItem item : items)
			{
				icons.add(item.getIcon());
			}

			icon = new IconAnimation(icons);
		}

		@Override
		public boolean isEmpty()
		{
			return false;
		}

		@Override
		public JsonElement toJson()
		{
			JsonArray array = new JsonArray();

			for (QuestItem item : items)
			{
				array.add(item.toJson());
			}

			return array;
		}

		@Override
		public Icon getIcon()
		{
			return icon;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public String getDisplayName()
		{
			if (items.size() == 1)
			{
				return items.get(0).getDisplayName();
			}

			String[] s = new String[items.size()];

			for (int i = 0; i < s.length; i++)
			{
				s[i] = items.get(i).getDisplayName();
			}

			return StringJoiner.with(", ").joinStrings(s);
		}

		@Override
		public boolean test(ItemStack stack)
		{
			for (QuestItem item : items)
			{
				if (item.test(stack))
				{
					return true;
				}
			}

			return false;
		}
	}

	public static class QuestItemEntry extends QuestItem
	{
		public final ItemEntry entry;
		private final Icon icon;

		public QuestItemEntry(ItemEntry e)
		{
			entry = e;
			icon = ItemIcon.getItemIcon(entry.getStack(1, false));
		}

		@Override
		public boolean isEmpty()
		{
			return entry.isEmpty();
		}

		@Override
		public JsonElement toJson()
		{
			return entry.toJson();
		}

		@Override
		public Icon getIcon()
		{
			return icon;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public String getDisplayName()
		{
			return entry.getStack(1, false).getDisplayName();
		}

		@Override
		public boolean test(ItemStack stack)
		{
			return entry.equalsEntry(ItemEntry.get(stack));
		}
	}

	public static class QuestOreItem extends QuestItem
	{
		public final String ore;
		private List<ItemEntry> entries;
		private Icon icon;

		public QuestOreItem(String o)
		{
			ore = o;
			Map<ItemEntry, ItemStack> map = new LinkedHashMap<>();

			for (ItemStack stack : OreDictionary.getOres(ore))
			{
				map.put(ItemEntry.get(stack), ItemHandlerHelper.copyStackWithSize(stack, 1));
			}

			entries = new ArrayList<>(map.keySet());

			List<Icon> icons = new ArrayList<>();

			for (ItemEntry entry : entries)
			{
				icons.add(ItemIcon.getItemIcon(entry.getStack(1, false)));
			}

			icon = new IconAnimation(icons);
		}

		@Override
		public boolean isEmpty()
		{
			return entries.isEmpty();
		}

		@Override
		public JsonElement toJson()
		{
			JsonObject json = new JsonObject();
			json.addProperty("ore", ore);
			return json;
		}

		@Override
		public Icon getIcon()
		{
			return icon;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public String getDisplayName()
		{
			return "Any " + ore; //LANG
		}

		@Override
		public boolean test(ItemStack stack)
		{
			ItemEntry item = ItemEntry.get(stack);

			for (ItemEntry entry : entries)
			{
				if (item.equalsEntry(entry))
				{
					return true;
				}
			}

			return false;
		}
	}

	public final QuestItem item;
	public final int count;

	public ItemTask(Quest parent, int index, QuestItem i, int c)
	{
		super(parent, index);
		item = i;
		count = c;
		parent.hasItemTasks = true;

		Icon icon = item.getIcon();

		if (icon instanceof IconAnimation)
		{
			for (Icon icon1 : ((IconAnimation) icon).list)
			{
				if (icon1 instanceof ItemIcon)
				{
					((ItemIcon) icon1).getStack().setCount(count);
				}
			}
		}
		else if (icon instanceof ItemIcon)
		{
			((ItemIcon) icon).getStack().setCount(count);
		}
	}

	@Override
	public int getMaxProgress()
	{
		return count;
	}

	@Override
	public Icon getIcon()
	{
		return item.getIcon();
	}

	@Override
	public JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		json.add("item", item.toJson());

		if (count != 1)
		{
			json.addProperty("count", count);
		}

		return json;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		String name = item.getDisplayName();

		if (count > 1)
		{
			return count + "x " + name;
		}

		return name;
	}

	@Override
	public boolean test(ItemStack stack)
	{
		return !stack.isEmpty() && item.test(stack);
	}

	@Override
	public ItemStack processItem(IProgressData data, ItemStack stack)
	{
		int progress = getProgress(data);
		int max = getMaxProgress();

		if (progress < max && test(stack))
		{
			int add = Math.min(stack.getCount(), Math.min(count, max - progress));

			if (add > 0 && data.setQuestTaskProgress(key, progress + add))
			{
				return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - add);
			}
		}

		return stack;
	}
}