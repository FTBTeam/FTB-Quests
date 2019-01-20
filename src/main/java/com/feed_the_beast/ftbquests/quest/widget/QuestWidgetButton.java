package com.feed_the_beast.ftbquests.quest.widget;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class QuestWidgetButton extends QuestWidget
{
	public String click = "";
	public String title = "";

	@Override
	public QuestWidgetType getType()
	{
		return QuestWidgetType.BUTTON;
	}

	@Override
	public Widget createWidget(Panel panel)
	{
		return new ButtonWidget(panel, this);
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("click", click);
		nbt.setString("title", title);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		click = nbt.getString("click");
		title = nbt.getString("title");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		data.writeString(click);
		data.writeString(title);
	}

	@Override
	public void readNetData(DataIn data)
	{
		click = data.readString();
		title = data.readString();
	}
}