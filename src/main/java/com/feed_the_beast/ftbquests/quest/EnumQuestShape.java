package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * @author LatvianModder
 */
public class EnumQuestShape extends Icon implements IStringSerializable
{
	public static final EnumQuestShape CIRCLE = new EnumQuestShape("circle", 36);
	public static final EnumQuestShape SQUARE = new EnumQuestShape("square", 4)
	{
		@Override
		public void fill(int angle)
		{
			double s = 0.9D;
			verticesX[0] = -s;
			verticesY[0] = -s;
			verticesX[1] = s;
			verticesY[1] = -s;
			verticesX[2] = s;
			verticesY[2] = s;
			verticesX[3] = -s;
			verticesY[3] = s;
			s = s * 31D / 32D;
			innerVerticesX[0] = -s;
			innerVerticesY[0] = -s;
			innerVerticesX[1] = s;
			innerVerticesY[1] = -s;
			innerVerticesX[2] = s;
			innerVerticesY[2] = s;
			innerVerticesX[3] = -s;
			innerVerticesY[3] = s;
		}
	};

	public static final EnumQuestShape PENTAGON = new EnumQuestShape("pentagon", 5);
	public static final EnumQuestShape HEXAGON = new EnumQuestShape("hexagon", 6);
	public static final EnumQuestShape OCTAGON = new EnumQuestShape("octagon", 8);

	public static final NameMap<EnumQuestShape> NAME_MAP = NameMap.create(CIRCLE, NameMap.ObjectProperties.withName((sender, o) -> new TextComponentTranslation("ftbquests.quest.shape." + o.getName())), CIRCLE, SQUARE, PENTAGON, HEXAGON, OCTAGON);

	public final String name;
	public final int vertices;
	public final double[] verticesX, verticesY;
	public final double[] innerVerticesX, innerVerticesY;

	public EnumQuestShape(String s, int v)
	{
		name = s;
		vertices = v;
		verticesX = new double[vertices + 1];
		verticesY = new double[vertices + 1];
		innerVerticesX = new double[vertices + 1];
		innerVerticesY = new double[vertices + 1];
		fill(0);
		verticesX[vertices] = verticesX[0];
		verticesY[vertices] = verticesY[0];
		innerVerticesX[vertices] = innerVerticesX[0];
		innerVerticesY[vertices] = innerVerticesY[0];
	}

	public void fill(int angle)
	{
		double m = Math.PI * 2D / (double) vertices;
		double a = (angle - 90) * Math.PI / 180D;

		for (int i = 0; i < vertices; i++)
		{
			verticesX[i] = Math.cos(i * m + a);
			verticesY[i] = Math.sin(i * m + a);
			innerVerticesX[i] = verticesX[i] * 31D / 32D;
			innerVerticesY[i] = verticesY[i] * 31D / 32D;
		}
	}

	@Override
	public String getName()
	{
		return name;
	}

	public String toString()
	{
		return "quest_shape:" + getName();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(int x, int y, int w, int h, Color4I col)
	{
		col = col.whiteIfEmpty();

		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();


		int r = col.redi();
		int g = col.greeni();
		int b = col.bluei();
		int a = col.alphai() / 2;
		double w2 = w / 2D;
		double h2 = h / 2D;

		buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(x + w2, y + h2, 0).color(r, g, b, a).endVertex();

		for (int i = vertices; i >= 0; i--)
		{
			buffer.pos(x + w2 + verticesX[i] * w2, y + h2 + verticesY[i] * h2, 0).color(r, g, b, a).endVertex();
		}

		tessellator.draw();

		r /= 3;
		g /= 3;
		b /= 3;
		a = col.alphai() * 3 / 4;

		buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(x + w2, y + h2, 0).color(r, g, b, 0).endVertex();

		for (int i = vertices; i >= 0; i--)
		{
			buffer.pos(x + w2 + innerVerticesX[i] * w2, y + h2 + innerVerticesY[i] * h2, 0).color(r, g, b, a).endVertex();
		}

		tessellator.draw();

		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
	}
}