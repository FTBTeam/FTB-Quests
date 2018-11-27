package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ImageIcon;
import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public final class EnumQuestShape extends Icon implements IWithID
{
	public static final EnumQuestShape CIRCLE = new EnumQuestShape("circle");
	public static final EnumQuestShape SQUARE = new EnumQuestShape("square");
	public static final EnumQuestShape PENTAGON = new EnumQuestShape("pentagon");
	public static final EnumQuestShape HEXAGON = new EnumQuestShape("hexagon");
	public static final EnumQuestShape OCTAGON = new EnumQuestShape("octagon");

	public static final NameMap<EnumQuestShape> NAME_MAP = NameMap.create(CIRCLE, NameMap.ObjectProperties.withName((sender, o) -> new TextComponentTranslation(o.langKey)), CIRCLE, SQUARE, PENTAGON, HEXAGON, OCTAGON);

	public final String id;
	public final String langKey;
	public final ImageIcon background, outline, shape;

	public EnumQuestShape(String i)
	{
		id = i;
		langKey = "ftbquests.quest.shape." + id;
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
}