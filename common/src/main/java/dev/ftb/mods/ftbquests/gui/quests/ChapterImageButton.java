package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author LatvianModder
 */
public class ChapterImageButton extends Button implements QuestPositionableButton {
	private static WeakReference<ChapterImage> clipboard = new WeakReference<>(null);

	public QuestScreen questScreen;
	public ChapterImage chapterImage;

	public static Optional<ChapterImage> getClipboard() {
		ChapterImage img = clipboard.get();
		if (img != null) {
			if (!img.chapter.invalid) {
				return Optional.of(img);
			} else {
				clipboard = new WeakReference<>(null);
			}
		}
		return Optional.empty();
	}

	public ChapterImageButton(Panel panel, ChapterImage i) {
		super(panel, Component.empty(), i.getImage());
		questScreen = (QuestScreen) panel.getGui();
		setSize(20, 20);
		chapterImage = i;
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		if (isMouseOver()) {
			if (!chapterImage.click.isEmpty() || questScreen.file.canEdit() && !button.isLeft()) {
				onClicked(button);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean checkMouseOver(int mouseX, int mouseY) {
		if (questScreen.questPanel.mouseOverQuest != null || questScreen.movingObjects || questScreen.viewQuestPanel.isMouseOver() || questScreen.chapterPanel.isMouseOver()) {
			return false;
		}

		if (chapterImage.click.isEmpty() && !questScreen.file.canEdit()) {
			return false;
		}

		return super.checkMouseOver(mouseX, mouseY);
	}

	@Override
	public void onClicked(MouseButton button) {
		if (questScreen.file.canEdit() && button.isRight()) {
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.edit"), ThemeProperties.EDIT_ICON.get(), () -> {
				ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
				chapterImage.getConfig(group.getGroup("chapter").getGroup("image"));
				group.savedCallback = accepted -> {
					if (accepted) {
						new EditObjectMessage(chapterImage.chapter).sendToServer();
					}
					run();
				};
				new EditConfigScreen(group).openGui();
			}));

			contextMenu.add(new ContextMenuItem(Component.translatable("gui.move"), ThemeProperties.MOVE_UP_ICON.get(chapterImage.chapter), () -> {
				questScreen.movingObjects = true;
				questScreen.selectedObjects.clear();
				questScreen.toggleSelected(chapterImage);
			}) {
				@Override
				public void addMouseOverText(TooltipList list) {
					list.add(Component.translatable("ftbquests.gui.move_tooltip").withStyle(ChatFormatting.DARK_GRAY));
				}
			});

			contextMenu.add(new ContextMenuItem(Component.translatable("gui.copy"), Icons.INFO, () -> {
				clipboard = new WeakReference<>(chapterImage);
			}) {
				@Override
				public void addMouseOverText(TooltipList list) {
					list.add(Component.literal(chapterImage.getImage().toString()).withStyle(ChatFormatting.DARK_GRAY));
				}
			});

			if (chapterImage.isAspectRatioOff()) {
				contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.fix_aspect_ratio_w"), Icons.ART,
						() -> chapterImage.fixupAspectRatio(true)));
				contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.fix_aspect_ratio_h"), Icons.ART,
						() -> chapterImage.fixupAspectRatio(false)));
			}

			contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.delete"), ThemeProperties.DELETE_ICON.get(), () -> {
				chapterImage.chapter.images.remove(chapterImage);
				new EditObjectMessage(chapterImage.chapter).sendToServer();
			}).setYesNo(Component.translatable("delete_item", chapterImage.getImage().toString())));

			getGui().openContextMenu(contextMenu);
		} else if (button.isLeft()) {
			if (!chapterImage.click.isEmpty()) {
				playClickSound();
				handleClick(chapterImage.click);
			}
		} else if (questScreen.file.canEdit() && button.isMiddle()) {
			if (!questScreen.selectedObjects.contains(chapterImage)) {
				questScreen.toggleSelected(chapterImage);
			}

			questScreen.movingObjects = true;
		}
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		for (String s : chapterImage.hover) {
			list.add(TextUtils.parseRawText(s));
		}
	}

	@Override
	public boolean shouldDraw() {
		return false;
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		boolean transparent = chapterImage.dependency != null && !questScreen.file.self.isCompleted(chapterImage.dependency);
		Icon image = transparent ? chapterImage.getImage().withColor(Color4I.WHITE.withAlpha(100)) : chapterImage.getImage();

		GuiHelper.setupDrawing();
		matrixStack.pushPose();

		if (chapterImage.corner) {
			matrixStack.translate(x, y, 0);
			matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) chapterImage.rotation));
			matrixStack.scale(w, h, 1);
			image.draw(matrixStack, 0, 0, 1, 1);
		} else {
			matrixStack.translate((int) (x + w / 2D), (int) (y + h / 2D), 0);
			matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) chapterImage.rotation));
			matrixStack.scale(w / 2F, h / 2F, 1);
			image.draw(matrixStack, -1, -1, 2, 2);
		}

		matrixStack.popPose();
	}

	@Override
	public Position getPosition() {
		return new Position(chapterImage.x, chapterImage.y, chapterImage.width, chapterImage.height);
	}
}
