package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ImageIcon;
import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public final class QuestShape extends Icon implements IWithID
{
	public static final QuestShape CIRCLE = new QuestShape("circle");
	public static final QuestShape SQUARE = new QuestShape("square");
	public static final QuestShape DIAMOND = new QuestShape("diamond");
	public static final QuestShape RSQUARE = new QuestShape("rsquare");
	public static final QuestShape PENTAGON = new QuestShape("pentagon");
	public static final QuestShape HEXAGON = new QuestShape("hexagon");
	public static final QuestShape OCTAGON = new QuestShape("octagon");
	public static final QuestShape HEART = new QuestShape("heart");
	public static final QuestShape GEAR = new QuestShape("gear");

	public static final NameMap<QuestShape> NAME_MAP = NameMap.createWithBaseTranslationKey(CIRCLE, "ftbquests.quest.shape", CIRCLE, SQUARE, DIAMOND, RSQUARE, PENTAGON, HEXAGON, OCTAGON, HEART, GEAR);

	public final String id;
	public final ImageIcon background, outline, shape;

	public QuestShape(String i)
	{
		id = i;
		background = new ImageIcon(new ResourceLocation(FTBQuests.MOD_ID, "textures/shapes/" + id + "/background.png"));
		outline = new ImageIcon(new ResourceLocation(FTBQuests.MOD_ID, "textures/shapes/" + id + "/outline.png"));
		shape = new ImageIcon(new ResourceLocation(FTBQuests.MOD_ID, "textures/shapes/" + id + "/shape.png"));
	}

	@Override
	public String getID()
	{
		return id;
	}

	public String toString()
	{
		return "quest_shape:" + id;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(int x, int y, int w, int h, Color4I col)
	{
		col = col.whiteIfEmpty();
		background.draw(x, y, w, h, col);
		outline.draw(x, y, w, h, col);
	}

	public int hashCode()
	{
		return id.hashCode();
	}

	public boolean equals(Object o)
	{
		return o == this;
	}
}