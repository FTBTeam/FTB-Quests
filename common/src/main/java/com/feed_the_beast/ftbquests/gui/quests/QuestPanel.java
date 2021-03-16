package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.MessageCreateTaskAt;
import com.feed_the_beast.ftbquests.net.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.ChapterImage;
import com.feed_the_beast.ftbquests.quest.Movable;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestShape;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.ftbquests.quest.task.TaskTypes;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ImageIcon;
import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.MathUtils;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class QuestPanel extends Panel {
	private static final ImageIcon DEFAULT_DEPENDENCY_LINE_TEXTURE = (ImageIcon) Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/dependency.png");
	public final QuestScreen questScreen;
	public double questX = 0;
	public double questY = 0;
	public double centerQuestX = 0;
	public double centerQuestY = 0;
	public QuestButton mouseOverQuest = null;
	public double questMinX, questMinY, questMaxX, questMaxY;

	public QuestPanel(Panel panel) {
		super(panel);
		questScreen = (QuestScreen) panel.getGui();
	}

	public void updateMinMax() {
		questMinX = Double.POSITIVE_INFINITY;
		questMinY = Double.POSITIVE_INFINITY;
		questMaxX = Double.NEGATIVE_INFINITY;
		questMaxY = Double.NEGATIVE_INFINITY;

		for (Widget w : widgets) {
			double qx, qy, qw, qh;

			if (w instanceof QuestButton) {
				Quest q = ((QuestButton) w).quest;
				qx = q.x;
				qy = q.y;
				qw = q.size;
				qh = q.size;
			} else if (w instanceof ChapterImageButton) {
				ChapterImage q = ((ChapterImageButton) w).chapterImage;
				qx = q.x;
				qy = q.y;
				qw = q.width;
				qh = q.height;
			} else {
				continue;
			}

			questMinX = Math.min(questMinX, qx - qw / 2D);
			questMinY = Math.min(questMinY, qy - qh / 2D);
			questMaxX = Math.max(questMaxX, qx + qw / 2D);
			questMaxY = Math.max(questMaxY, qy + qh / 2D);
		}

		if (questMinX == Double.POSITIVE_INFINITY) {
			questMinX = questMinY = questMaxX = questMaxY = 0D;
		}

		questMinX -= 40D;
		questMinY -= 30D;
		questMaxX += 40D;
		questMaxY += 30D;
	}

	public void scrollTo(double x, double y) {
		updateMinMax();

		double dx = (questMaxX - questMinX);
		double dy = (questMaxY - questMinY);

		setScrollX((x - questMinX) / dx * questScreen.scrollWidth - width / 2D);
		setScrollY((y - questMinY) / dy * questScreen.scrollHeight - height / 2D);
	}

	public void resetScroll() {
		alignWidgets();
		setScrollX((questScreen.scrollWidth - width) / 2D);
		setScrollY((questScreen.scrollHeight - height) / 2D);
	}

	@Override
	public void addWidgets() {
		if (questScreen.selectedChapter == null) {
			return;
		}

		for (ChapterImage image : questScreen.selectedChapter.images) {
			if (questScreen.file.canEdit() || !image.dev) {
				add(new ChapterImageButton(this, image));
			}
		}

		for (Quest quest : questScreen.selectedChapter.quests) {
			if (questScreen.file.canEdit() || quest.isVisible(ClientQuestFile.INSTANCE.self)) {
				add(new QuestButton(this, quest));
			}
		}

		alignWidgets();
	}

	@Override
	public void alignWidgets() {
		if (questScreen.selectedChapter == null) {
			return;
		}

		questScreen.scrollWidth = 0D;
		questScreen.scrollHeight = 0D;

		updateMinMax();

		double bs = questScreen.getQuestButtonSize();
		double bp = questScreen.getQuestButtonSpacing();

		questScreen.scrollWidth = (questMaxX - questMinX) * (bs + bp);
		questScreen.scrollHeight = (questMaxY - questMinY) * (bs + bp);

		for (Widget w : widgets) {
			double qx, qy, qw, qh;

			if (w instanceof QuestButton) {
				Quest q = ((QuestButton) w).quest;
				qx = q.x;
				qy = q.y;
				qw = q.size;
				qh = q.size;
			} else if (w instanceof ChapterImageButton) {
				ChapterImage q = ((ChapterImageButton) w).chapterImage;
				qx = q.x;
				qy = q.y;
				qw = q.width;
				qh = q.height;
			} else {
				continue;
			}

			double x = (qx - questMinX - qw / 2D) * (bs + bp) + bp / 2D + bp * (qw - 1D) / 2D;
			double y = (qy - questMinY - qh / 2D) * (bs + bp) + bp / 2D + bp * (qh - 1D) / 2D;
			w.setPosAndSize((int) x, (int) y, (int) (bs * qw), (int) (bs * qh));
		}

		setPosAndSize(20, 1, questScreen.width - 40, questScreen.height - 2);
	}

	@Override
	public void drawOffsetBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		if (questScreen.selectedChapter == null) {
			return;
		}

		GuiHelper.setupDrawing();

		for (Widget widget : widgets) {
			if (widget instanceof ChapterImageButton) {
				widget.draw(matrixStack, theme, widget.getX(), widget.getY(), widget.width, widget.height);
			}
		}

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuilder();

		Icon icon = ThemeProperties.DEPENDENCY_LINE_TEXTURE.get(questScreen.selectedChapter);

		if (icon instanceof ImageIcon) {
			icon.bindTexture();
		} else {
			DEFAULT_DEPENDENCY_LINE_TEXTURE.bindTexture();
		}

		Quest selectedQuest = questScreen.getViewedQuest();
		GuiHelper.setupDrawing();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);
		double mt = -(System.currentTimeMillis() * 0.001D);
		float mu = (float) ((mt * ThemeProperties.DEPENDENCY_LINE_UNSELECTED_SPEED.get(questScreen.selectedChapter)) % 1D);
		float ms = (float) ((mt * ThemeProperties.DEPENDENCY_LINE_SELECTED_SPEED.get(questScreen.selectedChapter)) % 1D);
		float s = (float) (questScreen.getZoom() * ThemeProperties.DEPENDENCY_LINE_THICKNESS.get(questScreen.selectedChapter) / 4D * 3D);

		for (Widget widget : widgets) {
			if (!(widget instanceof QuestButton)) {
				continue;
			}

			Quest wquest = ((QuestButton) widget).quest;

			if (wquest.hideDependencyLines.get(false)) {
				continue;
			}

			boolean unavailable = questScreen.file.self == null || !questScreen.file.self.canStartTasks(wquest);
			boolean complete = !unavailable && questScreen.file.self != null && questScreen.file.self.isComplete(wquest);

			for (QuestButton button : ((QuestButton) widget).getDependencies()) {
				if (button.quest == selectedQuest || wquest == selectedQuest) {
					continue;
				}

				int r, g, b, a;

				if (complete) {
					Color4I c = ThemeProperties.DEPENDENCY_LINE_COMPLETED_COLOR.get(questScreen.selectedChapter);
					r = c.redi();
					g = c.greeni();
					b = c.bluei();
					a = c.alphai();
				} else {
					Color4I c = Color4I.hsb(button.quest.id / 1000F, 0.2F, unavailable ? 0.3F : 0.8F);
					r = c.redi();
					g = c.greeni();
					b = c.bluei();
					a = 180;
				}

				int sx = widget.getX() + widget.width / 2;
				int sy = widget.getY() + widget.height / 2;
				int ex = button.getX() + button.width / 2;
				int ey = button.getY() + button.height / 2;
				float len = (float) MathUtils.dist(sx, sy, ex, ey);

				matrixStack.pushPose();
				matrixStack.translate(sx, sy, 0);
				matrixStack.mulPose(Vector3f.ZP.rotation((float) Math.atan2(ey - sy, ex - sx)));
				Matrix4f m = matrixStack.last().pose();

				buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
				buffer.vertex(m, 0, -s, 0).color(r, g, b, a).uv(len / s / 2F + mu, 0).endVertex();
				buffer.vertex(m, 0, s, 0).color(r, g, b, a).uv(len / s / 2F + mu, 1).endVertex();
				buffer.vertex(m, len, s, 0).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).uv(mu, 1).endVertex();
				buffer.vertex(m, len, -s, 0).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).uv(mu, 0).endVertex();
				tesselator.end();

				matrixStack.popPose();
			}
		}

		for (Widget widget : widgets) {
			if (!(widget instanceof QuestButton)) {
				continue;
			}

			Quest wquest = ((QuestButton) widget).quest;

			if (wquest.hideDependencyLines.get(false)) {
				continue;
			}

			for (QuestButton button : ((QuestButton) widget).getDependencies()) {
				int r, g, b, a;

				if (button.quest == selectedQuest) {
					Color4I c = ThemeProperties.DEPENDENCY_LINE_REQUIRED_FOR_COLOR.get(questScreen.selectedChapter);
					r = c.redi();
					g = c.greeni();
					b = c.bluei();
					a = c.alphai();
				} else if (wquest == selectedQuest) {
					Color4I c = ThemeProperties.DEPENDENCY_LINE_REQUIRES_COLOR.get(questScreen.selectedChapter);
					r = c.redi();
					g = c.greeni();
					b = c.bluei();
					a = c.alphai();
				} else {
					continue;
				}

				int sx = widget.getX() + widget.width / 2;
				int sy = widget.getY() + widget.height / 2;
				int ex = button.getX() + button.width / 2;
				int ey = button.getY() + button.height / 2;
				float len = (float) MathUtils.dist(sx, sy, ex, ey);

				matrixStack.pushPose();
				matrixStack.translate(sx, sy, 0);
				matrixStack.mulPose(Vector3f.ZP.rotation((float) Math.atan2(ey - sy, ex - sx)));
				Matrix4f m = matrixStack.last().pose();

				buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
				buffer.vertex(m, 0, -s, 0).color(r, g, b, a).uv(len / s / 2F + ms, 0).endVertex();
				buffer.vertex(m, 0, s, 0).color(r, g, b, a).uv(len / s / 2F + ms, 1).endVertex();
				buffer.vertex(m, len, s, 0).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).uv(ms, 1).endVertex();
				buffer.vertex(m, len, -s, 0).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).uv(ms, 0).endVertex();
				tesselator.end();

				matrixStack.popPose();
			}
		}

		RenderSystem.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		super.draw(matrixStack, theme, x, y, w, h);

		if (questScreen.selectedChapter != null && isMouseOver()) {
			//updateMinMax();

			double dx = (questMaxX - questMinX);
			double dy = (questMaxY - questMinY);

			double px = getX() - getScrollX();
			double py = getY() - getScrollY();

			double qx = (questScreen.getMouseX() - px) / questScreen.scrollWidth * dx + questMinX;
			double qy = (questScreen.getMouseY() - py) / questScreen.scrollHeight * dy + questMinY;
			centerQuestX = (questScreen.width / 2D - px) / questScreen.scrollWidth * dx + questMinX;
			centerQuestY = (questScreen.height / 2D - py) / questScreen.scrollHeight * dy + questMinY;

			if (isShiftKeyDown()) {
				questX = qx;
				questY = qy;
			} else if (questScreen.selectedObjects.size() == 1 && questScreen.selectedObjects.get(0) instanceof Quest) {
				Quest q = (Quest) questScreen.selectedObjects.get(0);
				double s = (1D / questScreen.file.gridScale) / q.size;
				questX = Mth.floor(qx * s + 0.5D) / s;
				questY = Mth.floor(qy * s + 0.5D) / s;
			} else {
				double s = 1D / questScreen.file.gridScale;
				questX = Mth.floor(qx * s + 0.5D) / s;
				questY = Mth.floor(qy * s + 0.5D) / s;
			}

			if (questScreen.file.canEdit()) {
				matrixStack.pushPose();
				matrixStack.translate(0, 0, 600);
				theme.drawString(matrixStack, "X:" + (questX < 0 ? "" : " ") + StringUtils.DOUBLE_FORMATTER_00.format(questX), x + 3, y + h - 18, Theme.SHADOW);
				theme.drawString(matrixStack, "Y:" + (questY < 0 ? "" : " ") + StringUtils.DOUBLE_FORMATTER_00.format(questY), x + 3, y + h - 10, Theme.SHADOW);
				theme.drawString(matrixStack, "CX:" + (centerQuestX < 0 ? "" : " ") + StringUtils.DOUBLE_FORMATTER_00.format(centerQuestX), x + w - 42, y + h - 18, Theme.SHADOW);
				theme.drawString(matrixStack, "CY:" + (centerQuestY < 0 ? "" : " ") + StringUtils.DOUBLE_FORMATTER_00.format(centerQuestY), x + w - 42, y + h - 10, Theme.SHADOW);
				matrixStack.popPose();

				double bs = questScreen.getQuestButtonSize();

				if (questScreen.movingObjects && !questScreen.selectedObjects.isEmpty()) {
					double ominX = Double.POSITIVE_INFINITY, ominY = Double.POSITIVE_INFINITY, omaxX = Double.NEGATIVE_INFINITY, omaxY = Double.NEGATIVE_INFINITY;

					for (Movable q : questScreen.selectedObjects) {
						ominX = Math.min(ominX, q.getX());
						ominY = Math.min(ominY, q.getY());
						omaxX = Math.max(omaxX, q.getX());
						omaxY = Math.max(omaxY, q.getY());
					}

					for (Movable m : questScreen.selectedObjects) {
						double ox = m.getX() - ominX;
						double oy = m.getY() - ominY;
						double sx = (questX + ox - questMinX) / dx * questScreen.scrollWidth + px;
						double sy = (questY + oy - questMinY) / dy * questScreen.scrollHeight + py;
						matrixStack.pushPose();
						matrixStack.translate(sx - bs * m.getWidth() / 2D, sy - bs * m.getHeight() / 2D, 0D);
						matrixStack.scale((float) (bs * m.getWidth()), (float) (bs * m.getHeight()), 1F);
						GuiHelper.setupDrawing();
						m.drawMoved(matrixStack);
						matrixStack.popPose();
					}

					if (QuestScreen.grid && questScreen.viewQuestPanel.quest == null) {
						double boxX = ominX / dx * questScreen.scrollWidth + px;
						double boxY = ominY / dy * questScreen.scrollHeight + py;
						double boxW = omaxX / dx * questScreen.scrollWidth + px - boxX;
						double boxH = omaxY / dy * questScreen.scrollHeight + py - boxY;

						matrixStack.pushPose();
						matrixStack.translate(0, 0, 1000);
						GuiHelper.drawHollowRect(matrixStack, (int) boxX, (int) boxY, (int) boxW, (int) boxH, Color4I.WHITE.withAlpha(30), false);
						matrixStack.popPose();
					}
				} else if (questScreen.viewQuestPanel.quest == null || !questScreen.viewQuestPanel.isMouseOver()) {
					//int z = treeGui.getZoom();
					double sx = (questX - questMinX) / dx * questScreen.scrollWidth + px;
					double sy = (questY - questMinY) / dy * questScreen.scrollHeight + py;
					matrixStack.pushPose();
					matrixStack.translate(sx - bs / 2D, sy - bs / 2D, 0D);
					matrixStack.scale((float) bs, (float) bs, 1F);
					GuiHelper.setupDrawing();
					RenderSystem.alphaFunc(GL11.GL_GREATER, 0.01F);
					QuestShape.get(questScreen.selectedChapter.getDefaultQuestShape()).shape.withColor(Color4I.WHITE.withAlpha(10)).draw(matrixStack, 0, 0, 1, 1);
					RenderSystem.defaultAlphaFunc();
					matrixStack.popPose();

					if (QuestScreen.grid && questScreen.viewQuestPanel.quest == null) {
						matrixStack.pushPose();
						matrixStack.translate(0, 0, 1000);
						Color4I.WHITE.draw(matrixStack, (int) sx, (int) sy, 1, 1);
						Color4I.WHITE.withAlpha(30).draw(matrixStack, getX(), (int) sy, width, 1);
						Color4I.WHITE.withAlpha(30).draw(matrixStack, (int) sx, getY(), 1, height);
						matrixStack.popPose();
					}
				}
			}
		}
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		if (questScreen.selectedChapter == null || questScreen.chapterPanel.isMouseOver()) {
			return false;
		}

		if (questScreen.movingObjects && questScreen.file.canEdit()) {
			if (questScreen.selectedChapter != null && !button.isRight() && !questScreen.selectedObjects.isEmpty()) {
				playClickSound();

				double minX = Double.POSITIVE_INFINITY;
				double minY = Double.POSITIVE_INFINITY;

				for (Movable q : questScreen.selectedObjects) {
					minX = Math.min(minX, q.getX());
					minY = Math.min(minY, q.getY());
				}

				for (Movable q : new ArrayList<>(questScreen.selectedObjects)) {
					q.move(questScreen.selectedChapter, questX + (q.getX() - minX), questY + (q.getY() - minY));
				}
			}

			questScreen.movingObjects = false;
			questScreen.selectedObjects.clear();
			return true;
		}

		if (super.mousePressed(button)) {
			return true;
		}

		if (!questScreen.viewQuestPanel.hidePanel && questScreen.getViewedQuest() != null) {
			questScreen.closeQuest();
			return true;
		}

		if (button.isLeft() && isMouseOver() && (questScreen.viewQuestPanel.hidePanel || questScreen.getViewedQuest() == null)) {
			questScreen.prevMouseX = getMouseX();
			questScreen.prevMouseY = getMouseY();
			questScreen.grabbed = 1;
			return true;
		}

		if (button.isRight() && questScreen.file.canEdit()) {
			playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			double qx = questX;
			double qy = questY;

			for (TaskType type : TaskTypes.TYPES.values()) {
				contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
					playClickSound();
					type.getGuiProvider().openCreationGui(this, new Quest(questScreen.selectedChapter), task -> new MessageCreateTaskAt(questScreen.selectedChapter, qx, qy, task).sendToServer());
				}));
			}

			contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.chapter.image"), GuiIcons.ART, () -> {
				playClickSound();
				ChapterImage image = new ChapterImage(questScreen.selectedChapter);
				image.x = qx;
				image.y = qy;
				questScreen.selectedChapter.images.add(image);
				new MessageEditObject(questScreen.selectedChapter).sendToServer();
			}));

			questScreen.openContextMenu(contextMenu);
			return true;
		}

		return false;
	}

	@Override
	public void mouseReleased(MouseButton button) {
		super.mouseReleased(button);
		questScreen.grabbed = 0;
	}

	@Override
	public boolean checkMouseOver(int mouseX, int mouseY) {
		if (questScreen.chapterPanel.expanded) {
			return false;
		}

		return super.checkMouseOver(mouseX, mouseY);
	}

	@Override
	public void updateMouseOver(int mouseX, int mouseY) {
		mouseOverQuest = null;
		super.updateMouseOver(mouseX, mouseY);

		for (Widget widget : widgets) {
			if (widget.isMouseOver() && widget instanceof QuestButton) {
				mouseOverQuest = (QuestButton) widget;
				break;
			}
		}
	}

	@Override
	public boolean keyPressed(Key key) {
		if (questScreen.selectedChapter != null && questScreen.getViewedQuest() == null && (key.is(GLFW.GLFW_KEY_MINUS) || key.is(GLFW.GLFW_KEY_EQUAL))) {
			questScreen.addZoom(key.is(GLFW.GLFW_KEY_MINUS) ? -1D : 1D);
			return true;
		}

		return super.keyPressed(key);
	}

	@Override
	public boolean scrollPanel(double scroll) {
		if (questScreen.selectedChapter != null && questScreen.getViewedQuest() == null && isMouseOver()) {
			questScreen.addZoom(scroll);
			return true;
		}

		return false;
	}
}