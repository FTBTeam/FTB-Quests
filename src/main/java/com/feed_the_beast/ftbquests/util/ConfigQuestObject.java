package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftbquests.gui.GuiSelectQuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigCallback;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigValue;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class ConfigQuestObject<T extends QuestObjectBase> extends ConfigValue<T>
{
	public final Predicate<QuestObjectBase> predicate;

	public ConfigQuestObject(Predicate<QuestObjectBase> t)
	{
		predicate = t;
	}

	@Override
	public String getStringForGUI(@Nullable QuestObjectBase value)
	{
		if (value == null)
		{
			return "";
		}

		return value.getTitle();
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback)
	{
		if (getCanEdit())
		{
			new GuiSelectQuestObject<>(this, callback).openGui();
		}
	}

	@Override
	public void addInfo(List<String> list)
	{
		if (value != null)
		{
			list.add(TextFormatting.AQUA + "ID: " + TextFormatting.RESET + value);
		}
	}
}