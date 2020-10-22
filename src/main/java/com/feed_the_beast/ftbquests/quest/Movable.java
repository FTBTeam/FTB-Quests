package com.feed_the_beast.ftbquests.quest;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

	@OnlyIn(Dist.CLIENT)
	void move(Chapter to, double x, double y);
}