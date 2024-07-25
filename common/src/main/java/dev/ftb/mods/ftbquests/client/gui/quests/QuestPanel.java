package dev.ftb.mods.ftbquests.client.gui.quests;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.config.ui.SelectImageResourceScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftblibrary.math.MathUtils;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientConfig;
import dev.ftb.mods.ftbquests.net.*;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class QuestPanel extends Panel {
	private static final ImageIcon DEFAULT_DEPENDENCY_LINE_TEXTURE = (ImageIcon) Icon.getIcon(FTBQuestsAPI.MOD_ID + ":textures/gui/dependency.png");

	private final QuestScreen questScreen;
	protected double questX = 0;
	protected double questY = 0;
	double centerQuestX = 0;
	double centerQuestY = 0;
	QuestButton mouseOverQuest = null;
	private double questMinX, questMinY, questMaxX, questMaxY;

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
				.filter(image -> questScreen.file.canEdit() || image.shouldShowImage(questScreen.file.selfTeamData))
				.sorted(Comparator.comparingInt(ChapterImage::getOrder))
				.forEach(image -> add(new ChapterImageButton(this, image)));

		questScreen.selectedChapter.getQuests().forEach(quest -> add(new QuestButton(this, quest)));

		questScreen.selectedChapter.getQuestLinks().forEach(link -> link.getQuest().ifPresent(quest -> add(new QuestLinkButton(this, link, quest))));

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
			if (w instanceof QuestPositionableButton pos) {
				double qx = pos.getPosition().x();
				double qy = pos.getPosition().y();
				double qw = pos.getPosition().w();
				double qh = pos.getPosition().h();

				double x = (qx - questMinX - qw / 2D) * (bs + bp) + bp / 2D + bp * (qw - 1D) / 2D;
				double y = (qy - questMinY - qh / 2D) * (bs + bp) + bp / 2D + bp * (qh - 1D) / 2D;
				w.setPosAndSize((int) x, (int) y, (int) (bs * qw), (int) (bs * qh));
			}
		}

		setPosAndSize(20, 1, questScreen.width - 40, questScreen.height - 2);
	}

	@Override
	public void drawOffsetBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		if (questScreen.selectedChapter == null || questScreen.file.selfTeamData == null) {
			return;
		}

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuilder();

		Icon icon = ThemeProperties.DEPENDENCY_LINE_TEXTURE.get(questScreen.selectedChapter);
		if (icon instanceof ImageIcon img) {
			img.bindTexture();
		} else {
			DEFAULT_DEPENDENCY_LINE_TEXTURE.bindTexture();
		}

		Quest selectedQuest = questScreen.getViewedQuest();
		if (selectedQuest == null) {
			Collection<Quest> sel = questScreen.getSelectedQuests();
			if (sel.size() == 1) {
				selectedQuest = questScreen.getSelectedQuests().stream().findFirst().orElse(null);
			}
		}

		double mt = -(System.currentTimeMillis() * 0.001D);
		float lineWidth = (float) (questScreen.getZoom() * ThemeProperties.DEPENDENCY_LINE_THICKNESS.get(questScreen.selectedChapter) / 4D * 3D);

		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

		// pass 1: render connections for all visible quests
		float mu = (float) ((mt * ThemeProperties.DEPENDENCY_LINE_UNSELECTED_SPEED.get(questScreen.selectedChapter)) % 1D);
		for (Widget widget : widgets) {
			if (widget.shouldDraw() && widget instanceof QuestButton qb && !qb.quest.shouldHideDependencyLines()) {
				boolean unavailable = !questScreen.file.selfTeamData.canStartTasks(qb.quest);
				boolean complete = !unavailable && questScreen.file.selfTeamData.isCompleted(qb.quest);
				Color4I c = complete ?
						ThemeProperties.DEPENDENCY_LINE_COMPLETED_COLOR.get(questScreen.selectedChapter) :
						ThemeProperties.DEPENDENCY_LINE_UNCOMPLETED_COLOR.get(questScreen.selectedChapter);
				if (unavailable || qb.quest.getProgressionMode() == ProgressionMode.FLEXIBLE && !questScreen.file.selfTeamData.areDependenciesComplete(qb.quest)) {
					// dim connection lines for unavailable quests
					c = c.withAlpha(Math.max(30, c.alphai() / 2));
				}

				for (QuestButton button : qb.getDependencies()) {
					if (button.shouldDraw() && button.quest != selectedQuest && qb.quest != selectedQuest && !button.quest.shouldHideDependentLines()) {
						renderConnection(widget, button, graphics.pose(), buffer, lineWidth,
								c.redi(), c.greeni(), c.bluei(), c.alphai(), c.alphai(),
								mu, tesselator);
					}
				}
			}

		}

		// pass 2: render highlighted connections for hovered quest(s) dependencies/dependents
		float ms = (float) ((mt * ThemeProperties.DEPENDENCY_LINE_SELECTED_SPEED.get(questScreen.selectedChapter)) % 1D);
		List<QuestButton> toOutline = new ArrayList<>();
		for (Widget widget : widgets) {
			if (widget.shouldDraw() && widget instanceof QuestButton qb && !qb.quest.shouldHideDependencyLines()) {
				for (QuestButton button : qb.getDependencies()) {
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
							renderConnection(widget, button, graphics.pose(), buffer, lineWidth, c.redi(), c.greeni(), c.bluei(), a2, a, ms, tesselator);
						} else if (qb.quest == selectedQuest || qb.isMouseOver()) {
							Color4I c = ThemeProperties.DEPENDENCY_LINE_REQUIRES_COLOR.get(questScreen.selectedChapter);
							renderConnection(widget, button, graphics.pose(), buffer, lineWidth, c.redi(), c.greeni(), c.bluei(), c.alphai(), c.alphai(), ms, tesselator);
						}
					}
				}
			}

		}
		toOutline.forEach(qb -> {
			QuestShape.get(qb.quest.getShape()).getShape()
					.withColor(Color4I.BLACK.withAlpha(30))
					.draw(graphics, qb.getX(), qb.getY(), qb.width, qb.height);
			QuestShape.get(qb.quest.getShape()).getOutline()
					.withColor(Color4I.BLACK.withAlpha(90))
					.draw(graphics, qb.getX(), qb.getY(), qb.width, qb.height);
		});
	}

	private void renderConnection(Widget widget, QuestButton button, PoseStack poseStack, BufferBuilder buffer, float s, int r, int g, int b, int a, int a1, float mu, Tesselator tesselator) {
		int sx = widget.getX() + widget.width / 2;
		int sy = widget.getY() + widget.height / 2;
		int ex = button.getX() + button.width / 2;
		int ey = button.getY() + button.height / 2;
		float len = (float) MathUtils.dist(sx, sy, ex, ey);

		poseStack.pushPose();
		poseStack.translate(sx, sy, 0);
		poseStack.mulPose(Axis.ZP.rotation((float) Math.atan2(ey - sy, ex - sx)));
		Matrix4f m = poseStack.last().pose();

		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		buffer.vertex(m, 0, -s, 0).color(r, g, b, a).uv(len / s / 2F + mu, 0).endVertex();
		buffer.vertex(m, 0, s, 0).color(r, g, b, a).uv(len / s / 2F + mu, 1).endVertex();
		buffer.vertex(m, len, s, 0).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a1).uv(mu, 1).endVertex();
		buffer.vertex(m, len, -s, 0).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a1).uv(mu, 0).endVertex();
		tesselator.end();

		poseStack.popPose();
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		super.draw(graphics, theme, x, y, w, h);

		if (questScreen.selectedChapter != null && isMouseOver()) {
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
			} else {
				// grid-snapping size is based on the smallest selected item
				//   although images always act as if they were size 1
				double minSize = questScreen.selectedObjects.stream()
						.map(m -> m instanceof ChapterImage ? 1d : m.getWidth())
						.min(Double::compare)
						.orElse(1d);
				double snap = 1D / (questScreen.file.getGridScale() * minSize);
				questX = Mth.floor(qx * snap + 0.5D) / snap;
				questY = Mth.floor(qy * snap + 0.5D) / snap;
			}

			if (questScreen.file.canEdit()) {
				PoseStack poseStack = graphics.pose();

				drawStatusBar(graphics, theme, poseStack);

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
						poseStack.pushPose();
						poseStack.translate(sx - bs * m.getWidth() / 2D, sy - bs * m.getHeight() / 2D, 0D);
						poseStack.scale((float) (bs * m.getWidth()), (float) (bs * m.getHeight()), 1F);
						GuiHelper.setupDrawing();
						RenderSystem.enableDepthTest();
						m.drawMoved(graphics);
						poseStack.popPose();
					}

					if (QuestScreen.grid && !questScreen.isViewingQuest()) {
						double boxX = ominX / dx * questScreen.scrollWidth + px;
						double boxY = ominY / dy * questScreen.scrollHeight + py;
						double boxW = omaxX / dx * questScreen.scrollWidth + px - boxX;
						double boxH = omaxY / dy * questScreen.scrollHeight + py - boxY;

						poseStack.pushPose();
						poseStack.translate(0, 0, 200);
						GuiHelper.drawHollowRect(graphics, (int) boxX, (int) boxY, (int) boxW, (int) boxH, Color4I.WHITE.withAlpha(30), false);
						poseStack.popPose();
					}
				} else if (!questScreen.isViewingQuest() || !questScreen.viewQuestPanel.isMouseOver()) {
					//int z = treeGui.getZoom();
					double sx = (questX - questMinX) / dx * questScreen.scrollWidth + px;
					double sy = (questY - questMinY) / dy * questScreen.scrollHeight + py;
					poseStack.pushPose();
					poseStack.translate(sx - bs / 2D, sy - bs / 2D, 0D);
					poseStack.scale((float) bs, (float) bs, 1F);
					GuiHelper.setupDrawing();
					RenderSystem.enableDepthTest();
					// TODO: custom shader to implement alphaFunc? for now however, rendering outline at alpha 30 works well
					//RenderSystem.alphaFunc(GL11.GL_GREATER, 0.01F);
					QuestShape.get(questScreen.selectedChapter.getDefaultQuestShape()).getOutline().withColor(Color4I.WHITE.withAlpha(30)).draw(graphics, 0, 0, 1, 1);
					//RenderSystem.defaultAlphaFunc();
					poseStack.popPose();

					if (QuestScreen.grid && !questScreen.isViewingQuest()) {
						poseStack.pushPose();
						poseStack.translate(0, 0, 1000);
						Color4I.WHITE.draw(graphics, (int) sx, (int) sy, 1, 1);
						Color4I.WHITE.withAlpha(30).draw(graphics, getX(), (int) sy, width, 1);
						Color4I.WHITE.withAlpha(30).draw(graphics, (int) sx, getY(), 1, height);
						poseStack.popPose();
					}
				}
			}
		}
	}

	private void drawStatusBar(GuiGraphics graphics, Theme theme, PoseStack poseStack) {
		poseStack.pushPose();

		int statusX = questScreen.chapterPanel.expanded ? questScreen.chapterPanel.width : questScreen.expandChaptersButton.width;
		int statusWidth = questScreen.chapterPanel.expanded ? width - statusX + questScreen.expandChaptersButton.width : width;
		Color4I statPanelBg = ThemeProperties.WIDGET_BACKGROUND.get();
		Color4I.DARK_GRAY.draw(graphics, statusX, height - 9, statusWidth, 1);
		statPanelBg.draw(graphics, statusX, height - 9, statusWidth, 10);

		poseStack.translate(statusX, height - 6, 250);
		poseStack.scale(0.5f, 0.5f, 0.5f);

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

		poseStack.popPose();
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

		if (button.isRight() && questScreen.file.canEdit()) {
			playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			double qx = questX;
			double qy = questY;

			for (TaskType type : TaskTypes.TYPES.values()) {
				contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIconSupplier(), b -> {
					playClickSound();
					type.getGuiProvider().openCreationGui(this, new Quest(0L, questScreen.selectedChapter),
							(task, extra) -> {
								String str = task.getProtoTranslation(TranslationKey.TITLE);
								if (!str.isEmpty()) {
									questScreen.file.getTranslationManager().addInitialTranslation(extra, questScreen.file.getLocale(),
											TranslationKey.TITLE, task.getProtoTranslation(TranslationKey.TITLE));
								}
								new CreateTaskAtMessage(questScreen.selectedChapter, qx, qy, task, extra).sendToServer();
							}
					);
				}));
			}

			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.chapter.image"), Icons.ART, b -> showImageCreationScreen(qx, qy)));

			String clip = getClipboardString();
			if (!ChapterImage.isImageInClipboard()) {
				QuestObjectBase.parseHexId(clip).ifPresent(questId -> {
					QuestObject qo = questScreen.file.get(questId);
					contextMenu.add(ContextMenuItem.SEPARATOR);
					if (qo instanceof Quest quest) {
						contextMenu.add(new PasteQuestMenuItem(quest, Component.translatable("ftbquests.gui.paste"),
								Icons.ADD,
								b -> new CopyQuestMessage(quest, questScreen.selectedChapter, qx, qy, true).sendToServer()));
						if (quest.hasDependencies()) {
							contextMenu.add(new PasteQuestMenuItem(quest, Component.translatable("ftbquests.gui.paste_no_deps"),
									Icons.ADD_GRAY.withTint(Color4I.rgb(0x008000)),
									b -> new CopyQuestMessage(quest, questScreen.selectedChapter, qx, qy, false).sendToServer()));
						}
						contextMenu.add(new PasteQuestMenuItem(quest, Component.translatable("ftbquests.gui.paste_link"),
								Icons.ADD_GRAY.withTint(Color4I.rgb(0x8080C0)),
								b -> {
									QuestLink link = new QuestLink(0L, questScreen.selectedChapter, quest.id);
									link.setPosition(qx, qy);
									new CreateObjectMessage(link, new CompoundTag()).sendToServer();
								}));
					} else if (qo instanceof Task task) {
						contextMenu.add(new AddTaskButton.PasteTaskMenuItem(task, b -> copyAndCreateTask(task, qx, qy)));
					}
				});
			} else {
				ChapterImageButton.getClipboardImage().ifPresent(clipImg -> {
					contextMenu.add(ContextMenuItem.SEPARATOR);
					contextMenu.add(new TooltipContextMenuItem(Component.translatable("ftbquests.gui.paste_image"),
							Icons.ADD,
							b -> new CopyChapterImageMessage(clipImg, questScreen.selectedChapter, qx, qy).sendToServer(),
							Component.literal(clipImg.getImage().toString()).withStyle(ChatFormatting.GRAY)));
				});
			}

			questScreen.openContextMenu(contextMenu);
			return true;
		}

		return false;
	}

	private void showImageCreationScreen(double qx, double qy) {
		ImageResourceConfig imageConfig = new ImageResourceConfig();
		new SelectImageResourceScreen(imageConfig, accepted -> {
			if (accepted) {
				playClickSound();
				ChapterImage image = new ChapterImage(questScreen.selectedChapter)
						.setImage(Icon.getIcon(imageConfig.getValue()))
						.setPosition(qx, qy);
				image.fixupAspectRatio(true);
				questScreen.selectedChapter.addImage(image);
				new EditObjectMessage(questScreen.selectedChapter).sendToServer();
			}
			QuestPanel.this.questScreen.openGui();
		}).openGui();
	}

	private void copyAndCreateTask(Task task, double qx, double qy) {
		Task newTask = QuestObjectBase.copy(task, () -> TaskType.createTask(0L, new Quest(0L, questScreen.selectedChapter), task.getType().getTypeId().toString()));
		if (newTask != null) {
			new CreateTaskAtMessage(questScreen.selectedChapter, qx, qy, newTask, null).sendToServer();
		}
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
		if (questScreen.selectedChapter != null && !questScreen.isViewingQuest() && (key.is(GLFW.GLFW_KEY_MINUS) || key.is(GLFW.GLFW_KEY_EQUAL))) {
			questScreen.addZoom(key.is(GLFW.GLFW_KEY_MINUS) ? -1D : 1D);
			return true;
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

	private static class PasteQuestMenuItem extends TooltipContextMenuItem {
		public PasteQuestMenuItem(Quest quest, Component title, Icon icon, @Nullable Consumer<Button> callback) {
			super(title, icon, callback,
					Component.literal("\"").append(quest.getTitle()).append("\""),
					Component.literal(QuestObjectBase.getCodeString(quest.id)).withStyle(ChatFormatting.DARK_GRAY)
			);
		}
	}
}
