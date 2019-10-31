package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ImageIcon;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateTaskAt;
import com.feed_the_beast.ftbquests.net.edit.MessageMoveQuest;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class PanelQuests extends Panel
{
	private static final ImageIcon DEFAULT_DEPENDENCY_LINE_TEXTURE = (ImageIcon) Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/dependency.png");
	public final GuiQuestTree treeGui;
	public double questX = 0;
	public double questY = 0;
	public double centerQuestX = 0;
	public double centerQuestY = 0;
	public ButtonQuest mouseOverQuest = null;

	public PanelQuests(Panel panel)
	{
		super(panel);
		treeGui = (GuiQuestTree) panel.getGui();
	}

	public void scrollTo(double x, double y)
	{
		double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

		for (Widget widget : widgets)
		{
			Quest quest = ((ButtonQuest) widget).quest;
			minX = Math.min(minX, quest.x);
			minY = Math.min(minY, quest.y);
			maxX = Math.max(maxX, quest.x);
			maxY = Math.max(maxY, quest.y);
		}

		if (minX == Double.POSITIVE_INFINITY)
		{
			minX = minY = maxX = maxY = 0;
		}

		minX -= 40;
		minY -= 30;
		maxX += 40;
		maxY += 30;

		double dx = (maxX - minX + 1);
		double dy = (maxY - minY + 1);

		setOffset(true);
		setScrollX((x - minX) / dx * treeGui.scrollWidth - width / 2D);
		setScrollY((y - minY) / dy * treeGui.scrollHeight - height / 2D);
		setOffset(false);
	}

	public void resetScroll()
	{
		alignWidgets();
		setScrollX((treeGui.scrollWidth - width) / 2D);
		setScrollY((treeGui.scrollHeight - height) / 2D);
	}

	@Override
	public void addWidgets()
	{
		if (treeGui.selectedChapter == null)
		{
			return;
		}

		for (Quest quest : treeGui.selectedChapter.quests)
		{
			if (treeGui.file.canEdit() || quest.isVisible(ClientQuestFile.INSTANCE.self))
			{
				add(new ButtonQuest(this, quest));
			}
		}

		alignWidgets();
	}

	@Override
	public void alignWidgets()
	{
		if (treeGui.selectedChapter == null)
		{
			return;
		}

		treeGui.scrollWidth = 0;
		treeGui.scrollHeight = 0;

		double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

		for (Widget widget : widgets)
		{
			Quest quest = ((ButtonQuest) widget).quest;
			minX = Math.min(minX, quest.x);
			minY = Math.min(minY, quest.y);
			maxX = Math.max(maxX, quest.x);
			maxY = Math.max(maxY, quest.y);
		}

		if (minX == Double.POSITIVE_INFINITY)
		{
			minX = minY = maxX = maxY = 0D;
		}

		minX -= 40;
		minY -= 30;
		maxX += 40;
		maxY += 30;

		double bs = treeGui.getQuestButtonSize();
		double bp = treeGui.getQuestButtonSpacing();

		treeGui.scrollWidth = (maxX - minX + 1D) * (bs + bp);
		treeGui.scrollHeight = (maxY - minY + 1D) * (bs + bp);

		for (Widget w : widgets)
		{
			Quest q = ((ButtonQuest) w).quest;
			double s = bs * q.size;
			double x = (q.x - minX - q.size / 2D) * (bs + bp) + bp / 2D + bp * (q.size - 1D) / 2D;
			double y = (q.y - minY - q.size / 2D) * (bs + bp) + bp / 2D + bp * (q.size - 1D) / 2D;
			w.setPosAndSize((int) x, (int) y, (int) s, (int) s);
		}

		setPosAndSize(20, 1, treeGui.width - 40, treeGui.height - 2);
	}

	@Override
	public void drawOffsetBackground(Theme theme, int x, int y, int w, int h)
	{
		if (treeGui.selectedChapter == null)
		{
			return;
		}

		GlStateManager.color(1F, 1F, 1F, 1F);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		Icon icon = ThemeProperties.DEPENDENCY_LINE_TEXTURE.get(treeGui.selectedChapter);

		if (icon instanceof ImageIcon)
		{
			icon.bindTexture();
		}
		else
		{
			DEFAULT_DEPENDENCY_LINE_TEXTURE.bindTexture();
		}

		Quest selectedQuest = treeGui.getViewedQuest();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		double mt = -(System.currentTimeMillis() * 0.001D);
		double mu = (mt * ThemeProperties.DEPENDENCY_LINE_UNSELECTED_SPEED.get(treeGui.selectedChapter)) % 1D;
		double ms = (mt * ThemeProperties.DEPENDENCY_LINE_SELECTED_SPEED.get(treeGui.selectedChapter)) % 1D;
		double s = treeGui.getZoom() * ThemeProperties.DEPENDENCY_LINE_THICKNESS.get(treeGui.selectedChapter) / 4D * 3D;

		for (Widget widget : widgets)
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
					Color4I c = ThemeProperties.DEPENDENCY_LINE_COMPLETED_COLOR.get(treeGui.selectedChapter);
					r = c.redi();
					g = c.greeni();
					b = c.bluei();
					a = c.alphai();
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
				buffer.pos(0, -s, 0).tex(len / s / 2D + mu, 0).color(r, g, b, a).endVertex();
				buffer.pos(0, s, 0).tex(len / s / 2D + mu, 1).color(r, g, b, a).endVertex();
				buffer.pos(len, s, 0).tex(mu, 1).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).endVertex();
				buffer.pos(len, -s, 0).tex(mu, 0).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).endVertex();
				tessellator.draw();
				GlStateManager.popMatrix();
			}
		}

		for (Widget widget : widgets)
		{
			Quest wquest = ((ButtonQuest) widget).quest;

			if (wquest.hideDependencyLines)
			{
				continue;
			}

			for (ButtonQuest button : ((ButtonQuest) widget).getDependencies())
			{
				int r, g, b, a;

				if (button.quest == selectedQuest)
				{
					Color4I c = ThemeProperties.DEPENDENCY_LINE_REQUIRED_FOR_COLOR.get(treeGui.selectedChapter);
					r = c.redi();
					g = c.greeni();
					b = c.bluei();
					a = c.alphai();
				}
				else if (wquest == selectedQuest)
				{
					Color4I c = ThemeProperties.DEPENDENCY_LINE_REQUIRES_COLOR.get(treeGui.selectedChapter);
					r = c.redi();
					g = c.greeni();
					b = c.bluei();
					a = c.alphai();
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
				buffer.pos(0, -s, 0).tex(len / s / 2D + ms, 0).color(r, g, b, a).endVertex();
				buffer.pos(0, s, 0).tex(len / s / 2D + ms, 1).color(r, g, b, a).endVertex();
				buffer.pos(len, s, 0).tex(ms, 1).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).endVertex();
				buffer.pos(len, -s, 0).tex(ms, 0).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).endVertex();
				tessellator.draw();
				GlStateManager.popMatrix();
			}
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.color(1F, 1F, 1F, 1F);
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		super.draw(theme, x, y, w, h);

		if (treeGui.selectedChapter != null && isMouseOver())
		{
			double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

			for (Widget widget : widgets)
			{
				Quest quest = ((ButtonQuest) widget).quest;
				minX = Math.min(minX, quest.x);
				minY = Math.min(minY, quest.y);
				maxX = Math.max(maxX, quest.x);
				maxY = Math.max(maxY, quest.y);
			}

			if (minX == Double.POSITIVE_INFINITY)
			{
				minX = minY = maxX = maxY = 0;
			}

			minX -= 40;
			minY -= 30;
			maxX += 40;
			maxY += 30;

			double dx = (maxX - minX + 1);
			double dy = (maxY - minY + 1);

			setOffset(true);
			double qx = (treeGui.getMouseX() - getX()) / treeGui.scrollWidth * dx + minX;
			double qy = (treeGui.getMouseY() - getY()) / treeGui.scrollHeight * dy + minY;
			centerQuestX = (treeGui.width / 2D - getX()) / treeGui.scrollWidth * dx + minX;
			centerQuestY = (treeGui.height / 2D - getY()) / treeGui.scrollHeight * dy + minY;
			setOffset(false);

			if (isShiftKeyDown())
			{
				questX = qx;
				questY = qy;
			}
			else
			{
				questX = MathHelper.floor(qx * 2D + 0.5D) / 2D;
				questY = MathHelper.floor(qy * 2D + 0.5D) / 2D;
			}

			if (treeGui.file.canEdit())
			{
				theme.pushFontUnicode(true);
				GlStateManager.pushMatrix();
				GlStateManager.translate(0D, 0D, 1000D);
				theme.drawString("X:" + (questX < 0 ? "" : " ") + StringUtils.DOUBLE_FORMATTER_00.format(questX), x + 3, y + h - 18, Theme.SHADOW);
				theme.drawString("Y:" + (questY < 0 ? "" : " ") + StringUtils.DOUBLE_FORMATTER_00.format(questY), x + 3, y + h - 10, Theme.SHADOW);
				theme.drawString("CX:" + (centerQuestX < 0 ? "" : " ") + StringUtils.DOUBLE_FORMATTER_00.format(centerQuestX), x + w - 30, y + h - 18, Theme.SHADOW);
				theme.drawString("CY:" + (centerQuestY < 0 ? "" : " ") + StringUtils.DOUBLE_FORMATTER_00.format(centerQuestY), x + w - 30, y + h - 10, Theme.SHADOW);
				GlStateManager.popMatrix();
				theme.popFontUnicode();

				if (treeGui.movingQuests && !treeGui.selectedQuests.isEmpty())
				{
					double bs = treeGui.getQuestButtonSize();

					double ominX = Double.POSITIVE_INFINITY, ominY = Double.POSITIVE_INFINITY, omaxX = Double.NEGATIVE_INFINITY, omaxY = Double.NEGATIVE_INFINITY;

					for (Quest q : treeGui.selectedQuests)
					{
						ominX = Math.min(ominX, q.x);
						ominY = Math.min(ominY, q.y);
						omaxX = Math.max(omaxX, q.x);
						omaxY = Math.max(omaxY, q.y);
					}

					setOffset(true);
					int panelX = getX();
					int panelY = getY();
					setOffset(false);

					for (Quest q : treeGui.selectedQuests)
					{
						double s = bs * q.size;
						double ox = (q.x - ominX);
						double oy = (q.y - ominY);
						double sx = (questX + ox - minX) / dx * treeGui.scrollWidth + panelX;
						double sy = (questY + oy - minY) / dy * treeGui.scrollHeight + panelY;
						GlStateManager.pushMatrix();
						GlStateManager.translate(sx - s / 2D, sy - s / 2D, 0D);
						GlStateManager.scale(s, s, 1D);
						GuiHelper.setupDrawing();
						q.getShape().shape.withColor(Color4I.WHITE.withAlpha(30)).draw(0, 0, 1, 1);
						GlStateManager.popMatrix();
					}

					if (GuiQuestTree.grid)
					{
						double boxX = ominX / dx * treeGui.scrollWidth + panelX;
						double boxY = ominY / dy * treeGui.scrollHeight + panelY;
						double boxW = omaxX / dx * treeGui.scrollWidth + panelX - boxX;
						double boxH = omaxY / dy * treeGui.scrollHeight + panelY - boxY;

						GlStateManager.pushMatrix();
						GlStateManager.translate(0, 0, 1000);
						GuiHelper.drawHollowRect((int) boxX, (int) boxY, (int) boxW, (int) boxH, Color4I.WHITE.withAlpha(30), false);
						GlStateManager.popMatrix();
					}
				}
				else
				{
					int z = treeGui.getZoom();
					double bs = treeGui.getQuestButtonSize();
					setOffset(true);
					double sx = (questX - minX) / dx * treeGui.scrollWidth + getX();
					double sy = (questY - minY) / dy * treeGui.scrollHeight + getY();
					setOffset(false);
					GlStateManager.pushMatrix();
					GlStateManager.translate(sx - bs / 2D, sy - bs / 2D, 0D);
					GlStateManager.scale(bs, bs, 1D);
					GuiHelper.setupDrawing();
					treeGui.selectedChapter.getDefaultQuestShape().shape.withColor(Color4I.WHITE.withAlpha(10)).draw(0, 0, 1, 1);
					GlStateManager.popMatrix();

					if (GuiQuestTree.grid)
					{
						GlStateManager.pushMatrix();
						GlStateManager.translate(0, 0, 1000);
						Color4I.WHITE.draw((int) sx, (int) sy, 1, 1);
						Color4I.WHITE.withAlpha(30).draw(0, (int) sy, width, 1);
						Color4I.WHITE.withAlpha(30).draw((int) sx, 0, 1, height);
						GlStateManager.popMatrix();
					}
				}
			}
		}
	}

	@Override
	public boolean mousePressed(MouseButton button)
	{
		if (treeGui.movingQuests && treeGui.file.canEdit())
		{
			if (treeGui.selectedChapter != null && !button.isRight() && !treeGui.selectedQuests.isEmpty())
			{
				GuiHelper.playClickSound();

				double minX = Double.POSITIVE_INFINITY;
				double minY = Double.POSITIVE_INFINITY;

				for (Quest q : treeGui.selectedQuests)
				{
					minX = Math.min(minX, q.x);
					minY = Math.min(minY, q.y);
				}

				for (Quest q : treeGui.selectedQuests)
				{
					new MessageMoveQuest(q.id, treeGui.selectedChapter.id, questX + (q.x - minX), questY + (q.y - minY)).sendToServer();
				}
			}

			treeGui.movingQuests = false;
			treeGui.selectedQuests.clear();
			return true;
		}

		if (super.mousePressed(button))
		{
			return true;
		}

		if (!treeGui.viewQuestPanel.hidePanel && treeGui.getViewedQuest() != null)
		{
			treeGui.closeQuest();
			return true;
		}

		if (button.isLeft() && isMouseOver() && (treeGui.viewQuestPanel.hidePanel || treeGui.getViewedQuest() == null))
		{
			treeGui.prevMouseX = getMouseX();
			treeGui.prevMouseY = getMouseY();
			treeGui.grabbed = 1;
			return true;
		}

		if (button.isRight() && treeGui.file.canEdit())
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			double qx = questX;
			double qy = questY;

			for (TaskType type : TaskType.getRegistry())
			{
				contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
					GuiHelper.playClickSound();
					type.getGuiProvider().openCreationGui(this, new Quest(treeGui.selectedChapter), task -> new MessageCreateTaskAt(treeGui.selectedChapter, qx, qy, task).sendToServer());
				}));
			}

			treeGui.openContextMenu(contextMenu);
			return true;
		}

		return false;
	}

	@Override
	public void mouseReleased(MouseButton button)
	{
		super.mouseReleased(button);
		treeGui.grabbed = 0;
	}

	@Override
	public boolean checkMouseOver(int mouseX, int mouseY)
	{
		if (!treeGui.chapterHoverPanel.widgets.isEmpty())
		{
			return false;
		}

		return super.checkMouseOver(mouseX, mouseY);
	}

	@Override
	public void updateMouseOver(int mouseX, int mouseY)
	{
		mouseOverQuest = null;
		super.updateMouseOver(mouseX, mouseY);

		for (Widget widget : widgets)
		{
			if (widget.isMouseOver() && widget instanceof ButtonQuest)
			{
				mouseOverQuest = (ButtonQuest) widget;
				break;
			}
		}
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