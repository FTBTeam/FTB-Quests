package dev.ftb.mods.ftbquests.client.gui.quests;

import com.mojang.datafixers.util.Pair;
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
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.CustomToast;
import dev.ftb.mods.ftbquests.client.gui.FTBQuestsTheme;
import dev.ftb.mods.ftbquests.client.gui.SelectQuestObjectScreen;
import dev.ftb.mods.ftbquests.net.*;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.reward.RandomReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.text.DateFormat;
import java.util.*;

public class QuestScreen extends BaseScreen {
	final ClientQuestFile file;

	double scrollWidth, scrollHeight;
	int prevMouseX, prevMouseY;
	MouseButton grabbed = null;
	Chapter selectedChapter;
	final List<Movable> selectedObjects;
	final ExpandChaptersButton expandChaptersButton;
	final ChapterPanel chapterPanel;
	boolean movingObjects = false;
	int zoom = 16;
	static boolean grid = false;
	private PersistedData pendingPersistedData;

	public final QuestPanel questPanel;
	public final OtherButtonsPanelBottom otherButtonsBottomPanel;
	public final OtherButtonsPanelTop otherButtonsTopPanel;
	public final ViewQuestPanel viewQuestPanel;

	public QuestScreen(ClientQuestFile clientQuestFile, @Nullable PersistedData persistedData) {
		file = clientQuestFile;
		selectedObjects = new ArrayList<>();

		expandChaptersButton = new ExpandChaptersButton(this);
		chapterPanel = new ChapterPanel(this);
		selectedChapter = file.getFirstVisibleChapter(file.selfTeamData);

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

	public void refreshChapterPanel() {
		chapterPanel.refreshWidgets();
	}

	public void refreshQuestPanel() {
		questPanel.refreshWidgets();
	}

	public void refreshViewQuestPanel() {
		viewQuestPanel.refreshWidgets();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return ClientQuestFile.INSTANCE.isPauseGame();
	}

	@Override
	public void addWidgets() {
		QuestTheme.currentObject = selectedChapter;
		add(questPanel);
		add(chapterPanel);
		add(expandChaptersButton);
		add(otherButtonsBottomPanel);
		add(otherButtonsTopPanel);
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
		file.setPersistedScreenInfo(getPersistedScreenData());
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

	public void scrollTo(Movable movable) {
		questPanel.scrollTo(movable.getX(), movable.getY());
	}

	public void viewQuest(Quest quest) {
		Quest current = viewQuestPanel.getViewedQuest();
		if (current != quest) {
			viewQuestPanel.setViewedQuest(quest);
			if (current == null) {
				pushModalPanel(viewQuestPanel);
			} else if (quest == null) {
				closeModalPanel(viewQuestPanel);
			}
			viewQuestPanel.updateMouseOver(getMouseX(), getMouseY());
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
		viewQuest(null);
	}

	public void toggleSelected(Movable movable) {
		viewQuest(null);

		if (selectedObjects.contains(movable)) {
			selectedObjects.remove(movable);
		} else {
			selectedObjects.add(movable);
		}
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
		ConfigGroup group = new ConfigGroup(FTBQuestsAPI.MOD_ID);
		ConfigGroup subGroup = object.createSubGroup(group);
		object.fillConfigGroup(subGroup);

		contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.edit"),
				ThemeProperties.EDIT_ICON.get(),
				b -> object.onEditButtonClicked(gui))
		);

		if (object instanceof QuestLink link) {
			link.getQuest().ifPresent(quest -> contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.edit_linked_quest"),
					ThemeProperties.EDIT_ICON.get(),
					b -> quest.onEditButtonClicked(gui))
			));
		}

