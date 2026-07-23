package dev.ftb.mods.ftbquests.client.gui.quests;

import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableImageResource;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectImageResourceScreen;
import dev.ftb.mods.ftblibrary.client.gui.GuiHelper;
import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.*;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftblibrary.math.MathUtils;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftblibrary.util.Vec2d;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientConfig;
import dev.ftb.mods.ftbquests.client.FTBQuestsKeyMappings;
import dev.ftb.mods.ftbquests.mixin.GuiGraphicsMixin;
import dev.ftb.mods.ftbquests.net.*;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class QuestPanel extends Panel {
	private final QuestScreen questScreen;
	protected double questX = 0;
	protected double questY = 0;
	protected double questXunsnapped = 0;
	protected double questYunsnapped = 0;
	double centerQuestX = 0;
	double centerQuestY = 0;
	@Nullable
	QuestButton mouseOverQuest = null;
	double questMinX;
	double questMinY;
	private double questMaxX;
	private double questMaxY;
	final BezierController bezierController;

	public QuestPanel(Panel panel) {
		super(panel);
		questScreen = (QuestScreen) panel.getGui();
		bezierController = new BezierController();
	}

	public void updateMinMax() {
		questMinX = Double.POSITIVE_INFINITY;
		questMinY = Double.POSITIVE_INFINITY;
		questMaxX = Double.NEGATIVE_INFINITY;
		questMaxY = Double.NEGATIVE_INFINITY;

		for (Widget w : widgets) {
			if (w instanceof QuestPositionableButton qb) {
				double qx = qb.getPosition().x();
				double qy = qb.getPosition().y();
				double qw = qb.getPosition().w();
				double qh = qb.getPosition().h();
				questMinX = Math.min(questMinX, qx - qw / 2D);
				questMinY = Math.min(questMinY, qy - qh / 2D);
				questMaxX = Math.max(questMaxX, qx + qw / 2D);
				questMaxY = Math.max(questMaxY, qy + qh / 2D);
			}
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

	public void withPreservedPos(Consumer<QuestPanel> r) {
		double sx = centerQuestX;
		double sy = centerQuestY;
		r.accept(this);
		scrollTo(sx, sy);
	}

	@Override
	public void addWidgets() {
		if (questScreen.selectedChapter == null) {
			return;
		}

		questScreen.selectedChapter.getImages().stream()
				.filter(image -> questScreen.file.canEdit() || image.shouldShowImage(FTBQuestsClient.getClientPlayerData()))
				.sorted(Comparator.comparingInt(ChapterImage::getOrder))
				.forEach(image -> add(new ChapterImageButton(this, image)));

		questScreen.selectedChapter.getQuests().forEach(quest -> add(new QuestButton(this, quest)));

		questScreen.selectedChapter.getQuestLinks().forEach(link -> link.getQuest().ifPresent(quest -> add(new QuestLinkButton(this, link, quest))));

		add(bezierController.control0);
		add(bezierController.control1);
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
			if (w instanceof QuestPositionableButton pos) {
				double qx = pos.getPosition().x();
				double qy = pos.getPosition().y();
				double qw = pos.getPosition().w();
				double qh = pos.getPosition().h();

				double x = (qx - questMinX - qw / 2D) * (bs + bp) + bp / 2D + bp * (qw - 1D) / 2D;
				double y = (qy - questMinY - qh / 2D) * (bs + bp) + bp / 2D + bp * (qh - 1D) / 2D;
				w.setPosAndSize((int) Math.round(x), (int) Math.round(y), (int) Math.round(bs * qw), (int) Math.round(bs * qh));

				if (w instanceof QuestButton qb) {
					qb.positionControlPoints();
					bezierController.repositionControlButtons(qb);
				}
			}
		}

		setPosAndSize(20, 1, questScreen.width - 40, questScreen.height - 2);
	}

	@Override
	public void drawOffsetBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
		if (questScreen.selectedChapter == null) {
			return;
		}

		Icon<?> dependencyLineTexture = ThemeProperties.DEPENDENCY_LINE_TEXTURE.get(questScreen.selectedChapter);

		Quest selectedQuest = questScreen.getViewedQuest();
		if (selectedQuest == null) {
			Collection<Quest> sel = questScreen.getSelectedQuests();
			if (sel.size() == 1) {
				selectedQuest = questScreen.getSelectedQuests().stream().findFirst().orElse(null);
			}
		}

		double mt = -(System.currentTimeMillis() * 0.001D);
		float lineWidth = (float) (questScreen.getZoom() * ThemeProperties.DEPENDENCY_LINE_THICKNESS.get(questScreen.selectedChapter) / 4D * 3D);

		// pass 1: render connections for all visible quests
		float mu = (float) ((mt * ThemeProperties.DEPENDENCY_LINE_UNSELECTED_SPEED.get(questScreen.selectedChapter)) % 1D);
		for (Widget widget : widgets) {
			if (widget.shouldDraw() && widget instanceof QuestButton qb && (!qb.quest.shouldHideDependencyLines() || qb.isMouseOver())) {
				boolean unavailable = !questScreen.file.selfTeamData.canStartTasks(qb.quest);
				boolean complete = !unavailable && questScreen.file.selfTeamData.isCompleted(qb.quest);
				Color4I c = complete ?
						ThemeProperties.DEPENDENCY_LINE_COMPLETED_COLOR.get(questScreen.selectedChapter) :
						(unavailable ?
								ThemeProperties.DEPENDENCY_LINE_UNAVAILABLE_COLOR.get(questScreen.selectedChapter) :
								ThemeProperties.DEPENDENCY_LINE_UNCOMPLETED_COLOR.get(questScreen.selectedChapter)
						);

				for (QuestButton button : qb.getDependencies().values()) {
					if (button.shouldDraw() && button.quest != selectedQuest && qb.quest != selectedQuest && !button.quest.shouldHideDependentLines()) {
						renderConnection(graphics, dependencyLineTexture, qb, button, graphics.pose(), lineWidth,
								c.redi(), c.greeni(), c.bluei(), c.alphai(), c.alphai(),
								mu);
					}
				}
			}

		}

		// pass 2: render highlighted connections for hovered quest(s) dependencies/dependents
		float ms = (float) ((mt * ThemeProperties.DEPENDENCY_LINE_SELECTED_SPEED.get(questScreen.selectedChapter)) % 1D);
		List<QuestButton> toOutline = new ArrayList<>();
		for (Widget widget : widgets) {
			if (widget.shouldDraw() && widget instanceof QuestButton qb && (!qb.quest.shouldHideDependencyLines() || qb.isMouseOver())) {
				for (QuestButton button : qb.getDependencies().values()) {
					if (button.shouldDraw()) {
						if (button.quest == selectedQuest || button.isMouseOver()) {
							Color4I c = ThemeProperties.DEPENDENCY_LINE_REQUIRED_FOR_COLOR.get(questScreen.selectedChapter);
							int a, a2;
							if (qb.shouldDraw()) {
								a = a2 = c.alphai();
							} else {
								a = c.alphai() / 4 * 3;
								a2 = 30;
								toOutline.add(qb);
							}
							renderConnection(graphics, dependencyLineTexture, qb, button, graphics.pose(), lineWidth, c.redi(), c.greeni(), c.bluei(), a2, a, ms);
						} else if (qb.quest == selectedQuest || qb.isMouseOver()) {
							Color4I c = ThemeProperties.DEPENDENCY_LINE_REQUIRES_COLOR.get(questScreen.selectedChapter);
							renderConnection(graphics, dependencyLineTexture, qb, button, graphics.pose(), lineWidth, c.redi(), c.greeni(), c.bluei(), c.alphai(), c.alphai(), ms);
						}
					}
				}
			}

		}
		toOutline.forEach(qb -> {
			IconHelper.renderIcon(QuestShape.get(qb.quest.getShape()).getShape()
							.withColor(Color4I.BLACK.withAlpha(30)),
					graphics, qb.getX(), qb.getY(), qb.width, qb.height);
			IconHelper.renderIcon(QuestShape.get(qb.quest.getShape()).getOutline()
							.withColor(Color4I.BLACK.withAlpha(90)),
					graphics, qb.getX(), qb.getY(), qb.width, qb.height);
		});
	}

	private void renderConnection(GuiGraphicsExtractor graphics, Icon<?> dependencyLineTexture, QuestButton startWidget, QuestButton endWidget, Matrix3x2fStack poseStack, float s, int r, int g, int b, int a, int a1, float mu) {
		if (!(dependencyLineTexture instanceof ImageIcon icon)) {
			return;
		}

		float sx = startWidget.getX() + startWidget.width / 2.0f;
		float sy = startWidget.getY() + startWidget.height / 2.0f;
		float ex = endWidget.getX() + endWidget.width / 2.0f;
		float ey = endWidget.getY() + endWidget.height / 2.0f;
		float dist = (float) MathUtils.dist(sx, sy, ex, ey);

		var points = startWidget.getConnectionPoints(endWidget, dist);

		poseStack.pushMatrix();
		poseStack.translate(sx, sy);

		for (int i = 0; i < points.size() - 1; i++) {
			Vec2d p1 = points.get(i);
			Vec2d p2 = points.get(i + 1);
			float len = (float) (Math.sqrt(p1.distanceToSqr(p2)) * 1.04f);

			poseStack.pushMatrix();
			poseStack.translate((float) p1.x(), (float) p1.y());
			poseStack.rotate((float) Math.atan2(p2.y() - p1.y(), p2.x() - p1.x()));

			var texture = Minecraft.getInstance().getTextureManager().getTexture(icon.texture);
			GuiElementRenderState state = new BlitRenderState(
					RenderPipelines.GUI_TEXTURED,
					TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler()),
					new Matrix3x2f(poseStack),
					0, (int) -s,
					(int) len, (int) s,
					len / s / 2F + mu, mu,
					0f, 1f,
					ARGB.color(a, r, g, b),
					null  // TODO is a null scissor area OK here?
			);
			((GuiGraphicsMixin) graphics).getGuiRenderState().addGuiElement(state);

			poseStack.popMatrix();
		}
		poseStack.popMatrix();
	}

	@Override
	public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
		super.draw(graphics, theme, x, y, w, h);

		var poseStack = graphics.pose();

		if (questScreen.file.canEdit()) {
			drawStatusBar(graphics, theme, poseStack);
			questScreen.file.getChangelog().draw(graphics, questScreen);
		}

		if (questScreen.selectedChapter != null && isMouseOver()) {
			double dx = (questMaxX - questMinX);
			double dy = (questMaxY - questMinY);

			double px = getX() - getScrollX();
			double py = getY() - getScrollY();

			questXunsnapped = (questScreen.getMouseX() - px) / questScreen.scrollWidth * dx + questMinX;
			questYunsnapped = (questScreen.getMouseY() - py) / questScreen.scrollHeight * dy + questMinY;
			centerQuestX = (questScreen.width / 2D - px) / questScreen.scrollWidth * dx + questMinX;
			centerQuestY = (questScreen.height / 2D - py) / questScreen.scrollHeight * dy + questMinY;

			if (isShiftKeyDown()) {
				questX = questXunsnapped;
				questY = questYunsnapped;
			} else {
				// grid-snapping size is based on the smallest selected item
				//   although images always act as if they were size 1
				double minSize = questScreen.selectedObjects.stream()
						.map(m -> m instanceof ChapterImage ? 1d : m.getWidth())
						.min(Double::compare)
						.orElse(1d);
				double snap = 1D / (questScreen.file.getGridScale() * minSize);
				questX = Mth.floor(questXunsnapped * snap + 0.5D) / snap;
				questY = Mth.floor(questYunsnapped * snap + 0.5D) / snap;
			}

			if (questScreen.file.canEdit()) {
				if (bezierController.isActive()) {
					var bezierHint = Component.translatable("ftbquests.gui.editing_bezier");
					graphics.text(theme.getFont(), bezierHint, width - theme.getStringWidth(bezierHint), 4, 0xFFC0C0C0);
				}

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
						if (m.isPositionLocked()) continue;

						double ox = m.getX() - ominX;
						double oy = m.getY() - ominY;
						double sx = (questX + ox - questMinX) / dx * questScreen.scrollWidth + px;
						double sy = (questY + oy - questMinY) / dy * questScreen.scrollHeight + py;
						poseStack.pushMatrix();
						// translate/rotate order is highly dependent on whether the object aligns at center or corner.  fun fun fun
						if (m.isAlignToCorner()) {
							poseStack.translate((float) (sx - bs * m.getWidth() / 2D), (float) (sy - bs * m.getHeight() / 2D));
						} else {
							poseStack.translate((float) sx, (float) sy);
						}
						poseStack.rotate((float) (Mth.DEG_TO_RAD * m.getRotation()));
						if (!m.isAlignToCorner()) {
							poseStack.translate((float) (-bs * m.getWidth() / 2D), (float) (-bs * m.getHeight() / 2D));
						}
						poseStack.scale((float) (bs * m.getWidth()), (float) (bs * m.getHeight()));
						m.drawMoved(graphics);
						poseStack.popMatrix();
					}

					if (QuestScreen.grid && !questScreen.isViewingQuest()) {
						double boxX = ominX / dx * questScreen.scrollWidth + px;
						double boxY = ominY / dy * questScreen.scrollHeight + py;
						double boxW = omaxX / dx * questScreen.scrollWidth + px - boxX;
						double boxH = omaxY / dy * questScreen.scrollHeight + py - boxY;

						poseStack.pushMatrix();
						poseStack.translate(0, 0);//, 200);
						GuiHelper.drawHollowRect(graphics, (int) Math.round(boxX), (int) Math.round(boxY), (int) Math.round(boxW), (int) Math.round(boxH), Color4I.WHITE.withAlpha(30), false);
						poseStack.popMatrix();
					}
				} else if (!questScreen.isViewingQuest() || !questScreen.viewQuestPanel.isMouseOver()) {
					double sx = (questX - questMinX) / dx * questScreen.scrollWidth + px;
					double sy = (questY - questMinY) / dy * questScreen.scrollHeight + py;
					poseStack.pushMatrix();
					poseStack.translate((float) (sx - bs / 2D), (float) (sy - bs / 2D));
					poseStack.scale((float) bs, (float) bs);
					IconHelper.renderIcon(QuestShape.get(questScreen.selectedChapter.getDefaultQuestShape()).getOutline().withColor(Color4I.WHITE.withAlpha(30)), graphics, 0, 0, 1, 1);
					poseStack.popMatrix();

					if (QuestScreen.grid && !questScreen.isViewingQuest()) {
						poseStack.pushMatrix();
						poseStack.translate(0, 0);//, 1000);
						IconHelper.renderIcon(Color4I.WHITE, graphics, (int) Math.round(sx), (int) Math.round(sy), 1, 1);
						IconHelper.renderIcon(Color4I.WHITE.withAlpha(30), graphics, getX(), (int) sy, width, 1);
						IconHelper.renderIcon(Color4I.WHITE.withAlpha(30), graphics, (int) Math.round(sx), getY(), 1, height);
						poseStack.popMatrix();
					}
				}
			}
		}
	}

	private void drawStatusBar(GuiGraphicsExtractor graphics, Theme theme, Matrix3x2fStack poseStack) {
		if (questScreen.selectedChapter == null) {
			return;
		}

		poseStack.pushMatrix();

		int statusX = questScreen.chapterPanel.isExpanded() ? questScreen.chapterPanel.width : questScreen.expandChaptersButton.width;
		int statusWidth = questScreen.chapterPanel.isExpanded() ? width - statusX + questScreen.expandChaptersButton.width : width;
		Color4I statPanelBorder = ThemeProperties.WIDGET_BORDER.get(questScreen.selectedChapter);
		Color4I statPanelBg = ThemeProperties.WIDGET_BACKGROUND.get(questScreen.selectedChapter);
		IconHelper.renderIcon(statPanelBorder, graphics, statusX, height - 9, statusWidth, 1);
		IconHelper.renderIcon(statPanelBg, graphics, statusX, height - 9, statusWidth, 10);

		poseStack.translate(statusX, height - 6);
		poseStack.scale(0.5f, 0.5f);

		String curStr = String.format("Cursor: [%+.2f, %+.2f]", questX, questY);
		int pos = theme.drawString(graphics, curStr, 6, 0, Theme.SHADOW) + 25;

		int total = questScreen.selectedChapter.getQuests().size()
				+ questScreen.selectedChapter.getQuestLinks().size()
				+ questScreen.selectedChapter.getImages().size();
		String sStr = String.format("%s: %d/%d", (questScreen.movingObjects ? "Moving" : "Selected"), questScreen.selectedObjects.size(), total);
		pos = theme.drawString(graphics, sStr, pos, 0, Theme.SHADOW) + 25;

		String langStr = "Lang: " + questScreen.file.getLocale() + (FTBQuestsClientConfig.EDITING_LOCALE.get().isEmpty() ? " [Auto]" : "");
		theme.drawString(graphics, langStr, pos, 0, Theme.SHADOW);

		String cStr = String.format("Center: [%.2f, %.2f]", centerQuestX, centerQuestY);
		theme.drawString(graphics, cStr, statusWidth * 2 - theme.getStringWidth(cStr) - 6, 0, Theme.SHADOW);

		poseStack.popMatrix();
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

				for (Movable q : questScreen.selectedObjects) {
					q.requestMove(questScreen.selectedChapter, questX + (q.getX() - minX), questY + (q.getY() - minY));
				}
			}

			questScreen.movingObjects = false;
			questScreen.selectedObjects.clear();
			return true;
		}

		if (super.mousePressed(button)) {
			return true;
		}

		if (questScreen.isViewingQuest()) {
			questScreen.closeQuest();
			return true;
		}

		if ((button.isLeft() || button.isMiddle() && questScreen.file.canEdit()) && isMouseOver() && !questScreen.isViewingQuest()) {
			questScreen.prevMouseX = getMouseX();
			questScreen.prevMouseY = getMouseY();
			questScreen.grabbed = button;
			return true;
		}

		if (button.isRight() && questScreen.file.canEdit() && !bezierController.isActive()) {
			playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			double qx = questX;
			double qy = questY;

			for (TaskType type : TaskTypes.TYPES.values()) {
				contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIconSupplier(), b -> {
					playClickSound();
					type.getGuiProvider().openCreationGui(this, new Quest(0L, questScreen.selectedChapter),
							task -> Play2ServerNetworking.send(CreateQuestAndTaskMessage.requestCreation(questScreen.selectedChapter, qx, qy, task))
					);
				}));
			}

			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.image"), Icons.ART, _ -> showImageCreationScreen(qx, qy)));

			QuestObjectBase.parseHexId(getClipboardString()).ifPresent(questId -> {
				QuestObjectBase qo = questScreen.file.getBase(questId);
				switch (qo) {
					case Quest quest -> {
						contextMenu.add(ContextMenuItem.SEPARATOR);
						contextMenu.add(new PasteQuestMenuItem(quest, Component.translatable("ftbquests.gui.paste"),
								Icons.ADD,
								_ -> Play2ServerNetworking.send(new CopyQuestMessage(quest.id, questScreen.selectedChapter.id, qx, qy, true))));
						if (quest.hasDependencies()) {
							contextMenu.add(new PasteQuestMenuItem(quest, Component.translatable("ftbquests.gui.paste_no_deps"),
									Icons.ADD_GRAY.withTint(Color4I.rgb(0x008000)),
									_ -> Play2ServerNetworking.send(new CopyQuestMessage(quest.id, questScreen.selectedChapter.id, qx, qy, false))));
						}
						contextMenu.add(new PasteQuestMenuItem(quest, Component.translatable("ftbquests.gui.paste_link"),
								Icons.ADD_GRAY.withTint(Color4I.rgb(0x8080C0)),
								_ -> {
									QuestLink link = new QuestLink(0L, questScreen.selectedChapter, quest.id).setPosition(qx, qy);
									Play2ServerNetworking.send(CreateObjectMessage.requestCreation(link));
								}));
					}
					case Task task -> {
						contextMenu.add(ContextMenuItem.SEPARATOR);
						contextMenu.add(new AddTaskButton.PasteTaskMenuItem(task, _ -> copyAndCreateTask(task, qx, qy)));
					}
					case ChapterImage img -> {
						contextMenu.add(ContextMenuItem.SEPARATOR);
						contextMenu.add(new TooltipContextMenuItem(Component.translatable("ftbquests.gui.paste_image"),
								Icons.ADD,
								_ -> Play2ServerNetworking.send(new CopyChapterImageMessage(img.getId(), questScreen.selectedChapter.getId(), qx, qy)),
								Component.literal(img.getImage().toString()).withStyle(ChatFormatting.GRAY)));
					}
					case null, default -> {}
				}
			});

			questScreen.openContextMenu(contextMenu).setExtraZlevel(900);
			return true;
		}

		return false;
	}

	private void showImageCreationScreen(double qx, double qy) {
		if (questScreen.selectedChapter == null) {
			return;
		}
		EditableImageResource imageConfig = new EditableImageResource();
		new SelectImageResourceScreen(imageConfig, accepted -> {
			if (accepted) {
				playClickSound();
				ChapterImage image = new ChapterImage(0L, questScreen.selectedChapter)
						.setImage(Icon.getIcon(imageConfig.getValue()))
						.setPosition(qx, qy);
				image.fixupAspectRatio(true);
				Play2ServerNetworking.send(CreateObjectMessage.requestCreation(image));
			}
			QuestPanel.this.questScreen.openGui();
		}).openGui();
	}

	private void copyAndCreateTask(Task task, double qx, double qy) {
		if (questScreen.selectedChapter == null) {
			return;
		}
		Task newTask = QuestObjectBase.copy(task,
				() -> TaskType.createTask(0L, new Quest(0L, questScreen.selectedChapter), task.getType().getTypeId().toString()));
		Play2ServerNetworking.send(CreateQuestAndTaskMessage.requestCreation(questScreen.selectedChapter, qx, qy, newTask));
	}

	@Override
	public void mouseReleased(MouseButton button) {
		super.mouseReleased(button);

		if (questScreen.grabbed != null && questScreen.grabbed.isMiddle() && questScreen.file.canEdit()) {
			// select any quests in the box
			questScreen.selectAllQuestsInBox(getMouseX(), getMouseY(), getScrollX(), getScrollY());
		}

		questScreen.grabbed = null;
	}

	@Override
	public boolean checkMouseOver(int mouseX, int mouseY) {
		if (questScreen.chapterPanel.isMouseOver()) {
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
		if (key.matches(FTBQuestsKeyMappings.KEY_GUI_TOGGLE_CHANGELOG)) {
			FTBQuestsClientConfig.setAlwaysShowChangelog(!FTBQuestsClientConfig.CHANGELOG_ALWAYS_SHOW.get());
			return true;
		} else if (questScreen.selectedChapter != null && !questScreen.isViewingQuest()) {
			if (key.matches(FTBQuestsKeyMappings.KEY_GUI_ZOOM_IN)) {
				questScreen.addZoom(1D);
				return true;
			} else if (key.matches(FTBQuestsKeyMappings.KEY_GUI_ZOOM_OUT)) {
				questScreen.addZoom(-1D);
				return true;
			}
		}
		return super.keyPressed(key);
	}

	@Override
	public boolean scrollPanel(double scroll) {
		if (questScreen.selectedChapter != null && !questScreen.isViewingQuest() && isMouseOver()) {
			if (FTBQuestsClientConfig.OLD_SCROLL_WHEEL.get()) {
				questScreen.addZoom(scroll);
			} else {
				if (isShiftKeyDown()) {
					setScrollX(getScrollX() - scroll * 15);
				} else if (isCtrlKeyDown()) {
					questScreen.addZoom(scroll);
				} else {
					setScrollY(getScrollY() - scroll * 15);
				}
			}
			return true;
		}

		return false;
	}

	void editBezierControlPoints(QuestButton questButton, QuestObject dep) {
		if (dep instanceof Quest quest) {
			bezierController.activate(questButton, quest);
		}
	}

	void finishEditingBezierControlPoints(boolean accepted) {
		bezierController.deactivate(accepted);
	}

	public void clearBezierControlPoints(QuestButton qb, Quest dep) {
		bezierController.clear(qb, dep);
	}

	private static class PasteQuestMenuItem extends TooltipContextMenuItem {
		public PasteQuestMenuItem(Quest quest, Component title, Icon icon, @Nullable Consumer<Button> callback) {
			super(title, icon, callback,
					Component.literal("\"").append(quest.getTitle()).append("\""),
					Component.literal(QuestObjectBase.getCodeString(quest.id)).withStyle(ChatFormatting.DARK_GRAY)
			);
		}
	}

	class BezierController {
		private final ControlPointButton control0;
		private final ControlPointButton control1;

		@Nullable
		private Data data = null;

		private BezierController() {
			control0 = new ControlPointButton(QuestPanel.this, this, () -> Objects.requireNonNull(data).depButton, 0);
			control1 = new ControlPointButton(QuestPanel.this, this, () -> Objects.requireNonNull(data).questButton, 1);
		}

		public void activate(QuestButton questButton, Quest depQuest) {
			if (data == null) {
				data = new Data(
						questButton,
						questButton.getDependencies().get(depQuest.getId()),
						questButton.quest.getBezierControlPoints(depQuest).orElse(null)
				);

				var controlPoints = questButton.getControlPoints(depQuest);
				if (controlPoints == null) {
					// no control points yet - default to positions of button and its dependency button
					control0.activate(data.depButton.getPosX() + data.depButton.getWidth() / 2, data.depButton.getPosY() + data.depButton.getHeight() / 2);
					control1.activate(questButton.getPosX() + questButton.getWidth() / 2, questButton.getPosY() + questButton.getHeight() / 2);
				} else {
					control0.activate(controlPoints.getFirst());
					control1.activate(controlPoints.getSecond());
				}
			}
		}

		public void deactivate(boolean acceptChange) {
			if (data != null) {
				control0.deactivate(acceptChange);
				control1.deactivate(acceptChange);

				if (acceptChange) {
					Play2ServerNetworking.send(EditObjectMessage.forQuestObject(data.questButton.quest));
				} else {
					// operation canceled - restore to previous
					data.questButton.quest.setBezierControlPoints(data.depButton.quest, data.savedControlPoints);
					data.questButton.positionControlPoints();
				}
				data = null;
			}
		}

		public void updateQuestButton(int index) {
			// update quest-space control points in the quest object and recalculate screen-space coords
			if (data != null) {
				data.questButton.quest.setBezierControlPoint(data.depButton.quest, index, questXunsnapped, questYunsnapped);
				data.questButton.positionControlPoints();
			}
		}

		public boolean isActive() {
			return data != null;
		}

		public void clear(QuestButton qb, Quest dep) {
			qb.quest.setBezierControlPoints(dep, null);
			qb.positionControlPoints();

			Play2ServerNetworking.send(EditObjectMessage.forQuestObject(qb.quest));
		}

		public void repositionControlButtons(QuestButton qb) {
			if (data != null && data.questButton == qb) {
				Pair<Vec2d, Vec2d> controlPoints = qb.getControlPoints(data.depButton.quest);
				if (controlPoints != null) {
					control0.setPos((int) controlPoints.getFirst().x(), (int) controlPoints.getFirst().y());
					control1.setPos((int) controlPoints.getSecond().x(), (int) controlPoints.getSecond().y());
				}
			}
		}

		private record Data(QuestButton questButton, QuestButton depButton, @Nullable Pair<Vec2d, Vec2d> savedControlPoints) {
		}
	}

	private class ControlPointButton extends Button {
		private final BezierController controller;
		private final int index; // 0 or 1
		private final Supplier<QuestButton> questButtonSupplier;

		private boolean active = false;
		private Vec2d startPosition = Vec2d.ZERO;  // screen coords
		private boolean dragging = false;
		private int dragOffsetX, dragOffsetY;

		public ControlPointButton(Panel panel, BezierController controller, Supplier<QuestButton> questButtonSupplier, int index) {
			super(panel, Component.empty(), Icons.MARKER);

			this.controller = controller;
			this.questButtonSupplier = questButtonSupplier;
			this.index = index;

			setSize(12, 12);
		}

		@Override
		public boolean checkMouseOver(int mouseX, int mouseY) {
			return super.checkMouseOver(mouseX + width / 2, mouseY + width / 2);
		}

		@Override
		public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
		}

		@Override
		public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
			int x0 = getX();
			int y0 = getY();

			int x1 = questButtonSupplier.get().getX() + questButtonSupplier.get().getWidth() / 2;
			int y1 = questButtonSupplier.get().getY() + questButtonSupplier.get().getHeight() / 2;

			float angle = (float) Mth.atan2(y1 - y0, x1 - x0);
			float len = (float) Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));

			graphics.pose().pushMatrix();
			graphics.pose().translate(x0, y0);
			graphics.pose().rotate(angle);
			graphics.horizontalLine(0, (int) len, 0, 0x40A0A0FF);
			graphics.pose().popMatrix();

			super.draw(graphics, theme, x - w / 2, y - w / 2, w, h);
		}

		@Override
		public boolean mouseDragged(int button, double dragX, double dragY) {
			if (isEnabled() && dragging) {
				int newX = getMouseX() - dragOffsetX - QuestPanel.this.getX();
				int newY = getMouseY() - dragOffsetY - QuestPanel.this.getY();
				if (!BaseScreen.isShiftKeyDown()) {
					newX = newX - (newX % 8);
					newY = newY - (newY % 8);
				}
				setPos(newX, newY);
				controller.updateQuestButton(index);
				return true;
			} else {
				return super.mouseDragged(button, dragX, dragY);
			}
		}

		@Override
		public boolean mousePressed(MouseButton button) {
			if (isEnabled() && isMouseOver) {
				dragging = true;
				dragOffsetX = getMouseX() - getX();
				dragOffsetY = getMouseY() - getY();
				return true;
			}
			return super.mousePressed(button);
		}

		@Override
		public void mouseReleased(MouseButton button) {
			dragging = false;
		}

		@Override
		public void onClicked(MouseButton button) {
		}

		@Override
		public boolean isEnabled() {
			return active;
		}

		@Override
		public boolean shouldDraw() {
			return active;
		}

		private void activate(Vec2d position) {
			active = true;
			startPosition = position;
			setPos((int) Math.round(position.x()), (int) Math.round(position.y()));
		}

		private void activate(int x, int y) {
			activate(new Vec2d(x, y));
		}

		public void deactivate(boolean accepted) {
			if (!accepted) {
				// reset button position to what it was when we activated it
				setPos((int) Math.round(startPosition.x()), (int) Math.round(startPosition.y()));
			}
			active = false;
		}
	}
}
