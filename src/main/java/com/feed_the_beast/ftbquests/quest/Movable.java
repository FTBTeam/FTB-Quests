package com.feed_the_beast.ftbquests.quest;

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
}