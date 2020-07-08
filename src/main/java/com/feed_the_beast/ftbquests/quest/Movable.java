package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.icon.Color4I;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public interface Movable
{
	Chapter getChapter();

	double getX();

	double getY();

	double getWidth();

	double getHeight();

	String getShape();

	@SideOnly(Side.CLIENT)
	void move(Chapter to, double x, double y);

	@SideOnly(Side.CLIENT)
	default void drawMoved()
	{
		QuestShape.get(getShape()).shape.withColor(Color4I.WHITE.withAlpha(30)).draw(0, 0, 1, 1);
	}
}