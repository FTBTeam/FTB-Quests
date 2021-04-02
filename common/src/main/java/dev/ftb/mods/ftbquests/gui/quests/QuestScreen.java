package dev.ftb.mods.ftbquests.gui.quests;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigValue;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigWithVariants;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.MathUtils;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.gui.CustomToast;
import dev.ftb.mods.ftbquests.gui.FTBQuestsTheme;
import dev.ftb.mods.ftbquests.gui.SelectQuestObjectScreen;
import dev.ftb.mods.ftbquests.net.MessageChangeProgress;
import dev.ftb.mods.ftbquests.net.MessageEditObject;
import dev.ftb.mods.ftbquests.quest.ChangeProgress;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.reward.RandomReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuestScreen extends GuiBase {
	public final ClientQuestFile file;
	public double scrollWidth, scrollHeight;
	public int prevMouseX, prevMouseY, grabbed;
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

	@Override
	public void addWidgets() {
		QuestTheme.currentObject = selectedChapter;
		add(chapterPanel);
		add(questPanel);
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
		selectedObjects.clear();
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
		if (getViewedQuest() != null) {
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

	public static void addObjectMenuItems(List<ContextMenuItem> contextMenu, Runnable gui, QuestObjectBase object) {
		ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
		ConfigGroup g = object.createSubGroup(group);
		object.getConfig(g);

		if (!g.getValues().isEmpty()) {
			List<ContextMenuItem> list = new ArrayList<>();

			for (ConfigValue c : g.getValues()) {
				if (c instanceof ConfigWithVariants) {
					MutableComponent name = new TranslatableComponent(c.getNameKey());

					if (!c.getCanEdit()) {
						name = name.withStyle(ChatFormatting.GRAY);
					}

					list.add(new ContextMenuItem(name, GuiIcons.SETTINGS, null) {
						@Override
						public void addMouseOverText(TooltipList list) {
							list.add(c.getStringForGUI(c.value));
						}

						@Override
						public void onClicked(Panel panel, MouseButton button) {
							c.onClicked(button, accepted -> {
								if (accepted) {
									c.setter.accept(c.value);
									new MessageEditObject(object).sendToServer();
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

		contextMenu.add(new ContextMenuItem(new TranslatableComponent("selectServer.edit"), ThemeProperties.EDIT_ICON.get(), () -> object.onEditButtonClicked(gui)));

		if (object instanceof RandomReward && !QuestObjectBase.isNull(((RandomReward) object).getTable())) {
			contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.reward_table.edit"), ThemeProperties.EDIT_ICON.get(), () -> ((RandomReward) object).getTable().onEditButtonClicked(gui)));
		}

		ContextMenuItem delete = new ContextMenuItem(new TranslatableComponent("selectServer.delete"), ThemeProperties.DELETE_ICON.get(), () -> ClientQuestFile.INSTANCE.deleteObject(object.id));

		if (!isShiftKeyDown()) {
			delete.setYesNo(new TranslatableComponent("delete_item", object.getTitle()));
		}

		contextMenu.add(delete);

		contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.reset_progress"), ThemeProperties.RELOAD_ICON.get(), () -> new MessageChangeProgress(ClientQuestFile.INSTANCE.self.uuid, Minecraft.getInstance().player.getUUID(), object.id, ChangeProgress.RESET).sendToServer()).setYesNo(new TranslatableComponent("ftbquests.gui.reset_progress_q")));

		if (object instanceof QuestObject) {
			contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.complete_instantly"), ThemeProperties.CHECK_ICON.get(), () -> new MessageChangeProgress(ClientQuestFile.INSTANCE.self.uuid, Minecraft.getInstance().player.getUUID(), object.id, ChangeProgress.COMPLETE).sendToServer()).setYesNo(new TranslatableComponent("ftbquests.gui.complete_instantly_q")));
		}

		contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.copy_id"), ThemeProperties.WIKI_ICON.get(), () -> setClipboardString(object.getCodeString())) {
			@Override
			public void addMouseOverText(TooltipList list) {
				list.add(new TextComponent(QuestObjectBase.getCodeString(object)));
			}
		});
	}

	public static void displayError(Component error) {
		Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, new TranslatableComponent("ftbquests.gui.error"), error));
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
		if (super.keyPressed(key)) {
			return true;
		}

		if (key.is(GLFW.GLFW_KEY_B)) {
			Theme.renderDebugBoxes = !Theme.renderDebugBoxes;

			if (Theme.renderDebugBoxes) {
				Minecraft.getInstance().getToasts().addToast(new CustomToast(new TextComponent("Debug rendering enabled!"), GuiIcons.BUG, new TextComponent("Press B to disable")));
			}

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
					SelectQuestObjectScreen gui = new SelectQuestObjectScreen<>(c, accepted -> {
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
					gui.setTitle(new TranslatableComponent("gui.search_box"));
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
			grabbed = 0;
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

		if (grabbed != 0) {
			int mx = getMouseX();
			int my = getMouseY();

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
		}
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
		super.addMouseOverText(list);
		//float hue = (float) ((System.currentTimeMillis() * 0.0001D) % 1D);
		//int rgb = Color4I.hsb(hue, 0.8F, 1F).rgba();
		//list.borderColorStart = rgb;
		//list.borderColorEnd = rgb;
		list.zOffset = 950;
	}

	public void addInfoTooltip(TooltipList list, QuestObjectBase object) {
		if (isKeyDown(GLFW.GLFW_KEY_F1) || isShiftKeyDown() && isCtrlKeyDown()) {
			list.add(new TextComponent(object.getCodeString()).withStyle(ChatFormatting.DARK_GRAY));

			if (object instanceof QuestObject) {
				Date s = file.self.getStartedTime(object.id);

				if (s != null) {
					list.add(new TextComponent("Started: ").append(s.toLocaleString()).withStyle(ChatFormatting.DARK_GRAY));
				}

				Date c = file.self.getCompletedTime(object.id);

				if (c != null) {
					list.add(new TextComponent("Completed: ").append(c.toLocaleString()).withStyle(ChatFormatting.DARK_GRAY));
				}
			} else if (object instanceof Reward) {
				Date c = file.self.getRewardClaimTime(Minecraft.getInstance().player.getUUID(), (Reward) object);

				if (c != null) {
					list.add(new TextComponent("Claimed: ").append(c.toLocaleString()).withStyle(ChatFormatting.DARK_GRAY));
				}
			}
		}
	}
}