package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.CreateObjectMessage;
import dev.ftb.mods.ftbquests.net.DeleteObjectMessage;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class QuestButton extends Button {
	public QuestScreen questScreen;
	public Quest quest;
	public QuestButton[] dependencies = null;

	public QuestButton(Panel panel, Quest q) {
		super(panel, q.getTitle(), q.getIcon());
		questScreen = (QuestScreen) panel.getGui();
		setSize(20, 20);
		quest = q;
	}

	@Override
	public boolean isEnabled() {
		return questScreen.file.canEdit() || quest.isVisible(questScreen.file.self);
	}

	@Override
	public boolean shouldDraw() {
		return questScreen.file.canEdit() || quest.isVisible(questScreen.file.self);
	}

	@Override
	public boolean checkMouseOver(int mouseX, int mouseY) {
		if (questScreen.movingObjects || questScreen.viewQuestPanel.isMouseOver() || questScreen.chapterPanel.isMouseOver()) {
			return false;
		}

		return super.checkMouseOver(mouseX, mouseY);
	}

	@Override
	public void updateMouseOver(int mouseX, int mouseY) {
		super.updateMouseOver(mouseX, mouseY);

		if (questScreen.questPanel.mouseOverQuest != null && questScreen.questPanel.mouseOverQuest != this) {
			isMouseOver = false;
		}

		if (isMouseOver) {
			QuestShape shape = QuestShape.get(quest.getShape());

			int ax = getX();
			int ay = getY();

			double relX = (mouseX - ax) / (double) width;
			double relY = (mouseY - ay) / (double) height;

			PixelBuffer pixelBuffer = shape.getShapePixels();

			int rx = (int) (relX * pixelBuffer.getWidth());
			int ry = (int) (relY * pixelBuffer.getHeight());

			if (rx < 0 || ry < 0 || rx >= pixelBuffer.getWidth() || ry >= pixelBuffer.getHeight()) {
				isMouseOver = false;
			} else {
				int a = (pixelBuffer.getRGB(rx, ry) >> 24) & 0xFF;

				if (a < 5) {
					isMouseOver = false;
				}
			}
		}

		if (isMouseOver && questScreen.questPanel.mouseOverQuest == null) {
			questScreen.questPanel.mouseOverQuest = this;
		}
	}

	public QuestButton[] getDependencies() {
		if (dependencies == null) {
			ArrayList<QuestButton> list = new ArrayList<>();

			for (QuestObject dependency : quest.dependencies) {
				if (!dependency.invalid && dependency instanceof Quest) {
					for (Widget widget : questScreen.questPanel.widgets) {
						if (widget instanceof QuestButton && dependency == ((QuestButton) widget).quest) {
							list.add((QuestButton) widget);
						}
					}
				}
			}

			dependencies = list.toArray(new QuestButton[0]);
		}

		return dependencies;
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();

		if (questScreen.file.canEdit() && button.isRight()) {
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			if (!questScreen.selectedObjects.isEmpty()) {
				if (!questScreen.selectedObjects.contains(quest)) {
					contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.add_dependencies"), ThemeProperties.ADD_ICON.get(), () -> {
						for (Movable q : questScreen.selectedObjects) {
							if (q instanceof Quest) {
								editDependency(quest, (Quest) q, true);
							}
						}
					}));

					contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.remove_dependencies"), ThemeProperties.DELETE_ICON.get(), () -> {
						for (Movable q : questScreen.selectedObjects) {
							if (q instanceof Quest) {
								editDependency(quest, (Quest) q, false);
							}
						}
					}));

					contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.add_dependencies_self"), ThemeProperties.ADD_ICON.get(), () -> {
						for (Movable q : questScreen.selectedObjects) {
							if (q instanceof Quest) {
								editDependency((Quest) q, quest, true);
							}
						}
					}));

					contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.remove_dependencies_self"), ThemeProperties.DELETE_ICON.get(), () -> {
						for (Movable q : questScreen.selectedObjects) {
							if (q instanceof Quest) {
								editDependency((Quest) q, quest, false);
							}
						}
					}));
				} else {
					contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.add_reward_all"), ThemeProperties.ADD_ICON.get(quest), () -> {
						List<ContextMenuItem> contextMenu2 = new ArrayList<>();

						for (RewardType type : RewardTypes.TYPES.values()) {
							contextMenu2.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
								playClickSound();
								type.getGuiProvider().openCreationGui(this, quest, reward -> {
									for (Movable movable : questScreen.selectedObjects) {
										if (movable instanceof Quest) {
											Reward r = type.provider.create((Quest) movable);
											CompoundTag nbt1 = new CompoundTag();
											reward.writeData(nbt1);
											r.readData(nbt1);
											CompoundTag extra = new CompoundTag();
											extra.putString("type", type.getTypeForNBT());
											new CreateObjectMessage(r, extra).sendToServer();
										}
									}
								});
							}));
						}

						getGui().openContextMenu(contextMenu2);
					}));

					contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.clear_reward_all"), ThemeProperties.CLOSE_ICON.get(quest), () -> {
						for (Movable movable : questScreen.selectedObjects) {
							if (movable instanceof Quest q) {
								q.rewards.forEach(r -> new DeleteObjectMessage(r.id).sendToServer());
							}
						}
					}));

					contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.delete"), ThemeProperties.DELETE_ICON.get(quest), () -> {
						questScreen.selectedObjects.forEach(q -> {
							if (q instanceof Quest) {
								ClientQuestFile.INSTANCE.deleteObject(((Quest) q).id);
							} else if (q instanceof ChapterImage) {
								((ChapterImage) q).chapter.images.remove(q);
								new EditObjectMessage(((ChapterImage) q).chapter).sendToServer();
							}
						});
						questScreen.selectedObjects.clear();
					}).setYesNo(Component.translatable("delete_item", Component.translatable("ftbquests.quests").append(" [" + questScreen.selectedObjects.size() + "]"))));
				}

				contextMenu.add(ContextMenuItem.SEPARATOR);
				contextMenu.add(new ContextMenuItem(Component.literal("Ctrl+A to select all quests").withStyle(ChatFormatting.GRAY), Icons.INFO, null).setCloseMenu(false));
				contextMenu.add(new ContextMenuItem(Component.literal("Ctrl+D to deselect all quests").withStyle(ChatFormatting.GRAY), Icons.INFO, null).setCloseMenu(false));
				contextMenu.add(new ContextMenuItem(Component.literal("Ctrl+Arrow Key to move selected quests").withStyle(ChatFormatting.GRAY), Icons.INFO, null).setCloseMenu(false));
			} else {
				contextMenu.add(new ContextMenuItem(Component.translatable("gui.move"), ThemeProperties.MOVE_UP_ICON.get(quest), () -> {
					questScreen.movingObjects = true;
					questScreen.selectedObjects.clear();
					questScreen.toggleSelected(quest);
				}) {
					@Override
					public void addMouseOverText(TooltipList list) {
						list.add(Component.translatable("ftbquests.gui.move_tooltip").withStyle(ChatFormatting.DARK_GRAY));
					}
				});

				// contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.edit_text"), GuiIcons.INFO, () -> TextEditorFrame.open(quest)));

//				contextMenu.add(ContextMenuItem.SEPARATOR);
				questScreen.addObjectMenuItems(contextMenu, getGui(), quest);
			}

			getGui().openContextMenu(contextMenu);
		} else if (button.isLeft()) {
			if (isCtrlKeyDown() && questScreen.file.canEdit()) {
				if (questScreen.isViewingQuest()) {
					questScreen.closeQuest();
				}

				questScreen.toggleSelected(quest);
			} else if (!quest.guidePage.isEmpty() && quest.tasks.isEmpty() && quest.rewards.isEmpty() && quest.getDescription().length == 0) {
				handleClick("guide", quest.guidePage);
			} else {
				questScreen.open(quest, false);
			}
		} else if (questScreen.file.canEdit() && button.isMiddle()) {
			if (!questScreen.selectedObjects.contains(quest)) {
				questScreen.toggleSelected(quest);
			}

			questScreen.movingObjects = true;
		} else if (button.isRight()) {
			questScreen.movingObjects = false;

			if (questScreen.getViewedQuest() != quest) {
				questScreen.viewQuestPanel.hidePanel = true;
				questScreen.viewQuest(quest);
			} else {
				questScreen.closeQuest();
			}
		}
	}

	private void editDependency(Quest quest, QuestObject object, boolean add) {
		List<QuestObject> prevDeps = new ArrayList<>(quest.dependencies);

		if (add != quest.hasDependency(object)) {
			if (add) {
				quest.dependencies.add(object);
			} else {
				quest.dependencies.remove(object);
			}
		}

		quest.removeInvalidDependencies();

		if (quest.verifyDependencies(false)) {
			new EditObjectMessage(quest).sendToServer();
			questScreen.questPanel.refreshWidgets();
		} else {
			quest.dependencies.clear();
			quest.dependencies.addAll(prevDeps);
			QuestScreen.displayError(Component.translatable("ftbquests.gui.looping_dependencies"));
		}
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse() {
		return quest.tasks.size() == 1 ? quest.tasks.get(0).getIngredient() : null;
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		questScreen.addInfoTooltip(list, quest);

		Component title = getTitle();

		if (questScreen.file.self != null) {
			if (questScreen.file.self.isStarted(quest) && !questScreen.file.self.isCompleted(quest)) {
				title = title.copy().append(Component.literal(" " + questScreen.file.self.getRelativeProgress(quest) + "%").withStyle(ChatFormatting.DARK_GRAY));
			}
		}

		list.add(title);

		Component description = quest.getSubtitle();

		if (description.getContents() != ComponentContents.EMPTY) {
			list.add(Component.literal("").append(description).withStyle(ChatFormatting.GRAY));
		}

		if (quest.optional) {
			list.add(Component.literal("[").withStyle(ChatFormatting.GRAY).append(Component.translatable("ftbquests.quest.optional")).append("]"));
		}
		if (quest.canRepeat) {
			list.add(Component.translatable("ftbquests.quest.can_repeat").withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		Color4I outlineColor = Color4I.WHITE.withAlpha(150);
		Icon qicon = Icon.EMPTY;
		Icon hicon = Icon.EMPTY;

		boolean isCompleted = questScreen.file.self.isCompleted(quest);
		boolean isStarted = isCompleted || questScreen.file.self.isStarted(quest);
		boolean canStart = isCompleted || isStarted || questScreen.file.self.canStartTasks(quest);

		if (canStart) {
			if (isCompleted) {
				if (questScreen.file.self.hasUnclaimedRewards(Minecraft.getInstance().player.getUUID(), quest)) {
					qicon = ThemeProperties.ALERT_ICON.get(quest);
				} else if (questScreen.file.self.isQuestPinned(quest.id)) {
					qicon = ThemeProperties.PIN_ICON_ON.get();
				} else {
					qicon = ThemeProperties.CHECK_ICON.get(quest);
				}

				outlineColor = ThemeProperties.QUEST_COMPLETED_COLOR.get(quest);
			} else if (isStarted) {
				outlineColor = ThemeProperties.QUEST_STARTED_COLOR.get(quest);
			}
		} else {
			outlineColor = Color4I.GRAY;
		}

		if (qicon == Icon.EMPTY && questScreen.file.self.isQuestPinned(quest.id)) {
			qicon = ThemeProperties.PIN_ICON_ON.get();
		}
		if (questScreen.file.canEdit() && !quest.isVisible(questScreen.file.self)) {
			hicon = ThemeProperties.HIDDEN_ICON.get();
		}

		QuestShape shape = QuestShape.get(quest.getShape());

		shape.shape.withColor(Color4I.DARK_GRAY).draw(matrixStack, x, y, w, h);
		shape.background.withColor(Color4I.WHITE.withAlpha(150)).draw(matrixStack, x, y, w, h);
		shape.outline.withColor(outlineColor).draw(matrixStack, x, y, w, h);

		if (!icon.isEmpty()) {
			float s = w * 2F / 3F;
			matrixStack.pushPose();
			matrixStack.translate(x + (w - s) / 2D, y + (h - s) / 2D, 0);
			matrixStack.scale(s, s, 1F);
			icon.draw(matrixStack, 0, 0, 1, 1);
			matrixStack.popPose();
		}

		GuiHelper.setupDrawing();
		// TODO: custom shader to implement alphaFunc?
		//RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);

		if (quest == questScreen.viewQuestPanel.quest || questScreen.selectedObjects.contains(quest)) {
			matrixStack.pushPose();
			matrixStack.translate(0, 0, 200);
			Color4I col = Color4I.WHITE.withAlpha((int) (190D + Math.sin(System.currentTimeMillis() * 0.003D) * 50D));
			shape.outline.withColor(col).draw(matrixStack, x, y, w, h);
			shape.background.withColor(col).draw(matrixStack, x, y, w, h);
			matrixStack.popPose();
		}

		if (!canStart) {
			matrixStack.pushPose();
			matrixStack.translate(0, 0, 200);
			shape.shape.withColor(Color4I.BLACK.withAlpha(100)).draw(matrixStack, x, y, w, h);
			matrixStack.popPose();
		}

		if (isMouseOver()) {
			matrixStack.pushPose();
			matrixStack.translate(0, 0, 200);
			shape.shape.withColor(Color4I.WHITE.withAlpha(100)).draw(matrixStack, x, y, w, h);
			matrixStack.popPose();
		}

		if (!qicon.isEmpty()) {
			float s = w / 8F * 3F;//(int) (treeGui.getZoom() / 2 * quest.size);
			matrixStack.pushPose();
			matrixStack.translate(x + w - s, y, 200);
			matrixStack.scale(s, s, 1F);
			qicon.draw(matrixStack, 0, 0, 1, 1);
			matrixStack.popPose();
		}

		if (!hicon.isEmpty()) {
			float s = w / 8F * 3F;//(int) (treeGui.getZoom() / 2 * quest.size);
			matrixStack.pushPose();
			matrixStack.translate(x, y, 200);
			matrixStack.scale(s, s, 1F);
			hicon.draw(matrixStack, 0, 0, 1, 1);
			matrixStack.popPose();
		}
	}
}
