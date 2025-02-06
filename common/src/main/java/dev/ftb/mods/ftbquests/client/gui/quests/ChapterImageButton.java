package dev.ftb.mods.ftbquests.client.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class ChapterImageButton extends Button implements QuestPositionableButton {
	private final QuestScreen questScreen;
	private final ChapterImage chapterImage;

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
		super(panel, Component.empty(), i.getImage());
		questScreen = (QuestScreen) panel.getGui();
		setSize(20, 20);
		chapterImage = i;
		setDrawLayer(DrawLayer.BACKGROUND); // draw *before* connection lines & quest widgets
	}

	public static Optional<ChapterImage> getClipboardImage() {
		ChapterImage img = ChapterImage.clipboard.get();
		if (img != null) {
			if (img.getChapter().isValid()) {
				return Optional.of(img);
			} else {
				ChapterImage.clipboard = new WeakReference<>(null);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean checkMouseOver(int mouseX, int mouseY) {
        if (questScreen.questPanel.mouseOverQuest != null
				|| questScreen.movingObjects
				|| questScreen.viewQuestPanel.isMouseOver()
				|| questScreen.chapterPanel.isMouseOver()
				|| chapterImage.getClick().isEmpty() && !questScreen.file.canEdit()) {
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
			return !button.isLeft() || !chapterImage.getClick().isEmpty();
		}
		return false;
	}

	@Override
	public void onClicked(MouseButton button) {
		if (questScreen.file.canEdit() && button.isRight()) {
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			contextMenu.add(ContextMenuItem.title(Component.literal("\"").append(chapterImage.getTitle()).append(Component.literal("\""))));
			contextMenu.add(ContextMenuItem.SEPARATOR);

			contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.edit"), ThemeProperties.EDIT_ICON.get(), b -> {
				String name = chapterImage.getImage() instanceof Color4I ? chapterImage.getColor().toString() : chapterImage.getImage().toString();
				ConfigGroup group = new ConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> {
					if (accepted) {
						EditObjectMessage.sendToServer(chapterImage.getChapter());
					}
					run();
				}).setNameKey("Img: " + name);
				chapterImage.fillConfigGroup(group.getOrCreateSubgroup("chapter").getOrCreateSubgroup("image"));
				new EditConfigScreen(group).openGui();
			}));

			contextMenu.add(new ContextMenuItem(Component.translatable("gui.move"), ThemeProperties.MOVE_UP_ICON.get(chapterImage.getChapter()),
					b -> questScreen.initiateMoving(chapterImage)) {
				@Override
				public void addMouseOverText(TooltipList list) {
					list.add(Component.translatable("ftbquests.gui.move_tooltip").withStyle(ChatFormatting.DARK_GRAY));
				}
			});

			contextMenu.add(new ContextMenuItem(Component.translatable("gui.copy"), Icons.INFO, b -> chapterImage.copyToClipboard()) {
				@Override
				public void addMouseOverText(TooltipList list) {
					list.add(Component.literal(chapterImage.getImage().toString()).withStyle(ChatFormatting.DARK_GRAY));
				}
			});

			if (chapterImage.isAspectRatioOff()) {
				contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.fix_aspect_ratio_w"), Icons.ART,
						b -> chapterImage.fixupAspectRatio(true)));
				contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.fix_aspect_ratio_h"), Icons.ART,
						b -> chapterImage.fixupAspectRatio(false)));
			}

			contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.delete"), ThemeProperties.DELETE_ICON.get(), b -> {
				chapterImage.getChapter().removeImage(chapterImage);
				EditObjectMessage.sendToServer(chapterImage.getChapter());
			}).setYesNoText(Component.translatable("delete_item", chapterImage.getImage().toString())));

			getGui().openContextMenu(contextMenu);
		} else if (button.isLeft()) {
			if (Screen.hasControlDown() && questScreen.file.canEdit()) {
				questScreen.toggleSelected(chapterImage);
			} else if (!chapterImage.getClick().isEmpty()) {
				playClickSound();
				handleClick(chapterImage.getClick());
			}
		} else if (questScreen.file.canEdit() && button.isMiddle()) {
			if (!questScreen.selectedObjects.contains(chapterImage)) {
				questScreen.toggleSelected(chapterImage);
			}

			questScreen.movingObjects = true;
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
		chapterImage.addHoverText(list);
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
		poseStack.pushPose();

		if (chapterImage.isAlignToCorner()) {
			poseStack.translate(x, y, 0);
			poseStack.mulPose(Axis.ZP.rotationDegrees((float) chapterImage.getRotation()));
			poseStack.scale(w, h, 1);
			image.draw(graphics, 0, 0, 1, 1);
			if (questScreen.selectedObjects.contains(moveAndDeleteFocus())) {
				Color4I col = Color4I.WHITE.withAlpha((int) (128D + Math.sin(System.currentTimeMillis() * 0.003D) * 50D));
				col.draw(graphics, 0, 0, 1, 1);
			}
		} else {
			poseStack.translate((int) (x + w / 2D), (int) (y + h / 2D), 0);
			poseStack.mulPose(Axis.ZP.rotationDegrees((float) chapterImage.getRotation()));
			poseStack.scale(w / 2F, h / 2F, 1);
			image.draw(graphics, -1, -1, 2, 2);
			if (questScreen.selectedObjects.contains(moveAndDeleteFocus())) {
				Color4I col = Color4I.WHITE.withAlpha((int) (128D + Math.sin(System.currentTimeMillis() * 0.003D) * 50D));
				col.draw(graphics, -1, -1, 2, 2);
			}
		}

		poseStack.popPose();
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
