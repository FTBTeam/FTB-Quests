package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ImageIcon;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;

/**
 * @author LatvianModder
 */
public final class QuestShape extends Icon
{
	public static final QuestShape DEFAULT = new QuestShape("default").circularCheck();
	public static final QuestShape CIRCLE = new QuestShape("circle").circularCheck();
	public static final QuestShape SQUARE = new QuestShape("square");
	public static final QuestShape DIAMOND = new QuestShape("diamond").circularCheck();
	public static final QuestShape RSQUARE = new QuestShape("rsquare");
	public static final QuestShape PENTAGON = new QuestShape("pentagon").circularCheck();
	public static final QuestShape HEXAGON = new QuestShape("hexagon").circularCheck();
	public static final QuestShape OCTAGON = new QuestShape("octagon").circularCheck();
	public static final QuestShape HEART = new QuestShape("heart");
	public static final QuestShape GEAR = new QuestShape("gear").circularCheck();

	public static final NameMap<QuestShape> NAME_MAP = NameMap.of(DEFAULT, Arrays.asList(DEFAULT, CIRCLE, SQUARE, DIAMOND, RSQUARE, PENTAGON, HEXAGON, OCTAGON, HEART, GEAR)).id(v -> v.id).baseNameKey("ftbquests.quest.shape").create();

	public final String id;
	public final ImageIcon background, outline, shape;
	public boolean circularCheck;

	public QuestShape(String i)
	{
		id = i;
		background = new ImageIcon(new ResourceLocation(FTBQuests.MOD_ID, "textures/shapes/" + id + "/background.png"));
		outline = new ImageIcon(new ResourceLocation(FTBQuests.MOD_ID, "textures/shapes/" + id + "/outline.png"));
		shape = new ImageIcon(new ResourceLocation(FTBQuests.MOD_ID, "textures/shapes/" + id + "/shape.png"));
		circularCheck = false;
	}

	public QuestShape circularCheck()
	{
		circularCheck = true;
		return this;
	}

	public String toString()
	{
		return "quest_shape:" + id;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void draw(MatrixStack matrixStack, int x, int y, int w, int h)
	{
		background.draw(matrixStack, x, y, w, h);
		outline.draw(matrixStack, x, y, w, h);
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