		if (!subGroup.getValues().isEmpty()) {
			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.copy_id.quick_properties"),
					Icons.SETTINGS,
					b -> openPropertiesSubMenu(object, subGroup))
			);
		}

		if (object instanceof RandomReward rr && !QuestObjectBase.isNull(rr.getTable())) {
			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.reward_table.edit"),
					ThemeProperties.EDIT_ICON.get(),
					b -> rr.getTable().onEditButtonClicked(gui))
			);
		}

		long delId = deletionFocus == null ? object.id : deletionFocus.getMovableID();
		QuestObjectBase delObject = ClientQuestFile.INSTANCE.getBase(delId);
		if (delObject != null) {
			ContextMenuItem delete = new ContextMenuItem(Component.translatable("selectServer.delete"),
					ThemeProperties.DELETE_ICON.get(),
					b -> ClientQuestFile.INSTANCE.deleteObject(delId));
			if (!isShiftKeyDown()) {
				delete.setYesNoText(Component.translatable("delete_item", delObject.getTitle()));
			}
			contextMenu.add(delete);
		}

		contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.reset_progress"),
				ThemeProperties.RELOAD_ICON.get(),
				b -> ChangeProgressMessage.sendToServer(file.selfTeamData, object, progressChange -> progressChange.setReset(true))
		).setYesNoText(Component.translatable("ftbquests.gui.reset_progress_q")));

		contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.gui.complete_instantly"),
				ThemeProperties.CHECK_ICON.get(),
				b -> ChangeProgressMessage.sendToServer(file.selfTeamData, object, progressChange -> progressChange.setReset(false))
		).setYesNoText(Component.translatable("ftbquests.gui.complete_instantly_q")));

		Component[] tooltip = object instanceof Quest ?
				new Component[] {
						Component.literal(QuestObjectBase.getCodeString(object)),
						Component.translatable("ftbquests.gui.copy_id.paste_hint").withStyle(ChatFormatting.GRAY)
				} :
				new Component[] {
						Component.literal(QuestObjectBase.getCodeString(object))
				};
		if (selectedChapter != null) {
			if (selectedChapter.isAutofocus(object.id)) {
				contextMenu.add(new ContextMenuItem(Component.translatable("ftbquest.gui.clear_autofocused"),
						Icons.MARKER,
						b -> setAutofocusedId(0L)));
			} else if (object instanceof Quest || object instanceof QuestLink) {
				contextMenu.add(new ContextMenuItem(Component.translatable("ftbquest.gui.set_autofocused"),
						Icons.MARKER,
						b -> setAutofocusedId(object.id)));
			}
		}
		contextMenu.add(new TooltipContextMenuItem(Component.translatable("ftbquests.gui.copy_id"),
				ThemeProperties.WIKI_ICON.get(),
				b -> setClipboardString(object.getCodeString()),
				tooltip)
		);
	}

	private void setAutofocusedId(long id) {
		selectedChapter.setAutofocus(id);
		new EditObjectMessage(selectedChapter).sendToServer();
	}

	private List<ContextMenuItem> scanForConfigEntries(List<ContextMenuItem> res, QuestObjectBase object, ConfigGroup g) {
		for (ConfigValue<?> value : g.getValues()) {
			if (value instanceof ConfigWithVariants) {
				MutableComponent name = Component.translatable(value.getNameKey());
				if (!value.getCanEdit()) {
					name = name.withStyle(ChatFormatting.GRAY);
				}

				res.add(new ContextMenuItem(name, Icons.SETTINGS, null) {
					@Override
					public void addMouseOverText(TooltipList list) {
						list.add(value.getStringForGUI());
					}

					@Override
					public void onClicked(Button button, Panel panel, MouseButton mouseButton) {
						value.onClicked(button, mouseButton, accepted -> {
							if (accepted) {
								value.applyValue();
								new EditObjectMessage(object).sendToServer();
							}
						});
					}

					@Override
					public void drawIcon(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
						value.getIcon().draw(graphics, x, y, w, h);
					}
				});
			}
		}
		for (ConfigGroup sub : g.getSubgroups()) {
			scanForConfigEntries(res, object, sub);
		}
		return res;
	}

	private void openPropertiesSubMenu(QuestObjectBase object, ConfigGroup g) {
		List<ContextMenuItem> subMenu = new ArrayList<>();

		subMenu.add(new ContextMenuItem(object.getTitle(), Color4I.empty(), null).setCloseMenu(false));
		subMenu.add(ContextMenuItem.SEPARATOR);
		subMenu.addAll(scanForConfigEntries(new ArrayList<>(), object, g));

		getGui().openContextMenu(subMenu);
	}

	public static void displayError(Component error) {
//		Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, Component.translatable("ftbquests.gui.error"), error));
		Minecraft.getInstance().getToasts().addToast(new CustomToast(Component.translatable("ftbquests.gui.error"), Icons.BARRIER, error));
	}

	private boolean moveSelectedQuests(double x, double y) {
		for (Movable movable : selectedObjects) {
			if (movable.getChapter() == selectedChapter) {
				movable.move(selectedChapter, movable.getX() + x, movable.getY() + y);
			}
		}

		return true;
	}

	private boolean copyObjectsToClipboard() {
		Movable toCopy = null;

		if (selectedObjects.size() > 1) {
			displayError(Component.translatable("ftbquests.quest.cannot_copy_many"));
		} else if (selectedObjects.isEmpty()) {
			// no objects selected: copy hovered object if there is one
			toCopy = questPanel.getWidgets().stream()
					.filter(w -> w instanceof QuestPositionableButton && w.isMouseOver())
					.map(w -> ((QuestPositionableButton) w).moveAndDeleteFocus())
					.findFirst()
					.orElse(null);
		} else {
			// one object selected
			toCopy = selectedObjects.get(0);
		}

		if (toCopy != null) {
			toCopy.copyToClipboard();
			Minecraft.getInstance().getToasts().addToast(new CustomToast(Component.translatable("ftbquests.quest.copied"),
					Icons.INFO, Component.literal(toCopy.getTitle().getString())));
			return true;
		}

		return false;
    }

	private boolean pasteSelectedQuest(boolean withDeps) {
		if (ChapterImage.isImageInClipboard()) {
			return pasteSelectedImage();
		} else {
			return QuestObjectBase.parseHexId(getClipboardString()).map(id -> {
				Quest quest = file.getQuest(id);
				if (quest == null) return false;
				Pair<Double, Double> qxy = getSnappedXY();
				new CopyQuestMessage(quest, selectedChapter, qxy.getFirst(), qxy.getSecond(), withDeps).sendToServer();
				return true;
			}).orElse(false);
		}
	}

	private boolean pasteSelectedImage() {
		return ChapterImageButton.getClipboardImage().map(clipImg -> {
			Pair<Double,Double> qxy = getSnappedXY();
			new CopyChapterImageMessage(clipImg, selectedChapter, qxy.getFirst(), qxy.getSecond()).sendToServer();
			return true;
		}).orElse(false);
	}

	private boolean pasteSelectedQuestLinks() {
		String clip = getClipboardString();
		if (clip.isEmpty()) return false;

		return QuestObjectBase.parseHexId(clip).map(id -> {
			if (file.getQuest(id) == null) return false;
			Pair<Double,Double> qxy = getSnappedXY();
			QuestLink link = new QuestLink(0L, selectedChapter, id);
			link.setPosition(qxy.getFirst(), qxy.getSecond());
			new CreateObjectMessage(link, new CompoundTag(), false).sendToServer();
			return true;
		}).orElse(false);
	}

	private Pair<Double,Double> getSnappedXY() {
		double snap = 1D / file.getGridScale();
		double qx = Mth.floor(questPanel.questX * snap + 0.5D) / snap;
		double qy = Mth.floor(questPanel.questY * snap + 0.5D) / snap;
		return Pair.of(qx,qy);
	}

	void deleteSelectedObjects() {
		selectedObjects.forEach(movable -> {
			if (movable instanceof Quest q) {
				file.deleteObject(q.id);
			} else if (movable instanceof QuestLink ql) {
				file.deleteObject(ql.id);
			} else if (movable instanceof ChapterImage img) {
				img.getChapter().removeImage(img);
				new EditObjectMessage(img.getChapter()).sendToServer();
			}
		});
		selectedObjects.clear();
	}

	@Override
	public boolean keyPressed(Key key) {
		if (super.keyPressed(key)) {
			return true;
		} else if (FTBQuestsClient.KEY_QUESTS.matches(key.keyCode, key.scanCode)) {
			closeGui(true);
			return true;
		}

		List<Chapter> visibleChapters = file.getVisibleChapters(file.selfTeamData);

		if (key.is(GLFW.GLFW_KEY_TAB)) {
			if (selectedChapter != null && file.getVisibleChapters(file.selfTeamData).size() > 1) {

				if (!visibleChapters.isEmpty()) {
					selectChapter(visibleChapters.get(MathUtils.mod(visibleChapters.indexOf(selectedChapter) + (isShiftKeyDown() ? -1 : 1), visibleChapters.size())));
					selectedChapter.getAutofocus().ifPresent(this::scrollTo);
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

			if (i < visibleChapters.size()) {
				selectChapter(visibleChapters.get(i));
				selectedChapter.getAutofocus().ifPresent(this::scrollTo);
			}

			return true;
		}

		if (!file.canEdit()) {
			return false;
		}

		// all edit-mode keybinds handled below here

		if (key.is(GLFW.GLFW_KEY_DELETE) && !selectedObjects.isEmpty()) {
			if (!isShiftKeyDown()) {
				Component title = Component.translatable("delete_item", Component.translatable("ftbquests.objects", selectedObjects.size()));
				getGui().openYesNo(title, Component.empty(), this::deleteSelectedObjects);
			} else {
				deleteSelectedObjects();
			}
		} else if (key.modifiers.control()) {
			double step = key.modifiers.shift() ? 0.1D : 0.5D;

			switch (key.keyCode) {
				case GLFW.GLFW_KEY_A -> {
					if (selectedChapter != null) {
						selectedObjects.addAll(selectedChapter.getQuests());
						selectedObjects.addAll(selectedChapter.getQuestLinks());
						selectedObjects.addAll(selectedChapter.getImages());
					}
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
				case GLFW.GLFW_KEY_C -> {
					return copyObjectsToClipboard();
				}
				case GLFW.GLFW_KEY_V -> {
					if (key.modifiers.alt()) {
						return pasteSelectedQuestLinks();
					} else {
						return pasteSelectedQuest(!key.modifiers.shift());
					}
				}
			}
		}

		return false;
	}

	private void openQuestSelectionGUI() {
		ConfigQuestObject<QuestObject> c = new ConfigQuestObject<>(QuestObjectType.CHAPTER.or(QuestObjectType.QUEST).or(QuestObjectType.QUEST_LINK));
		new SelectQuestObjectScreen<>(c, accepted -> {
			if (accepted) {
				if (c.getValue() instanceof Chapter chapter) {
					selectChapter(chapter);
				} else if (c.getValue() instanceof Quest quest) {
					zoom = 20;
					selectChapter(quest.getChapter());
					questPanel.scrollTo(quest.getX(), quest.getY());
					viewQuest(quest);
				} else if (c.getValue() instanceof QuestLink link) {
					zoom = 20;
					selectChapter(link.getChapter());
					questPanel.scrollTo(link.getX(), link.getY());
					link.getQuest().ifPresent(this::viewQuest);
				}
			}
			QuestScreen.this.openGui();
		}).openGui();
	}

	@Override
	public void tick() {
		if (pendingPersistedData != null) {
			restorePersistedScreenData(file, pendingPersistedData);
			pendingPersistedData = null;
		}

		if (selectedChapter != null && !selectedChapter.isValid()) {
			selectChapter(null);
		}

		if (selectedChapter == null) {
			selectChapter(file.getFirstVisibleChapter(file.selfTeamData));
			if (selectedChapter != null) {
				selectedChapter.getAutofocus().ifPresent(this::scrollTo);
			}
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
			questPanel.withPreservedPos(QuestPanel::resetScroll);
		}
	}

	@Override
	public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		QuestTheme.currentObject = selectedChapter;
		super.drawBackground(graphics, theme, x, y, w, h);

		int pw = 20;

		Color4I borderColor = ThemeProperties.WIDGET_BORDER.get(selectedChapter);
		Color4I backgroundColor = ThemeProperties.WIDGET_BACKGROUND.get(selectedChapter);

		borderColor.draw(graphics, x + pw - 1, y + 1, 1, h - 2);
		backgroundColor.draw(graphics, x + 1, y + 1, pw - 2, h - 2);

		borderColor.draw(graphics, x + w - pw, y + 1, 1, h - 2);
		backgroundColor.draw(graphics, x + w - pw + 1, y + 1, pw - 2, h - 2);

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
				GuiHelper.drawHollowRect(graphics, boxX, boxY, boxW, boxH, Color4I.DARK_GRAY, false);
				Color4I.DARK_GRAY.withAlpha(40).draw(graphics, boxX, boxY, boxW, boxH);
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

		questPanel.getWidgets().forEach(w -> {
			if (w instanceof QuestPositionableButton qb && rect.contains((int) (w.getX() - scrollX), (int) (w.getY() - scrollY))) {
				toggleSelected(qb.moveAndDeleteFocus());
			}
		});
	}

	@Override
	public void drawForeground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		Color4I borderColor = ThemeProperties.WIDGET_BORDER.get(selectedChapter);
		GuiHelper.drawHollowRect(graphics, x, y, w, h, borderColor, false);
		super.drawForeground(graphics, theme, x, y, w, h);
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	@Override
	public boolean drawDefaultBackground(GuiGraphics graphics) {
		return false;
	}

	public void open(@Nullable QuestObject object, boolean focus) {
		if (object instanceof Chapter chapter) {
			selectChapter(chapter);
		} else if (object instanceof Quest quest) {
			selectChapter(quest.getChapter());
			viewQuest(quest);
			if (focus) {
				questPanel.scrollTo(quest.getX() + 0.5D, quest.getY() + 0.5D);
			}
		} else if (object instanceof QuestLink link) {
			link.getQuest().ifPresent(quest -> {
				selectChapter(link.getChapter());
				viewQuest(quest);
				if (focus) {
					questPanel.scrollTo(link.getX() + 0.5D, link.getY() + 0.5D);
				}
			});
		} else if (object instanceof Task task) {
			selectChapter(task.getQuest().getChapter());
			viewQuest(task.getQuest());
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
//		list.zOffset = 950;
//		list.zOffsetItemTooltip = 500;
		super.addMouseOverText(list);
	}

	public void addInfoTooltip(TooltipList list, QuestObjectBase object) {
		if (isKeyDown(GLFW.GLFW_KEY_F1) || isShiftKeyDown() && isCtrlKeyDown()) {
			list.add(Component.literal(object.getCodeString()).withStyle(ChatFormatting.DARK_GRAY));

			if (object instanceof QuestObject) {
				file.selfTeamData.getStartedTime(object.id)
						.ifPresent(date -> list.add(formatDate("Started", date)));
				file.selfTeamData.getCompletedTime(object.id)
						.ifPresent(date -> list.add(formatDate("Completed", date)));
			} else if (object instanceof Reward r) {
				file.selfTeamData.getRewardClaimTime(FTBQuestsClient.getClientPlayer().getUUID(), r)
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
		return pendingPersistedData != null ? pendingPersistedData : new PersistedData(this);
	}

	private void restorePersistedScreenData(BaseQuestFile file, PersistedData persistedData) {
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

	public void initiateMoving(Movable movable) {
		movingObjects = true;
		selectedObjects.clear();
		toggleSelected(movable);
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
			selectedQuests = questScreen.selectedObjects.stream().map(Movable::getMovableID).filter(id -> id != 0).toList();
			chaptersExpanded = questScreen.chapterPanel.expanded;
		}
	}
}
