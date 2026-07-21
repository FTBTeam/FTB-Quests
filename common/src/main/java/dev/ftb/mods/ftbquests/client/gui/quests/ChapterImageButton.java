package dev.ftb.mods.ftbquests.client.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ChapterImageButton extends Button implements QuestPositionableButton {
	private final QuestScreen questScreen;
	private final ChapterImage chapterImage;

	@Nullable
	private List<Component> splitText = null;
	private float widestText = -1;

	private static final BiFunction<XYPair, Double, XYPair> MEMOIZED_ROTATE = Util.memoize((xy, rotateDeg) -> {
		// cartesian -> polar, rotate, polar -> cartesian
		double radius = xy.radius();
		double angle = xy.angle();
		double rotateRad = Math.toRadians(rotateDeg);
		// yes, negative is needed here
		return new XYPair(radius * Math.cos(angle - rotateRad), radius * Math.sin(angle - rotateRad));
	});
	private record XYPair(double x, double y) {
		double radius() {
			return Math.sqrt(x * x + y * y);
		}
		double angle() {
			return Math.atan2(y, x);
		}
	}

	public ChapterImageButton(Panel panel, ChapterImage chapterImage) {
		super(panel, chapterImage.getTitle(), chapterImage.getImage());

		questScreen = (QuestScreen) panel.getGui();
		setSize(20, 20);
		this.chapterImage = chapterImage;
		setDrawLayer(DrawLayer.BACKGROUND); // draw *before* connection lines & quest widgets
	}

	@Override
	public boolean checkMouseOver(int mouseX, int mouseY) {
        if (questScreen.questPanel.mouseOverQuest != null
				|| questScreen.movingObjects
				|| questScreen.viewQuestPanel.isMouseOver()
				|| questScreen.chapterPanel.isMouseOver()
				|| chapterImage.getClickAction().isNone() && !questScreen.file.canEdit()) {
            return false;
        }

		if (chapterImage.getRotation() != 0) {
			// need a bit of trig here, and we'll memoize it for performance
			// rotate the effective mouse position about either the corner or the center of the image
			double cx = chapterImage.isAlignToCorner() ? getX() : getX() + getWidth() / 2.0;
			double cy = chapterImage.isAlignToCorner() ? getY() : getY() + getHeight() / 2.0;

			XYPair rotated = MEMOIZED_ROTATE.apply(new XYPair(mouseX - cx, mouseY - cy), chapterImage.getRotation());
			mouseX = (int) (cx + rotated.x);
			mouseY = (int) (cy + rotated.y);
		}

        return super.checkMouseOver(mouseX, mouseY);
	}

	@Override
	public boolean mousePressed(MouseButton button) {
        if (isMouseOver() && getWidgetType() != WidgetType.DISABLED) {
			onClicked(button);
			// returning false on left button click allows click-through for panning behaviour
			//  (also, images with a click action defined should swallow the mouse click)
			return !button.isLeft() || button.isLeft() && Screen.hasAltDown() || !chapterImage.getClickAction().isNone();
		}
		return false;
	}

	@Override
	public void onClicked(MouseButton button) {
		if (questScreen.questPanel.bezierController.isActive()) {
			return;
		}

		if (questScreen.file.canEdit() && button.isRight()) {
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			contextMenu.add(ContextMenuItem.title(Component.literal("\"").append(chapterImage.getTitle()).append(Component.literal("\""))));
			contextMenu.add(ContextMenuItem.SEPARATOR);

			contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.edit"), ThemeProperties.EDIT_ICON.get(),
					b -> chapterImage.onEditButtonClicked(questScreen, title)));

			if (!chapterImage.isPositionLocked()) {
				contextMenu.add(new ContextMenuItem(Component.translatable("gui.move"), ThemeProperties.MOVE_UP_ICON.get(chapterImage.getChapter()),
						b -> questScreen.initiateMoving(chapterImage)) {
					@Override
					public void addMouseOverText(TooltipList list) {
						list.add(Component.translatable("ftbquests.gui.move_tooltip").withStyle(ChatFormatting.DARK_GRAY));
					}
				});
			}

			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.copy_id"), Icons.INFO, b -> chapterImage.copyToClipboard()) {
				@Override
				public void addMouseOverText(TooltipList list) {
					list.add(Component.literal(chapterImage.getCodeString()).withStyle(ChatFormatting.DARK_GRAY));
				}
			});

			if (chapterImage.isAspectRatioOff()) {
				contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.fix_aspect_ratio_w"), Icons.ART,
						b -> chapterImage.fixupAspectRatio(true)));
				contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.fix_aspect_ratio_h"), Icons.ART,
						b -> chapterImage.fixupAspectRatio(false)));
			}

			int nSelected = questScreen.selectedObjects.size();
			Component yesNo = Component.translatable("delete_item", nSelected > 0 ?
					Component.translatable("ftbquests.objects", nSelected) :
					Component.literal(chapterImage.getImage().toString())
			);
			contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.delete"), ThemeProperties.DELETE_ICON.get(),
					b -> handleDeletion()).setYesNoText(yesNo));

			getGui().openContextMenu(contextMenu);
		} else if (button.isLeft()) {
			if (Screen.hasControlDown() && questScreen.file.canEdit()) {
				questScreen.toggleSelected(chapterImage);
			} else if (Screen.hasAltDown() && questScreen.file.canEdit()) {
				chapterImage.onEditButtonClicked(questScreen, title);
			} else if (!chapterImage.getClickAction().isNone()) {
				playClickSound();
				chapterImage.getClickAction().run();
			}
		} else if (questScreen.file.canEdit() && button.isMiddle() && !chapterImage.isPositionLocked()) {
			if (!questScreen.selectedObjects.contains(chapterImage)) {
				questScreen.toggleSelected(chapterImage);
			}

			questScreen.movingObjects = true;
		}
	}

	private void handleDeletion() {
		if (questScreen.selectedObjects.isEmpty()) {
			questScreen.file.deleteObjects(List.of(chapterImage.getId()));
		} else {
			questScreen.deleteSelectedObjects();
		}
	}

	@Override
    public boolean collidesWith(int x, int y, int w, int h) {
        // small kludge: always try to render rotated images, even if they're off-screen
        // while it's possible to do extra calculations to determine the effective bounding area of a rotated image,
        //   it adds a lot of complexity for a relatively small benefit
        return chapterImage.getRotation() != 0 || super.collidesWith(x, y, w, h);
    }

	@Override
	public void addMouseOverText(TooltipList list) {
		TextUtils.processComponentWithPossibleNewlines(getTitle(), list::add);
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		Icon image = chapterImage.getImage();

		// if we've got this far and the image shouldn't normally be drawn, we must be in edit mode
		boolean transparent = !chapterImage.shouldShowImage(questScreen.file.selfTeamData);
		if (transparent) {
			image = image.withColor(Color4I.WHITE.withAlpha(100));
		} else if (!chapterImage.getColor().equals(Color4I.WHITE) || chapterImage.getAlpha() < 255) {
			image = image.withColor(chapterImage.getColor().withAlpha(chapterImage.getAlpha()));
		}

		PoseStack poseStack = graphics.pose();

		float offsetX = chapterImage.isAlignToCorner() ? 0 : w / 2F;
		float offsetY = chapterImage.isAlignToCorner() ? 0 : h / 2F;

		poseStack.pushPose();
		poseStack.translate(x + offsetX, y + offsetY, 0);
		poseStack.mulPose(Axis.ZP.rotationDegrees((float) chapterImage.getRotation()));
		poseStack.translate(-offsetX, -offsetY, 0);
		poseStack.scale(w, h, 1);
		image.draw(graphics, 0, 0, 1, 1);
		if (questScreen.selectedObjects.contains(moveAndDeleteFocus())) {
			Color4I col = Color4I.WHITE.withAlpha((int) (128D + Math.sin(System.currentTimeMillis() * 0.003D) * 50D));
			col.draw(graphics, 0, 0, 1, 1);
		}
		poseStack.scale(1F / w, 1F / h, 1);
		maybeRenderText(graphics, theme, w, h);
		poseStack.popPose();
	}

	private void maybeRenderText(GuiGraphics graphics, Theme theme, int w, int h) {
		if (chapterImage.shouldDrawTextOnImage()) {
			if (splitText == null || widestText < 0) {
				splitText = new ArrayList<>();
				TextUtils.processComponentWithPossibleNewlines(chapterImage.getTitle(), splitText::add);
				for (Component l : splitText) {
					widestText = Math.max(widestText, theme.getStringWidth(l));
				}
			}
			if (splitText.isEmpty() || widestText == 0f) {
				return;
			}
			float inset = chapterImage.getTextInset() / 100F;
			int w0 = w, h0 = h;
			if (inset != 0f) {
				w = Math.round(w * (1f - inset * 2f));
				h = Math.round(h * (1f - inset * 2f));
			}

			float scaleH = w / widestText;
			float height = theme.getFontHeight() * splitText.size();
			float scaleV = h / height;
			float scale = Math.min(scaleH, scaleV);

			float y1 = switch (chapterImage.getVerticalTextAlign()) {
				case START -> 0;
				case MIDDLE -> (h - height * scale) / 2;
				case END -> h - height * scale;
			};
			graphics.pose().pushPose();
			if (inset != 0f) {
				graphics.pose().translate(w0 * inset, h0 * inset, 0);
			}
			graphics.pose().scale(scale, scale, scale);
			for (Component l : splitText) {
				float x1 = switch (chapterImage.getHorizontalTextAlign()) {
					case START -> 0;
					case MIDDLE -> (w - theme.getStringWidth(l) * scale) / 2;
					case END -> w - theme.getStringWidth(l) * scale;
				};
				theme.drawString(graphics, l, (int) (x1 / scale), (int) (y1 / scale), Color4I.WHITE, chapterImage.isTextShadow() ? Theme.SHADOW : 0);
				y1 += theme.getFontHeight() * scale;
			}
			graphics.pose().popPose();
		}
	}

	@Override
	public Position getPosition() {
		return new Position(chapterImage.getX(), chapterImage.getY(), chapterImage.getWidth(), chapterImage.getHeight());
	}

	@Override
	public int compareTo(@NotNull Widget o) {
		return o instanceof ChapterImageButton cb2 ?
				Integer.compare(chapterImage.getOrder(), cb2.chapterImage.getOrder()) :
				0;
	}

	@Override
	public Movable moveAndDeleteFocus() {
		return chapterImage;
	}
}
