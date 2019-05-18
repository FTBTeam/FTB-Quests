package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ImageIcon;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Quest;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * @author LatvianModder
 */
public class PanelQuests extends Panel
{
	public static final ImageIcon DEPENDENCY = (ImageIcon) Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/dependency.png");
	public final GuiQuestTree treeGui;

	public PanelQuests(Panel panel)
	{
		super(panel);
		treeGui = (GuiQuestTree) panel.getGui();
	}

	private int getxy(int x, int y)
	{
		return ((x + Quest.POS_LIMIT) << 16) | (y + Quest.POS_LIMIT);
	}

	@Override
	public void addWidgets()
	{
		if (treeGui.selectedChapter == null)
		{
			return;
		}

		boolean canEdit = treeGui.file.canEdit();

		IntOpenHashSet set = new IntOpenHashSet();

		for (Quest quest : treeGui.selectedChapter.quests)
		{
			if (treeGui.file.canEdit() || !quest.getActualVisibility(ClientQuestFile.INSTANCE.self).isInvisible())
			{
				add(new ButtonQuest(this, quest));

				if (canEdit)
				{
					set.add(getxy(quest.x, quest.y));
				}
			}
		}

		if (canEdit)
		{
			for (int y = -Quest.POS_LIMIT; y <= Quest.POS_LIMIT; y++)
			{
				for (int x = -Quest.POS_LIMIT; x <= Quest.POS_LIMIT; x++)
				{
					if (!set.contains(getxy(x, y)))
					{
						add(new ButtonDummyQuest(this, (byte) x, (byte) y));
					}
				}
			}
		}

		alignWidgets();
	}

	@Override
	public void alignWidgets()
	{
		treeGui.scrollWidth = 0;
		treeGui.scrollHeight = 0;

		int minX = Quest.POS_LIMIT + 1, minY = Quest.POS_LIMIT + 1, maxX = -(Quest.POS_LIMIT + 1), maxY = -(Quest.POS_LIMIT + 1);

		for (Widget widget : widgets)
		{
			if (widget instanceof ButtonQuest)
			{
				Quest quest = ((ButtonQuest) widget).quest;
				minX = Math.min(minX, quest.x);
				minY = Math.min(minY, quest.y);
				maxX = Math.max(maxX, quest.x);
				maxY = Math.max(maxY, quest.y);
			}
		}

		minX -= 6;
		minY -= 6;
		maxX += 6;
		maxY += 6;

		int bsize = treeGui.getZoom() * 2 - 2;

		treeGui.scrollWidth = (maxX - minX + 1) * bsize;
		treeGui.scrollHeight = (maxY - minY + 1) * bsize;

		for (Widget widget : widgets)
		{
			int x, y;

			if (widget instanceof ButtonQuest)
			{
				Quest quest = ((ButtonQuest) widget).quest;
				x = quest.x;
				y = quest.y;
			}
			else
			{
				ButtonDummyQuest button = (ButtonDummyQuest) widget;
				x = button.x;
				y = button.y;
			}

			widget.setPosAndSize((x - minX) * bsize, (y - minY) * bsize, bsize, bsize);
		}

		setPosAndSize(0, treeGui.chapterPanel.height, treeGui.width, treeGui.height - treeGui.chapterPanel.height);
	}

