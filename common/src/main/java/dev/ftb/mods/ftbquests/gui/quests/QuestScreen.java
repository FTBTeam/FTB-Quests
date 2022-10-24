package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.config.ConfigWithVariants;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.math.MathUtils;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.gui.FTBQuestsTheme;
import dev.ftb.mods.ftbquests.gui.SelectQuestObjectScreen;
import dev.ftb.mods.ftbquests.net.ChangeProgressMessage;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.reward.RandomReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuestScreen extends BaseScreen {
	public final ClientQuestFile file;
	public double scrollWidth, scrollHeight;
	public int prevMouseX, prevMouseY;
	public MouseButton grabbed = null;
	public Chapter selectedChapter;
	public final List<Movable> selectedObjects;
	public final ExpandChaptersButton expandChaptersButton;
	public final ChapterPanel chapterPanel;
	public final QuestPanel questPanel;
	public final OtherButtonsPanelBottom otherButtonsBottomPanel;
	public final OtherButtonsPanelTop otherButtonsTopPanel;
	public final ViewQuestPanel viewQuestPanel;
	public boolean movingObjects = false;
	public int zoom = 16;
	public long lastShiftPress = 0L;
	public static boolean grid = false;

	public QuestScreen(ClientQuestFile q) {
		file = q;
		selectedObjects = new ArrayList<>();

		expandChaptersButton = new ExpandChaptersButton(this);
		chapterPanel = new ChapterPanel(this);
		selectedChapter = file.getFirstVisibleChapter(file.self);

		questPanel = new QuestPanel(this);
		otherButtonsBottomPanel = new OtherButtonsPanelBottom(this);
		otherButtonsTopPanel = new OtherButtonsPanelTop(this);
		viewQuestPanel = new ViewQuestPanel(this);

		selectChapter(null);
	}

	@Nullable
	public Quest getViewedQuest() {
		return viewQuestPanel.quest;
	}

	public boolean isViewingQuest() {
		return getViewedQuest() != null;
	}

	@Override
	public void addWidgets() {
		QuestTheme.currentObject = selectedChapter;
		add(questPanel);
		add(chapterPanel);
		add(expandChaptersButton);
		add(otherButtonsBottomPanel);
		add(otherButtonsTopPanel);
		add(viewQuestPanel);
	}

	@Override
	public void alignWidgets() {
		QuestTheme.currentObject = selectedChapter;
		otherButtonsBottomPanel.alignWidgets();
		otherButtonsTopPanel.alignWidgets();
		chapterPanel.alignWidgets();
		expandChaptersButton.setPosAndSize(0, 0, 20, height);
	}

	@Override
	public boolean onInit() {
		//Keyboard.enableRepeatEvents(true);
		return setFullscreen();
	}

	@Override
	public void onClosed() {
		// selectedObjects.clear();
		super.onClosed();
		//Keyboard.enableRepeatEvents(false);
	}

	public void selectChapter(@Nullable Chapter chapter) {
		if (selectedChapter != chapter) {
			//movingQuests = false;
			closeQuest();
			selectedChapter = chapter;
			questPanel.refreshWidgets();
			questPanel.resetScroll();
		}
	}

	public void viewQuest(Quest quest) {
		//selectedQuests.clear();

		if (viewQuestPanel.quest != quest) {
			viewQuestPanel.quest = quest;
			viewQuestPanel.refreshWidgets();
		}
	}

	@Override
	public void onBack() {
		if (isViewingQuest()) {
			closeQuest();
		} else {
			super.onBack();
		}
	}

	public void closeQuest() {
		//selectedQuests.clear();

		if (viewQuestPanel.quest != null) {
			viewQuestPanel.quest = null;
			viewQuestPanel.hidePanel = false;
			viewQuestPanel.refreshWidgets();
		}
	}

	public void toggleSelected(Movable movable) {
		if (viewQuestPanel.quest != null) {
			viewQuestPanel.quest = null;
			viewQuestPanel.refreshWidgets();
		}

		if (selectedObjects.contains(movable)) {
			selectedObjects.remove(movable);
		} else {
			selectedObjects.add(movable);
		}
	}

	public void addObjectMenuItems(List<ContextMenuItem> contextMenu, Runnable gui, QuestObjectBase object) {
		ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
		ConfigGroup g = object.createSubGroup(group);
		object.getConfig(g);

		contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.edit"), ThemeProperties.EDIT_ICON.get(), () -> object.onEditButtonClicked(gui)));
		contextMenu.add(ContextMenuItem.SEPARATOR);

		if (!g.getValues().isEmpty()) {
			List<ContextMenuItem> list = new ArrayList<>();

			for (ConfigValue c : g.getValues()) {
				if (c instanceof ConfigWithVariants) {
					MutableComponent name = Component.translatable(c.getNameKey());

					if (!c.getCanEdit()) {
						name = name.withStyle(ChatFormatting.GRAY);
					}

					list.add(new ContextMenuItem(name, Icons.SETTINGS, null) {
						@Override
						public void addMouseOverText(TooltipList list) {
							list.add(c.getStringForGUI(c.value));
						}

						@Override
						public void onClicked(Panel panel, MouseButton button) {
							c.onClicked(button, accepted -> {
								if (accepted) {
									c.setter.accept(c.value);
									new EditObjectMessage(object).sendToServer();
								}
							});
						}

						@Override
						public void drawIcon(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
							c.getIcon(c.value).draw(matrixStack, x, y, w, h);
						}
					});
				}
			}

			if (!list.isEmpty()) {
				list.sort(null);
				contextMenu.addAll(list);
				contextMenu.add(ContextMenuItem.SEPARATOR);
			}
		}

		if (object instanceof RandomReward && !QuestObjectBase.isNull(((RandomReward) object).getTable())) {
			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.reward_table.edit"), ThemeProperties.EDIT_ICON.get(), () -> ((RandomReward) object).getTable().onEditButtonClicked(gui)));
		}

		ContextMenuItem delete = new ContextMenuItem(Component.translatable("selectServer.delete"), ThemeProperties.DELETE_ICON.get(), () -> ClientQuestFile.INSTANCE.deleteObject(object.id));

		if (!isShiftKeyDown()) {
			delete.setYesNo(Component.translatable("delete_item", object.getTitle()));
		}

		contextMenu.add(delete);

		contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.reset_progress"), ThemeProperties.RELOAD_ICON.get(), () -> ChangeProgressMessage.send(file.self, object, progressChange -> {
			progressChange.reset = true;
		})).setYesNo(Component.translatable("ftbquests.gui.reset_progress_q")));

		contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.complete_instantly"), ThemeProperties.CHECK_ICON.get(), () -> ChangeProgressMessage.send(file.self, object, progressChange -> {
			progressChange.reset = false;
		})).setYesNo(Component.translatable("ftbquests.gui.complete_instantly_q")));

		contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.copy_id"), ThemeProperties.WIKI_ICON.get(), () -> setClipboardString(object.getCodeString())) {
			@Override
			public void addMouseOverText(TooltipList list) {
				list.add(Component.literal(QuestObjectBase.getCodeString(object)));
			}
		});
	}

	public static void displayError(Component error) {
		Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, Component.translatable("ftbquests.gui.error"), error));
	}

	private boolean moveSelectedQuests(double x, double y) {
		for (Movable movable : selectedObjects) {
			if (movable.getChapter() == selectedChapter) {
				movable.move(selectedChapter, movable.getX() + x, movable.getY() + y);
			}
		}

		return true;
	}

	@Override
	public boolean keyPressed(Key key) {
		if (key.esc()) {
			// checking for open context menu first is important
			if (contextMenu != null) {
				openContextMenu((Panel) null);
				return true;
			} else if (isViewingQuest()) {
				closeQuest();
				return true;
			}
		}

		if (super.keyPressed(key)) {
			return true;
		} else if (FTBQuestsClient.KEY_QUESTS.matches(key.keyCode, key.scanCode)) {
			closeGui(true);
			return true;
		}

		if (key.is(GLFW.GLFW_KEY_TAB)) {
			if (selectedChapter != null && file.getVisibleChapters(file.self).size() > 1) {
				List<Chapter> visibleChapters = file.getVisibleChapters(file.self);

				if (!visibleChapters.isEmpty()) {
					selectChapter(visibleChapters.get(MathUtils.mod(visibleChapters.indexOf(selectedChapter) + (isShiftKeyDown() ? -1 : 1), visibleChapters.size())));
				}
			}

			return true;
		}

		if (key.is(GLFW.GLFW_KEY_SPACE)) {
			questPanel.resetScroll();
			return true;
		}

		if (key.is(GLFW.GLFW_KEY_R) && key.modifiers.onlyControl()) {
			grid = !grid;
			return true;
		}

		if (key.keyCode >= GLFW.GLFW_KEY_1 && key.keyCode <= GLFW.GLFW_KEY_9) {
			int i = key.keyCode - GLFW.GLFW_KEY_1;

			if (i < file.getVisibleChapters(file.self).size()) {
				selectChapter(file.getVisibleChapters(file.self).get(i));
			}

			return true;
		}

		if (key.modifiers.control() && selectedChapter != null && file.canEdit()) {
			double step;

			if (key.modifiers.shift()) {
				step = 0.1D;
			} else {
				step = 0.5D;
			}

			switch (key.keyCode) {
				case GLFW.GLFW_KEY_A:
					selectedObjects.addAll(selectedChapter.quests);
					return true;
				case GLFW.GLFW_KEY_D:
					selectedObjects.clear();
					return true;
				case GLFW.GLFW_KEY_DOWN:
					return moveSelectedQuests(0D, step);
				case GLFW.GLFW_KEY_UP:
					return moveSelectedQuests(0D, -step);
				case GLFW.GLFW_KEY_LEFT:
					return moveSelectedQuests(-step, 0D);
				case GLFW.GLFW_KEY_RIGHT:
					return moveSelectedQuests(step, 0D);
			}
		}

		if (key.keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || key.keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
			long now = System.currentTimeMillis();

			if (lastShiftPress == 0L) {
				lastShiftPress = now;
			} else {
				if (now - lastShiftPress <= 400L) {
					ConfigQuestObject<QuestObject> c = new ConfigQuestObject<>(QuestObjectType.CHAPTER.or(QuestObjectType.QUEST));
					SelectQuestObjectScreen<?> gui = new SelectQuestObjectScreen<>(c, accepted -> {
						if (accepted) {
							if (c.value instanceof Chapter) {
								selectChapter((Chapter) c.value);
							} else if (c.value instanceof Quest) {
								zoom = 20;
								selectChapter(((Quest) c.value).chapter);
								viewQuestPanel.hidePanel = false;
								viewQuest((Quest) c.value);
							}
						}

						QuestScreen.this.openGui();
					});

					gui.focus();
					gui.setTitle(Component.translatable("gui.search_box"));
					gui.openGui();
				}

				lastShiftPress = 0L;
			}
		}

		return false;
	}

	@Override
	public void tick() {
		if (selectedChapter != null && selectedChapter.invalid) {
			selectChapter(null);
		}

		if (selectedChapter == null) {
			selectChapter(file.getFirstVisibleChapter(file.self));
		}

		super.tick();
	}

	public int getZoom() {
		return zoom;
	}

	public double getQuestButtonSize() {
		return getZoom() * 3D / 2D;
	}

	public double getQuestButtonSpacing() {
		return getZoom() * ThemeProperties.QUEST_SPACING.get(selectedChapter) / 4D;
	}

	public void addZoom(double up) {
		int z = zoom;
		zoom = (int) Mth.clamp(zoom + up * 4, 4, 28);

		if (zoom != z) {
			grabbed = null;
			double sx = questPanel.centerQuestX;
			double sy = questPanel.centerQuestY;
			questPanel.resetScroll();
			questPanel.scrollTo(sx, sy);
		}
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		QuestTheme.currentObject = selectedChapter;
		super.drawBackground(matrixStack, theme, x, y, w, h);

		int pw = 20;

		Color4I borderColor = ThemeProperties.WIDGET_BORDER.get(selectedChapter);
		Color4I backgroundColor = ThemeProperties.WIDGET_BACKGROUND.get(selectedChapter);

		borderColor.draw(matrixStack, x + pw - 1, y + 1, 1, h - 2);
		backgroundColor.draw(matrixStack, x + 1, y + 1, pw - 2, h - 2);

		borderColor.draw(matrixStack, x + w - pw, y + 1, 1, h - 2);
		backgroundColor.draw(matrixStack, x + w - pw + 1, y + 1, pw - 2, h - 2);

		if (grabbed != null) {
			int mx = getMouseX();
			int my = getMouseY();
			if (grabbed.isLeft()) {

				if (scrollWidth > questPanel.width) {
					questPanel.setScrollX(Math.max(Math.min(questPanel.getScrollX() + (prevMouseX - mx), scrollWidth - questPanel.width), 0));
				} else {
					questPanel.setScrollX((scrollWidth - questPanel.width) / 2);
				}

				if (scrollHeight > questPanel.height) {
					questPanel.setScrollY(Math.max(Math.min(questPanel.getScrollY() + (prevMouseY - my), scrollHeight - questPanel.height), 0));
				} else {
					questPanel.setScrollY((scrollHeight - questPanel.height) / 2);
				}

				prevMouseX = mx;
				prevMouseY = my;
			} else if (grabbed.isMiddle()) {
				int boxX = Math.min(prevMouseX, mx);
				int boxY = Math.min(prevMouseY, my);
				int boxW = Math.abs(mx - prevMouseX);
				int boxH = Math.abs(my - prevMouseY);
				GuiHelper.drawHollowRect(matrixStack, boxX, boxY, boxW, boxH, Color4I.DARK_GRAY, false);
				Color4I.DARK_GRAY.withAlpha(40).draw(matrixStack, boxX, boxY, boxW, boxH);
			}
		}
	}

	void selectAllQuestsInBox(int mouseX, int mouseY, double scrollX, double scrollY) {
		int x1 = Math.min(prevMouseX, mouseX);
		int x2 = Math.max(prevMouseX, mouseX);
		int y1 = Math.min(prevMouseY, mouseY);
		int y2 = Math.max(prevMouseY, mouseY);
		Rect2i rect = new Rect2i(x1, y1, x2 - x1, y2 - y1);

		if (!Screen.hasControlDown()) selectedObjects.clear();

		questPanel.widgets.forEach(w -> {
			if (w instanceof QuestButton qb && rect.contains((int) (w.getX() - scrollX), (int) (w.getY() - scrollY))) {
				toggleSelected(qb.quest);
			}
		});
	}

	@Override
	public void drawForeground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		Color4I borderColor = ThemeProperties.WIDGET_BORDER.get(selectedChapter);
		GuiHelper.drawHollowRect(matrixStack, x, y, w, h, borderColor, false);
		super.drawForeground(matrixStack, theme, x, y, w, h);
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	@Override
	public boolean drawDefaultBackground(PoseStack matrixStack) {
		return false;
	}

	public void open(@Nullable QuestObject object, boolean focus) {
		if (object instanceof Chapter) {
			selectChapter((Chapter) object);
		} else if (object instanceof Quest) {
			viewQuestPanel.hidePanel = false;
			Quest q = (Quest) object;
			selectChapter(q.chapter);
			viewQuest(q);

			if (focus) {
				questPanel.scrollTo(q.x + 0.5D, q.y + 0.5D);
			}
		} else if (object instanceof Task) {
			viewQuestPanel.hidePanel = false;
			selectChapter(((Task) object).quest.chapter);
			viewQuest(((Task) object).quest);
		}

		openGui();
	}

	@Override
	public boolean handleClick(String scheme, String path) {
		if (scheme.isEmpty() && path.startsWith("#")) {
			open(file.get(file.getID(path)), true);
			return true;
		}

		return super.handleClick(scheme, path);
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		list.zOffset = 950;
		list.zOffsetItemTooltip = 500;
		super.addMouseOverText(list);
		//float hue = (float) ((System.currentTimeMillis() * 0.0001D) % 1D);
		//int rgb = Color4I.hsb(hue, 0.8F, 1F).rgba();
		//list.borderColorStart = rgb;
		//list.borderColorEnd = rgb;
	}

	public void addInfoTooltip(TooltipList list, QuestObjectBase object) {
		if (isKeyDown(GLFW.GLFW_KEY_F1) || isShiftKeyDown() && isCtrlKeyDown()) {
			list.add(Component.literal(object.getCodeString()).withStyle(ChatFormatting.DARK_GRAY));

			if (object instanceof QuestObject) {
				Date s = file.self.getStartedTime(object.id);

				if (s != null) {
					list.add(Component.literal("Started: ").append(s.toLocaleString()).withStyle(ChatFormatting.DARK_GRAY));
				}

				Date c = file.self.getCompletedTime(object.id);

				if (c != null) {
					list.add(Component.literal("Completed: ").append(c.toLocaleString()).withStyle(ChatFormatting.DARK_GRAY));
				}
			} else if (object instanceof Reward) {
				Date c = file.self.getRewardClaimTime(Minecraft.getInstance().player.getUUID(), (Reward) object);

				if (c != null) {
					list.add(Component.literal("Claimed: ").append(c.toLocaleString()).withStyle(ChatFormatting.DARK_GRAY));
				}
			}
		}
	}
}
