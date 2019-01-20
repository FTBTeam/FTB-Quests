package com.feed_the_beast.ftbquests.quest.widget;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;

import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public enum QuestWidgetType implements IWithID
{
	TEXT_FIELD("text_box", QuestWidgetTextField::new, GuiIcons.NOTES),
	BUTTON("button", QuestWidgetButton::new, GuiIcons.BLUE_BUTTON);

	public static final NameMap<QuestWidgetType> NAME_MAP = NameMap.create(TEXT_FIELD, values());

	private final String id;
	public final Supplier<QuestWidget> supplier;
	public final String translationKey;
	public final Icon icon;

	QuestWidgetType(String s, Supplier<QuestWidget> su, Icon i)
	{
		id = s;
		supplier = su;
		translationKey = "ftbquests.chapter.widget." + id;
		icon = i;
	}

	@Override
	public String getID()
	{
		return id;
	}
}