	@Override
	public void drawOffsetBackground(Theme theme, int x, int y, int w, int h)
	{
		GlStateManager.color(1F, 1F, 1F, 1F);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		DEPENDENCY.bindTexture();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		double moving = -(System.currentTimeMillis() * 0.001D) % 1D;
		double s = treeGui.getZoom() / 8D;
		Quest selectedQuest = treeGui.getViewedQuest();

		for (Widget widget : widgets)
		{
			if (widget instanceof ButtonQuest)
			{
				Quest wquest = ((ButtonQuest) widget).quest;

				if (wquest.hideDependencyLines)
				{
					continue;
				}

				boolean unavailable = treeGui.file.self == null || !wquest.canStartTasks(treeGui.file.self);
				boolean complete = !unavailable && treeGui.file.self != null && wquest.isComplete(treeGui.file.self);

				for (ButtonQuest button : ((ButtonQuest) widget).getDependencies())
				{
					if (button.quest == selectedQuest || wquest == selectedQuest)
					{
						continue;
					}

					int r, g, b, a;

					if (complete)
					{
						r = 100;
						g = 220;
						b = 100;
						a = 255;
					}
					else
					{
						Color4I c = Color4I.hsb(button.quest.id / 1000F, 0.2F, unavailable ? 0.3F : 0.8F);
						r = c.redi();
						g = c.greeni();
						b = c.bluei();
						a = 180;
					}

					double sx = widget.getX() + widget.width / 2D;
					double sy = widget.getY() + widget.height / 2D;
					double ex = button.getX() + button.width / 2D;
					double ey = button.getY() + button.height / 2D;
					double len = MathUtils.dist(sx, sy, ex, ey);

					GlStateManager.pushMatrix();
					GlStateManager.translate(sx, sy, 0);
					GlStateManager.rotate((float) (Math.atan2(ey - sy, ex - sx) * 180D / Math.PI), 0F, 0F, 1F);
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
					buffer.pos(0, -s, 0).tex(len / s / 2D, 0).color(r, g, b, a).endVertex();
					buffer.pos(0, s, 0).tex(len / s / 2D, 1).color(r, g, b, a).endVertex();
					buffer.pos(len, s, 0).tex(0D, 1).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).endVertex();
					buffer.pos(len, -s, 0).tex(0D, 0).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).endVertex();
					tessellator.draw();
					GlStateManager.popMatrix();
				}
			}
		}

		for (Widget widget : widgets)
		{
			if (widget instanceof ButtonQuest)
			{
				Quest wquest = ((ButtonQuest) widget).quest;

				if (wquest.hideDependencyLines)
				{
					continue;
				}

				for (ButtonQuest button : ((ButtonQuest) widget).getDependencies())
				{
					int r, g, b;

					if (button.quest == selectedQuest)
					{
						r = 200;
						g = 200;
						b = 0;
					}
					else if (wquest == selectedQuest)
					{
						r = 0;
						g = 200;
						b = 200;
					}
					else
					{
						continue;
					}

					double sx = widget.getX() + widget.width / 2D;
					double sy = widget.getY() + widget.height / 2D;
					double ex = button.getX() + button.width / 2D;
					double ey = button.getY() + button.height / 2D;
					double len = MathUtils.dist(sx, sy, ex, ey);

					GlStateManager.pushMatrix();
					GlStateManager.translate(sx, sy, 0);
					GlStateManager.rotate((float) (Math.atan2(ey - sy, ex - sx) * 180D / Math.PI), 0F, 0F, 1F);
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
					buffer.pos(0, -s, 0).tex(len / s / 2D + moving, 0).color(r, g, b, 255).endVertex();
					buffer.pos(0, s, 0).tex(len / s / 2D + moving, 1).color(r, g, b, 255).endVertex();
					buffer.pos(len, s, 0).tex(moving, 1).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, 255).endVertex();
					buffer.pos(len, -s, 0).tex(moving, 0).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, 255).endVertex();
					tessellator.draw();
					GlStateManager.popMatrix();
				}
			}
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.color(1F, 1F, 1F, 1F);
	}

	@Override
	public boolean mousePressed(MouseButton button)
	{
		boolean b = super.mousePressed(button);

		if (!b && !treeGui.viewQuestPanel.hidePanel && treeGui.getViewedQuest() != null)
		{
			treeGui.closeQuest();
		}

		if (!b && button.isLeft() && isMouseOver() && (treeGui.viewQuestPanel.hidePanel || treeGui.getViewedQuest() == null))
		{
			treeGui.prevMouseX = getMouseX();
			treeGui.prevMouseY = getMouseY();
			treeGui.grabbed = 1;
			b = true;
		}

		return b;
	}

	@Override
	public void mouseReleased(MouseButton button)
	{
		super.mouseReleased(button);
		treeGui.grabbed = 0;
	}

	@Override
	public boolean scrollPanel(int scroll)
	{
		if (treeGui.selectedChapter != null && treeGui.getViewedQuest() == null && isMouseOver())
		{
			treeGui.addZoom(scroll);
			return true;
		}

		return false;
	}
}