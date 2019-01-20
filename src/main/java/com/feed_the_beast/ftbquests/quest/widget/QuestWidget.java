package com.feed_the_beast.ftbquests.quest.widget;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public abstract class QuestWidget
{
	public int x, y, w, h;

	public abstract QuestWidgetType getType();

	public abstract Widget createWidget(Panel panel);

	public abstract void writeData(NBTTagCompound nbt);

	public abstract void readData(NBTTagCompound nbt);

	public abstract void writeNetData(DataOut data);

	public abstract void readNetData(DataIn data);
}