package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.config.DoubleConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
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
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class QuestButton extends Button implements QuestPositionableButton {
	public final QuestScreen questScreen;
	public final Quest quest;
	private Collection<QuestButton> dependencies = null;

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
		if (!shouldDraw() || questScreen.movingObjects || questScreen.viewQuestPanel.isMouseOver() || questScreen.chapterPanel.isMouseOver()) {
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

	public Collection<QuestButton> getDependencies() {
		if (dependencies == null) {
			List<QuestButton> list = new ArrayList<>();
			quest.getDependencies().forEach(dependency -> {
				if (!dependency.invalid && dependency instanceof Quest) {
					for (Widget widget : questScreen.questPanel.widgets) {
						if (widget instanceof QuestButton qb && dependency == qb.quest) {
							list.add(qb);
						}
					}
				}
			});
			dependencies = List.copyOf(list);
		}

		return dependencies;
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();

		if (questScreen.file.canEdit() && button.isRight()) {
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			Collection<Quest> selected = questScreen.getSelectedQuests();
			if (!selected.isEmpty()) {
				if (!selected.contains(quest)) {
					contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.add_dependencies"),
							ThemeProperties.ADD_ICON.get(),
							() -> selected.forEach(q -> editDependency(quest, q, true)))
					);
					contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.remove_dependencies"),
							ThemeProperties.DELETE_ICON.get(),
							() -> selected.forEach(q -> editDependency(quest, q, false)))
					);
					contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.add_dependencies_self"),
							ThemeProperties.ADD_ICON.get(),
							() -> selected.forEach(q -> editDependency(q, quest, true)))
					);
					contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.remove_dependencies_self"),
							ThemeProperties.DELETE_ICON.get(),
							() -> selected.forEach(q -> editDependency(q, quest, false)))
					);
				} else {
					contextMenu.add(new ContextMenuItem(Component.translatable("gui.move"),
							ThemeProperties.MOVE_UP_ICON.get(quest),
							() -> questScreen.movingObjects = true));
					contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.add_reward_all"),
							ThemeProperties.ADD_ICON.get(quest),
							this::openAddRewardContextMenu));
					contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.clear_reward_all"),
							ThemeProperties.CLOSE_ICON.get(quest),
							() -> selected.forEach(q -> q.rewards.forEach(r -> new DeleteObjectMessage(r.id).sendToServer()))));
					contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.bulk_change_size"),
							Icons.SETTINGS,
							this::bulkChangeSize));
					contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.delete"),
							ThemeProperties.DELETE_ICON.get(quest),
							this::deleteSelectedObjects)
							.setYesNo(Component.translatable("delete_item", Component.translatable("ftbquests.quests").append(" [" + questScreen.selectedObjects.size() + "]"))));
				}

				contextMenu.add(ContextMenuItem.SEPARATOR);
				contextMenu.add(new ContextMenuItem(Component.literal("Ctrl+A to select all quests").withStyle(ChatFormatting.GRAY), Icons.INFO, null).setCloseMenu(false));
				contextMenu.add(new ContextMenuItem(Component.literal("Ctrl+D to deselect all quests").withStyle(ChatFormatting.GRAY), Icons.INFO, null).setCloseMenu(false));
				contextMenu.add(new ContextMenuItem(Component.literal("Ctrl+Arrow Key to move selected quests").withStyle(ChatFormatting.GRAY), Icons.INFO, null).setCloseMenu(false));

				getGui().openContextMenu(contextMenu);
			} else {
				ContextMenuBuilder.create(theQuestObject(), questScreen)
						.withDeletionFocus(moveAndDeleteFocus())
						.insertAtTop(List.of(new TooltipContextMenuItem(Component.translatable("gui.move"),
								ThemeProperties.MOVE_UP_ICON.get(quest),
								() -> {
									questScreen.movingObjects = true;
									questScreen.selectedObjects.clear();
									questScreen.toggleSelected(moveAndDeleteFocus());
								},
								Component.translatable("ftbquests.gui.move_tooltip").withStyle(ChatFormatting.DARK_GRAY))
						))
						.openContextMenu(getGui());
			}
		} else if (button.isLeft()) {
			if (isCtrlKeyDown() && questScreen.file.canEdit()) {
				if (questScreen.isViewingQuest()) {
					questScreen.closeQuest();
				}
				questScreen.toggleSelected(moveAndDeleteFocus());
			} else if (!quest.guidePage.isEmpty() && quest.tasks.isEmpty() && quest.rewards.isEmpty() && quest.getDescription().length == 0) {
				handleClick("guide", quest.guidePage);
			} else {
				toggleQuestViewPanel();
			}
		} else if (questScreen.file.canEdit() && button.isMiddle()) {
			if (!questScreen.selectedObjects.contains(moveAndDeleteFocus())) {
				questScreen.toggleSelected(moveAndDeleteFocus());
			}

			questScreen.movingObjects = true;
		} else if (button.isRight()) {
			questScreen.movingObjects = false;
			toggleQuestViewPanel();
		}
	}

	private void toggleQuestViewPanel() {
		if (!questScreen.file.canEdit() && quest.hideDetailsUntilStartable() && !questScreen.file.self.canStartTasks(quest)) {
			return;
		}

		if (questScreen.getViewedQuest() != quest) {
			questScreen.open(theQuestObject(), false);
		} else {
			questScreen.closeQuest();
		}
	}

	private void bulkChangeSize() {
		Collection<Quest> quests = questScreen.getSelectedQuests();
		if (quests.isEmpty()) return;

		double val = quests.stream().findFirst().map(q -> q.size).orElse(1.0);
		var c = new DoubleConfig(0.0625D, 8D);

		EditConfigFromStringScreen.open(c, val, 1.0, Component.translatable("ftbquests.quest.size"), accepted -> {
			if (accepted) {
				quests.forEach(q -> {
					q.size = c.value;
					new EditObjectMessage(q).sendToServer();
				});
			}
			run();
		});
	}

	private void openAddRewardContextMenu() {
		List<ContextMenuItem> contextMenu2 = new ArrayList<>();

		for (RewardType type : RewardTypes.TYPES.values()) {
			contextMenu2.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
				playClickSound();
				type.getGuiProvider().openCreationGui(this, quest, reward -> questScreen.getSelectedQuests().forEach(quest -> {
					Reward r = type.provider.create(quest);
					CompoundTag nbt1 = new CompoundTag();
					reward.writeData(nbt1);
					r.readData(nbt1);
					CompoundTag extra = new CompoundTag();
					extra.putString("type", type.getTypeForNBT());
					new CreateObjectMessage(r, extra).sendToServer();
				}));
			}));
		}

		getGui().openContextMenu(contextMenu2);
	}

	private void deleteSelectedObjects() {
		questScreen.selectedObjects.forEach(movable -> {
			if (movable instanceof Quest q) {
				questScreen.file.deleteObject(q.id);
			} else if (movable instanceof QuestLink ql) {
				questScreen.file.deleteObject(ql.id);
			} else if (movable instanceof ChapterImage img) {
				img.chapter.images.remove(movable);
				new EditObjectMessage(img.chapter).sendToServer();
			}
		});
		questScreen.selectedObjects.clear();
	}

	private void editDependency(Quest quest, QuestObject object, boolean add) {
		List<QuestObject> prevDeps = quest.getDependencies().toList();

		if (add != quest.hasDependency(object)) {
			if (add) {
				quest.addDependency(object);
			} else {
				quest.removeDependency(object);
			}
		}

		quest.removeInvalidDependencies();

		if (quest.verifyDependencies(false)) {
			new EditObjectMessage(quest).sendToServer();
			questScreen.questPanel.refreshWidgets();
		} else {
			quest.clearDependencies();
			prevDeps.forEach(quest::addDependency);
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

		if (title.getString().contains("\n")) {
			// I'm not proud of this kludge but getting titles with embedded newlines and possible styling
			// to work well as tooltips is not fun
			title.visit((style, txt) -> {
				for (String s : txt.split("\n")) {
					if (!s.isEmpty()) list.add(Component.literal(s).withStyle(style));
				}
				return Optional.empty();
			}, title.getStyle());
		} else {
			list.add(title);
		}

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
		if (!questScreen.file.self.canStartTasks(quest)) {
			list.add(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("ftbquests.quest.locked")).append("]"));
		}
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		Color4I outlineColor = ThemeProperties.QUEST_NOT_STARTED_COLOR.get(quest);
		Icon questIcon = Color4I.EMPTY;
		Icon hiddenIcon = Color4I.EMPTY;

		TeamData teamData = questScreen.file.self;
		boolean isCompleted = teamData.isCompleted(quest);
		boolean isStarted = isCompleted || teamData.isStarted(quest);
		boolean canStart = isCompleted || isStarted || teamData.areDependenciesComplete(quest);
		Player player = Minecraft.getInstance().player;

		if (canStart) {
			if (isCompleted) {
				if (teamData.hasUnclaimedRewards(player.getUUID(), quest)) {
					questIcon = ThemeProperties.ALERT_ICON.get(quest);
				} else if (teamData.isQuestPinned(player, quest.id)) {
					questIcon = ThemeProperties.PIN_ICON_ON.get();
				} else {
					questIcon = ThemeProperties.CHECK_ICON.get(quest);
				}

				outlineColor = ThemeProperties.QUEST_COMPLETED_COLOR.get(quest);
			} else if (isStarted) {
				if (teamData.areDependenciesComplete(quest)) {
					outlineColor = ThemeProperties.QUEST_STARTED_COLOR.get(quest);
				}
				if (quest.getProgressionMode() == ProgressionMode.FLEXIBLE && quest.allTasksCompleted(teamData)) {
					questIcon = new ThemeProperties.CheckIcon(Color4I.rgb(0x606060), Color4I.rgb(0x808080));
				}
			}
		} else {
			outlineColor = ThemeProperties.QUEST_LOCKED_COLOR.get(quest);
		}

		if (questIcon == Color4I.EMPTY && teamData.isQuestPinned(player, quest.id)) {
			questIcon = ThemeProperties.PIN_ICON_ON.get();
		}
		if (questScreen.file.canEdit() && !quest.isVisible(teamData)) {
			hiddenIcon = ThemeProperties.HIDDEN_ICON.get();
		}

		QuestShape shape = QuestShape.get(getShape());

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

		if (questScreen.viewQuestPanel.viewingQuest(quest) || questScreen.selectedObjects.contains(moveAndDeleteFocus())) {
			matrixStack.pushPose();
			matrixStack.translate(0, 0, 200);
			Color4I col = Color4I.WHITE.withAlpha((int) (190D + Math.sin(System.currentTimeMillis() * 0.003D) * 50D));
			shape.outline.withColor(col).draw(matrixStack, x, y, w, h);
			shape.background.withColor(col).draw(matrixStack, x, y, w, h);
			matrixStack.popPose();
		}

		if (!canStart || !teamData.areDependenciesComplete(quest)) {
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

		if (!questIcon.isEmpty()) {
			float s = w / 8F * 3F;//(int) (treeGui.getZoom() / 2 * quest.size);
			matrixStack.pushPose();
			matrixStack.translate(x + w - s, y, 200);
			matrixStack.scale(s, s, 1F);
			questIcon.draw(matrixStack, 0, 0, 1, 1);
			matrixStack.popPose();
		}

		if (!hiddenIcon.isEmpty()) {
			float s = w / 8F * 3F;//(int) (treeGui.getZoom() / 2 * quest.size);
			matrixStack.pushPose();
			matrixStack.translate(x, y, 200);
			matrixStack.scale(s, s, 1F);
			hiddenIcon.draw(matrixStack, 0, 0, 1, 1);
			matrixStack.popPose();
		}
	}

	protected String getShape() {
		return quest.getShape();
	}

	/**
	 * Get the position at which the GUI button should be added, along with its size
	 * @return the GUI position and size
	 */
	@Override
	public Position getPosition() {
		return new Position(quest.x, quest.y, quest.size, quest.size);
	}

	/**
	 * This is the quest for regular quests, but the quest link (not the quest it links to) for quest links
	 * @return the object which should be moved or deleted via a GUI operation
	 */
	protected QuestObject theQuestObject() {
		return quest;
	}

	/**
	 * The focus object as a Movable (which will definitely be the case, so the cast is safe)
	 * @return a Movable; can be used for moving the button, and also deleting the quest object
	 */
	public Movable moveAndDeleteFocus() {
		return (Movable) theQuestObject();
	}
}
