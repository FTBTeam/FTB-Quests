package com.feed_the_beast.ftbquests.quest.widget;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class QuestWidgetTextField extends QuestWidget
{
	public List<String> text = new ArrayList<>();

	@Override
	public QuestWidgetType getType()
	{
		return QuestWidgetType.TEXT_FIELD;
	}

	@Override
	public Widget createWidget(Panel panel)
	{
		return new TextFieldWidget(panel, this);
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		NBTTagList list = new NBTTagList();

		for (String s : text)
		{
			list.appendTag(new NBTTagString(s));
		}

		nbt.setTag("text", list);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		text.clear();
		NBTTagList list = nbt.getTagList("text", Constants.NBT.TAG_STRING);

		for (int i = 0; i < list.tagCount(); i++)
		{
			text.add(list.getStringTagAt(i));
		}
	}

	@Override
	public void writeNetData(DataOut data)
	{
		data.writeCollection(text, DataOut.STRING);
	}

	@Override
	public void readNetData(DataIn data)
	{
		text.clear();
		data.readCollection(text, DataIn.STRING);
	}
}