package dev.ftb.mods.ftbquests.gui.quests;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.PixelBuffer;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.MessageCreateObject;
import dev.ftb.mods.ftbquests.net.MessageEditObject;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
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
	public boolean checkMouseOver(int mouseX, int mouseY) {
		if (questScreen.movingObjects || questScreen.viewQuestPanel.isMouseOver() || questScreen.chapterPanel.expanded) {
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
					contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.add_dependencies"), ThemeProperties.ADD_ICON.get(), () -> {
						for (Movable q : questScreen.selectedObjects) {
							if (q instanceof Quest) {
								editDependency(quest, (Quest) q, true);
							}
						}
					}));

					contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.remove_dependencies"), ThemeProperties.DELETE_ICON.get(), () -> {
						for (Movable q : questScreen.selectedObjects) {
							if (q instanceof Quest) {
								editDependency(quest, (Quest) q, false);
							}
						}
					}));

					contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.add_dependencies_self"), ThemeProperties.ADD_ICON.get(), () -> {
						for (Movable q : questScreen.selectedObjects) {
							if (q instanceof Quest) {
								editDependency((Quest) q, quest, true);
							}
						}
					}));

					contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.remove_dependencies_self"), ThemeProperties.DELETE_ICON.get(), () -> {
						for (Movable q : questScreen.selectedObjects) {
							if (q instanceof Quest) {
								editDependency((Quest) q, quest, false);
							}
						}
					}));
				} else {
					contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.add_reward_all"), ThemeProperties.ADD_ICON.get(quest), () -> {
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
											new MessageCreateObject(r, extra).sendToServer();
										}
									}
								});
							}));
						}

						getGui().openContextMenu(contextMenu2);
					}));

					contextMenu.add(new ContextMenuItem(new TranslatableComponent("selectServer.delete"), ThemeProperties.DELETE_ICON.get(quest), () -> {
						questScreen.selectedObjects.forEach(q -> {
							if (q instanceof Quest) {
								ClientQuestFile.INSTANCE.deleteObject(((Quest) q).id);
							} else if (q instanceof ChapterImage) {
								((ChapterImage) q).chapter.images.remove(q);
								new MessageEditObject(((ChapterImage) q).chapter).sendToServer();
							}
						});
						questScreen.selectedObjects.clear();
					}).setYesNo(new TranslatableComponent("delete_item", new TranslatableComponent("ftbquests.quests") + " [" + questScreen.selectedObjects.size() + "]")));
				}

				contextMenu.add(ContextMenuItem.SEPARATOR);
				contextMenu.add(new ContextMenuItem(new TextComponent("Ctrl+A to select all quests").withStyle(ChatFormatting.GRAY), GuiIcons.INFO, null));
				contextMenu.add(new ContextMenuItem(new TextComponent("Ctrl+D to deselect all quests").withStyle(ChatFormatting.GRAY), GuiIcons.INFO, null));
				contextMenu.add(new ContextMenuItem(new TextComponent("Ctrl+Arrow Key to move selected quests").withStyle(ChatFormatting.GRAY), GuiIcons.INFO, null));
			} else {
				contextMenu.add(new ContextMenuItem(new TranslatableComponent("gui.move"), ThemeProperties.MOVE_UP_ICON.get(quest), () -> {
					questScreen.movingObjects = true;
					questScreen.selectedObjects.clear();
					questScreen.toggleSelected(quest);
				}) {
					@Override
					public void addMouseOverText(TooltipList list) {
						list.add(new TranslatableComponent("ftbquests.gui.move_tooltip").withStyle(ChatFormatting.DARK_GRAY));
					}
				});

				// contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.edit_text"), GuiIcons.INFO, () -> TextEditorFrame.open(quest)));

				contextMenu.add(ContextMenuItem.SEPARATOR);
				QuestScreen.addObjectMenuItems(contextMenu, getGui(), quest);
			}

			getGui().openContextMenu(contextMenu);
		} else if (button.isLeft()) {
			if (isCtrlKeyDown() && questScreen.file.canEdit()) {
				if (questScreen.getViewedQuest() != null) {
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
			new MessageEditObject(quest).sendToServer();
			questScreen.questPanel.refreshWidgets();
		} else {
			quest.dependencies.clear();
			quest.dependencies.addAll(prevDeps);
			QuestScreen.displayError(new TranslatableComponent("ftbquests.gui.looping_dependencies"));
		}
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse() {
		return quest.tasks.size() == 1 ? quest.tasks.get(0).getIngredient() : null;
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		Component title = getTitle();

		if (questScreen.file.self != null) {
			if (questScreen.file.self.isStarted(quest) && !questScreen.file.self.isCompleted(quest)) {
				title = title.copy().append(new TextComponent(" " + questScreen.file.self.getRelativeProgress(quest) + "%").withStyle(ChatFormatting.DARK_GRAY));
			}
		}

		list.add(title);

		Component description = quest.getSubtitle();

		if (description != TextComponent.EMPTY) {
			list.add(new TextComponent("").append(description).withStyle(ChatFormatting.GRAY));
		}

		if (quest.optional) {
			list.add(new TextComponent("[").withStyle(ChatFormatting.GRAY).append(new TranslatableComponent("ftbquests.quest.optional")).append("]"));
		}
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		Color4I outlineColor = Color4I.WHITE.withAlpha(150);
		Icon qicon = Icon.EMPTY;

		boolean cantStart = !questScreen.file.self.canStartTasks(quest);

		if (!cantStart) {
			if (questScreen.file.self.isCompleted(quest)) {
				if (questScreen.file.self.hasUnclaimedRewards(quest)) {
					qicon = ThemeProperties.ALERT_ICON.get(quest);
				} else {
					qicon = ThemeProperties.CHECK_ICON.get(quest);
				}

				outlineColor = ThemeProperties.QUEST_COMPLETED_COLOR.get(quest);
			} else if (questScreen.file.self.isStarted(quest)) {
				outlineColor = ThemeProperties.QUEST_STARTED_COLOR.get(quest);
			}
		} else {
			outlineColor = Color4I.GRAY;
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
		RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);

		if (quest == questScreen.viewQuestPanel.quest || questScreen.selectedObjects.contains(quest)) {
			matrixStack.pushPose();
			matrixStack.translate(0, 0, 200);
			Color4I col = Color4I.WHITE.withAlpha((int) (190D + Math.sin(System.currentTimeMillis() * 0.003D) * 50D));
			shape.outline.withColor(col).draw(matrixStack, x, y, w, h);
			shape.background.withColor(col).draw(matrixStack, x, y, w, h);
			matrixStack.popPose();
		}

		if (cantStart) {
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
			float s = w / 2F;//(int) (treeGui.getZoom() / 2 * quest.size);
			matrixStack.pushPose();
			matrixStack.translate(x + w - s, y, 200);
			matrixStack.scale(s, s, 1F);
			qicon.draw(matrixStack, 0, 0, 1, 1);
			matrixStack.popPose();
		}
	}
}