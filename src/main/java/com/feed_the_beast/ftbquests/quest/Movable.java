package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.mojang.blaze3d.matrix.MatrixStack;
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

	@OnlyIn(Dist.CLIENT)
	default void drawMoved(MatrixStack matrixStack)
	{
		QuestShape.get(getShape()).shape.withColor(Color4I.WHITE.withAlpha(30)).draw(matrixStack, 0, 0, 1, 1);
	}
}