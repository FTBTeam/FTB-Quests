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
import dev.ftb.mods.ftblibrary.util.ClientUtils;
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

import java.text.DateFormat;
import java.util.*;

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
	public static boolean grid = false;
	private PersistedData pendingPersistedData;

	public QuestScreen(ClientQuestFile q, @Nullable PersistedData persistedData) {
		file = q;
		selectedObjects = new ArrayList<>();

		expandChaptersButton = new ExpandChaptersButton(this);
		chapterPanel = new ChapterPanel(this);
		selectedChapter = file.getFirstVisibleChapter(file.self);

		questPanel = new QuestPanel(this);
		otherButtonsBottomPanel = new OtherButtonsPanelBottom(this);
		otherButtonsTopPanel = new OtherButtonsPanelTop(this);
		viewQuestPanel = new ViewQuestPanel(this);

		// defer restoring data till the first tick; things like scroll pos etc. are dependent
		// on all the widgets being present
		this.pendingPersistedData = persistedData;

		selectChapter(null);
	}

	@Nullable
	public Quest getViewedQuest() {
		return viewQuestPanel.getViewedQuest();
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
		return setFullscreen();
	}

	@Override
	public void onClosed() {
		file.setPersistedScreenInfo(new PersistedData(this));
		super.onClosed();
	}

	public void selectChapter(@Nullable Chapter chapter) {
		if (selectedChapter != chapter) {
			closeQuest();
			selectedChapter = chapter;
			questPanel.refreshWidgets();
			questPanel.resetScroll();
		}
	}

	public void viewQuest(Quest quest) {
		viewQuestPanel.setViewedQuest(quest);
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
		if (viewQuestPanel.setViewedQuest(null)) {
			viewQuestPanel.hidePanel = false;
		}
	}

	public void toggleSelected(Movable movable) {
		viewQuestPanel.setViewedQuest(null);

		if (selectedObjects.contains(movable)) {
			selectedObjects.remove(movable);
		} else {
			selectedObjects.add(movable);
		}
	}

	public void addObjectMenuItems(List<ContextMenuItem> contextMenu, Runnable gui, QuestObjectBase object) {
		addObjectMenuItems(contextMenu, gui, object, object instanceof Movable m ? m : null);
	}

	/**
	 * Add any relevant context menu entries for the given quest object
	 *
	 * @param contextMenu the menu to add to
	 * @param gui the gui to return to from any screens that might be opened
	 * @param object the quest object to add menu operations for
	 * @param deletionFocus the object to be deleted by the delete operation (which could be different from the quest object...)
	 */
	public void addObjectMenuItems(List<ContextMenuItem> contextMenu, Runnable gui, QuestObjectBase object, Movable deletionFocus) {
		ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
		ConfigGroup subGroup = object.createSubGroup(group);
		object.getConfig(subGroup);

		contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.edit"),
				ThemeProperties.EDIT_ICON.get(),
				() -> object.onEditButtonClicked(gui))
		);

		if (object instanceof QuestLink link) {
			link.getQuest().ifPresent(quest -> {
				contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.edit_linked_quest"),
						ThemeProperties.EDIT_ICON.get(),
						() -> quest.onEditButtonClicked(gui))
				);
			});
		}

		if (!subGroup.getValues().isEmpty()) {
			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.copy_id.quick_properties"),
					Icons.SETTINGS,
					() -> openPropertiesSubMenu(object, subGroup))
			);
		}

		if (object instanceof RandomReward rr && !QuestObjectBase.isNull(rr.getTable())) {
			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.reward_table.edit"),
					ThemeProperties.EDIT_ICON.get(),
					() -> rr.getTable().onEditButtonClicked(gui))
			);
		}

		long delId = deletionFocus == null ? object.id : deletionFocus.getMovableID();
		QuestObjectBase delObject = ClientQuestFile.INSTANCE.getBase(delId);
		if (delObject != null) {
			ContextMenuItem delete = new ContextMenuItem(Component.translatable("selectServer.delete"),
					ThemeProperties.DELETE_ICON.get(),
					() -> ClientQuestFile.INSTANCE.deleteObject(delId));
			if (!isShiftKeyDown()) {
				delete.setYesNo(Component.translatable("delete_item", delObject.getTitle()));
			}
			contextMenu.add(delete);
		}

		contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.reset_progress"),
				ThemeProperties.RELOAD_ICON.get(),
				() -> ChangeProgressMessage.send(file.self, object, progressChange -> progressChange.reset = true)
		).setYesNo(Component.translatable("ftbquests.gui.reset_progress_q")));

		contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.complete_instantly"),
				ThemeProperties.CHECK_ICON.get(), () -> ChangeProgressMessage.send(file.self, object,
				progressChange -> progressChange.reset = false)
		).setYesNo(Component.translatable("ftbquests.gui.complete_instantly_q")));

		Component[] tooltip = object instanceof Quest ?
				new Component[] {
						Component.literal(QuestObjectBase.getCodeString(object)),
						Component.translatable("ftbquests.gui.copy_id.paste_hint").withStyle(ChatFormatting.GRAY)
				} :
				new Component[] {
						Component.literal(QuestObjectBase.getCodeString(object))
				};
		contextMenu.add(new TooltipContextMenuItem(Component.translatable("ftbquests.gui.copy_id"),
				ThemeProperties.WIKI_ICON.get(),
				() -> setClipboardString(object.getCodeString()),
				tooltip)
		);
	}

	private void openPropertiesSubMenu(QuestObjectBase object, ConfigGroup g) {
		List<ContextMenuItem> subMenu = new ArrayList<>();

		subMenu.add(new ContextMenuItem(object.getTitle(), Color4I.EMPTY, null).setCloseMenu(false));
		subMenu.add(ContextMenuItem.SEPARATOR);
		for (ConfigValue c : g.getValues()) {
			if (c instanceof ConfigWithVariants) {
				MutableComponent name = Component.translatable(c.getNameKey());

				if (!c.getCanEdit()) {
					name = name.withStyle(ChatFormatting.GRAY);
				}

				subMenu.add(new ContextMenuItem(name, Icons.SETTINGS, null) {
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

		getGui().openContextMenu(subMenu);
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

		if (key.is(GLFW.GLFW_KEY_F) && key.modifiers.onlyControl()) {
			openQuestSelectionGUI();
			return true;
		}

		if (key.is(GLFW.GLFW_KEY_0)) {
			addZoom((16 - zoom) / 4.0);
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
			double step = key.modifiers.shift() ? 0.1D : 0.5D;

			switch (key.keyCode) {
				case GLFW.GLFW_KEY_A -> {
					selectedObjects.addAll(selectedChapter.quests);
					selectedObjects.addAll(selectedChapter.questLinks);
					return true;
				}
				case GLFW.GLFW_KEY_D -> {
					selectedObjects.clear();
					return true;
				}
				case GLFW.GLFW_KEY_DOWN -> {
					return moveSelectedQuests(0D, step);
				}
				case GLFW.GLFW_KEY_UP -> {
					return moveSelectedQuests(0D, -step);
				}
				case GLFW.GLFW_KEY_LEFT -> {
					return moveSelectedQuests(-step, 0D);
				}
				case GLFW.GLFW_KEY_RIGHT -> {
					return moveSelectedQuests(step, 0D);
				}
			}
		}

		return false;
	}

	private void openQuestSelectionGUI() {
		ConfigQuestObject<QuestObject> c = new ConfigQuestObject<>(QuestObjectType.CHAPTER.or(QuestObjectType.QUEST).or(QuestObjectType.QUEST_LINK));
		SelectQuestObjectScreen<?> gui = new SelectQuestObjectScreen<>(c, accepted -> {
			if (accepted) {
				if (c.value instanceof Chapter chapter) {
					selectChapter(chapter);
				} else if (c.value instanceof Quest quest) {
					zoom = 20;
					selectChapter(quest.chapter);
					viewQuestPanel.hidePanel = false;
					questPanel.scrollTo(quest.x, quest.y);
					viewQuest(quest);
				} else if (c.value instanceof QuestLink link) {
					zoom = 20;
					selectChapter(link.getChapter());
					viewQuestPanel.hidePanel = false;
					questPanel.scrollTo(link.getX(), link.getY());
					link.getQuest().ifPresent(this::viewQuest);
				}
			}

			QuestScreen.this.openGui();
		});

		gui.focus();
		gui.setTitle(Component.translatable("gui.search_box"));
		gui.openGui();
	}

	@Override
	public void tick() {
		if (pendingPersistedData != null) {
			restorePersistedScreenData(file, pendingPersistedData);
			pendingPersistedData = null;
		}

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
				toggleSelected(qb.moveAndDeleteFocus());
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
		if (object instanceof Chapter chapter) {
			selectChapter(chapter);
		} else if (object instanceof Quest quest) {
			selectChapter(quest.chapter);
			viewQuestPanel.hidePanel = false;
			viewQuest(quest);
			if (focus) {
				questPanel.scrollTo(quest.x + 0.5D, quest.y + 0.5D);
			}
		} else if (object instanceof QuestLink link) {
			link.getQuest().ifPresent(quest -> {
				viewQuestPanel.hidePanel = false;
				viewQuest(quest);
				if (focus) {
					questPanel.scrollTo(quest.x + 0.5D, quest.y + 0.5D);
				}
			});
		} else if (object instanceof Task task) {
			selectChapter(task.quest.chapter);
			viewQuestPanel.hidePanel = false;
			viewQuest(task.quest);
		}

		// in case we've just opened the gui; we don't want switch away from the view object on the next tick
		pendingPersistedData = null;

		if (ClientUtils.getCurrentGuiAs(QuestScreen.class) != this) {
			openGui();
		}
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
				file.self.getStartedTime(object.id)
						.ifPresent(date -> list.add(formatDate("Started", date)));
				file.self.getCompletedTime(object.id)
						.ifPresent(date -> list.add(formatDate("Completed", date)));
			} else if (object instanceof Reward r) {
				file.self.getRewardClaimTime(FTBQuests.PROXY.getClientPlayer().getUUID(), r)
						.ifPresent(date -> list.add(formatDate("Claimed", date)));
			}
		}
	}

	private static Component formatDate(String prefix, Date date) {
		return Component.literal(prefix + ": ")
				.append(Component.literal(DateFormat.getDateTimeInstance().format(date))
						.withStyle(ChatFormatting.DARK_GRAY)
				);
	}

	public Collection<Quest> getSelectedQuests() {
		Map<Long,Quest> questMap = new HashMap<>();
		selectedObjects.forEach(movable -> {
			if (movable instanceof Quest q) {
				questMap.put(q.id, q);
			} else if (movable instanceof QuestLink ql) {
				ql.getQuest().ifPresent(q -> questMap.put(q.id, q));
			}
		});
		return List.copyOf(questMap.values());
	}

	public PersistedData getPersistedScreenData() {
		return new PersistedData(this);
	}

	private void restorePersistedScreenData(QuestFile file, PersistedData persistedData) {
		zoom = persistedData.zoom;
		selectChapter(file.getChapter(persistedData.selectedChapter));

		selectedObjects.clear();
		persistedData.selectedQuests.stream()
				.mapToLong(id -> id)
				.filter(id -> file.get(id) instanceof Movable)
				.mapToObj(id -> (Movable) file.get(id))
				.forEach(selectedObjects::add);

		questPanel.scrollTo(persistedData.scrollX, persistedData.scrollY);
		questPanel.centerQuestX = persistedData.scrollX;
		questPanel.centerQuestY = persistedData.scrollY;
		chapterPanel.setExpanded(persistedData.chaptersExpanded);
	}

	/**
	 * Allows certain attributes of the GUI to be remembered between invocations
	 */
	public static class PersistedData {
		private final int zoom;
		private final double scrollX, scrollY;
		private final long selectedChapter;
		private final List<Long> selectedQuests;
		private final boolean chaptersExpanded;

		private PersistedData(QuestScreen questScreen) {
			zoom = questScreen.zoom;
			scrollX = questScreen.questPanel.centerQuestX;
			scrollY = questScreen.questPanel.centerQuestY;
			selectedChapter = questScreen.selectedChapter == null ? 0L : questScreen.selectedChapter.id;
			selectedQuests = questScreen.selectedObjects.stream().map(Movable::getMovableID).toList();
			chaptersExpanded = questScreen.chapterPanel.expanded;
		}
	}
}
