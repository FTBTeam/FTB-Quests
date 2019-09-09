package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public enum QuestObjectType implements IWithID, Predicate<QuestObjectBase>
{
	NULL("null", 1, TextFormatting.BLACK),
	FILE("file", 2, TextFormatting.RED),
	CHAPTER("chapter", 4, TextFormatting.GOLD),
	QUEST("quest", 8, TextFormatting.GREEN),
	TASK("task", 16, TextFormatting.BLUE),
	//32
	REWARD("reward", 64, TextFormatting.LIGHT_PURPLE),
	REWARD_TABLE("reward_table", 128, TextFormatting.YELLOW);

	public static final NameMap<QuestObjectType> NAME_MAP = NameMap.create(NULL, values());
	public static final Predicate<QuestObjectBase> ALL_PROGRESSING = object -> object instanceof QuestObject;
	public static final Predicate<QuestObjectBase> ALL_PROGRESSING_OR_NULL = object -> object == null || object instanceof QuestObject;

	private final String id;
	private final String translationKey;
	private final int flag;
	private final TextFormatting color;

	QuestObjectType(String i, int f, TextFormatting c)
	{
		id = i;
		translationKey = "ftbquests." + id;
		flag = f;
		color = c;
	}

	@Override
	public String getId()
	{
		return id;
	}

	public String getTranslationKey()
	{
		return translationKey;
	}

	public int getFlag()
	{
		return flag;
	}

	public TextFormatting getColor()
	{
		return color;
	}

	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return I18n.format(translationKey);
	}

	@Override
	public boolean test(QuestObjectBase object)
	{
		return (object == null ? NULL : object.getObjectType()) == this;
	}
}