package dev.ftb.mods.ftbquests.client.gui.quests;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftblibrary.client.gui.WidgetType;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenuItem;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

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

	public ChapterImageButton(Panel panel, ChapterImage i) {
		super(panel, i.getTitle(), i.getImage());
		questScreen = (QuestScreen) panel.getGui();
		setSize(20, 20);
		chapterImage = i;
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
			return !button.isLeft() || button.isLeft() && Minecraft.getInstance().hasAltDown() || !chapterImage.getClickAction().isNone();
		}
		return false;
	}

	@Override
	public void onClicked(MouseButton button) {
		if (questScreen.questPanel.bezierController.isActive()) {
			return;
		}

		Component title = chapterImage.getTitle().getString().isEmpty() ?
				Component.literal(chapterImage.getImage().toString()) :
				chapterImage.getTitle();

		if (questScreen.file.canEdit() && button.isRight()) {
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			contextMenu.add(ContextMenuItem.title(title));
			contextMenu.add(ContextMenuItem.SEPARATOR);

			contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.edit"), ThemeProperties.EDIT_ICON.get(), _ -> chapterImage.onEditButtonClicked(questScreen)));

			if (!chapterImage.isPositionLocked()) {
				contextMenu.add(new ContextMenuItem(Component.translatable("gui.move"), ThemeProperties.MOVE_UP_ICON.get(chapterImage.getChapter()),
						_ -> questScreen.initiateMoving(chapterImage)) {
					@Override
					public void addMouseOverText(TooltipList list) {
						list.add(Component.translatable("ftbquests.gui.move_tooltip").withStyle(ChatFormatting.DARK_GRAY));
					}
				});
			}

			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.copy_id"), Icons.INFO, _ -> chapterImage.copyToClipboard()) {
				@Override
				public void addMouseOverText(TooltipList list) {
					list.add(Component.literal(chapterImage.getCodeString()).withStyle(ChatFormatting.DARK_GRAY));
				}
			});

			if (chapterImage.isAspectRatioOff()) {
				contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.fix_aspect_ratio_w"), Icons.ART,
                        _ -> chapterImage.fixupAspectRatio(true)));
				contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.fix_aspect_ratio_h"), Icons.ART,
                        _ -> chapterImage.fixupAspectRatio(false)));
			}

			int nSelected = questScreen.selectedObjects.size();
			Component yesNo = Component.translatable("delete_item", nSelected > 0 ?
					Component.translatable("ftbquests.objects", nSelected) :
					chapterImage.getTitle()
			);
			contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.delete"), ThemeProperties.DELETE_ICON.get(), _ -> handleDeletion()).setYesNoText(yesNo));

			getGui().openContextMenu(contextMenu);
		} else if (button.isLeft()) {
			if (Minecraft.getInstance().hasControlDown() && questScreen.file.canEdit()) {
				questScreen.toggleSelected(chapterImage);
			} else if (isKeyDown(InputConstants.KEY_LALT) && questScreen.file.canEdit()) {
				chapterImage.onEditButtonClicked(questScreen, title);
			} else if (isKeyDown(InputConstants.KEY_RALT) && questScreen.file.canEdit()) {
				chapterImage.copyToClipboard();
				FTBQuestsClient.showInfoToast(Component.translatable("ftbquests.quest.copied"), Component.literal(moveAndDeleteFocus().getTitle().getString()));
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
		if (!chapterImage.shouldDrawTextOnImage()) {
			TextUtils.processComponentWithPossibleNewlines(getTitle(), list::add);
		}
	}

	@Override
	public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
		Icon<?> image = chapterImage.getImage();

		// if we've got this far and the image shouldn't normally be drawn, we must be in edit mode
		boolean transparent = !chapterImage.shouldShowImage(FTBQuestsClient.getClientPlayerData());
		if (transparent) {
			image = image.withColor(Color4I.WHITE.withAlpha(100));
		} else if (!chapterImage.getColor().equals(Color4I.WHITE) || chapterImage.getAlpha() < 255) {
			image = image.withColor(chapterImage.getColor().withAlpha(chapterImage.getAlpha()));
		} else if (chapterImage.getImage().isEmpty() && chapterImage.getQuestFile().canEdit()) {
			image = Color4I.BLACK.withAlpha(40);
		}

		Matrix3x2fStack poseStack = graphics.pose();

		float offsetX = chapterImage.isAlignToCorner() ? 0 : w / 2F;
		float offsetY = chapterImage.isAlignToCorner() ? 0 : h / 2F;

		poseStack.pushMatrix();
		poseStack.translate(x + offsetX, y + offsetY);
		poseStack.rotate((float) (Mth.DEG_TO_RAD * chapterImage.getRotation()));
		poseStack.translate(-offsetX, -offsetY);
		poseStack.scale(w, h);
		IconHelper.renderIcon(image, graphics, 0, 0, 1, 1);
		if (questScreen.selectedObjects.contains(moveAndDeleteFocus())) {
			Color4I col = Color4I.WHITE.withAlpha((int) (128D + Math.sin(System.currentTimeMillis() * 0.003D) * 50D));
			IconHelper.renderIcon(col, graphics, 0, 0, 1, 1);
		}
		poseStack.scale(1F / w, 1F / h);
		maybeRenderText(graphics, theme, w, h);
		poseStack.popMatrix();
	}

    private void maybeRenderText(GuiGraphicsExtractor graphics, Theme theme, int w, int h) {
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
			graphics.pose().pushMatrix();
			if (inset != 0f) {
				graphics.pose().translate(w0 * inset, h0 * inset);
			}
			graphics.pose().scale(scale, scale);
			for (Component l : splitText) {
				float x1 = switch (chapterImage.getHorizontalTextAlign()) {
					case START -> 0;
					case MIDDLE -> (w - theme.getStringWidth(l) * scale) / 2;
					case END -> w - theme.getStringWidth(l) * scale;
				};
				theme.drawString(graphics, l, (int) (x1 / scale), (int) (y1 / scale), Color4I.WHITE, chapterImage.isTextShadow() ? Theme.SHADOW : 0);
				y1 += theme.getFontHeight() * scale;
			}
			graphics.pose().popMatrix();
		}
    }

	@Override
	public Position getPosition() {
		return new Position(chapterImage.getX(), chapterImage.getY(), chapterImage.getWidth(), chapterImage.getHeight());
	}

	@Override
	public int compareTo(Widget o) {
		return o instanceof ChapterImageButton cb2 ?
				Integer.compare(chapterImage.getOrder(), cb2.chapterImage.getOrder()) :
				0;
	}

	@Override
	public Movable moveAndDeleteFocus() {
		return chapterImage;
	}
}
