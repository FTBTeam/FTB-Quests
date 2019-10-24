package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.config.IIteratingConfig;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.IOpenableGui;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.FTBQuestsTheme;
import com.feed_the_beast.ftbquests.gui.GuiSelectQuestObject;
import com.feed_the_beast.ftbquests.net.edit.MessageChangeProgress;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.feed_the_beast.ftbquests.net.edit.MessageMoveQuest;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.reward.RandomReward;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.theme.QuestTheme;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GuiQuestTree extends GuiBase
{
	public final ClientQuestFile file;
	public int scrollWidth, scrollHeight, prevMouseX, prevMouseY, grabbed;
	public Chapter selectedChapter;
	public final HashSet<Quest> selectedQuests;
	public final PanelChapters chapterPanel;
	public final PanelQuests questPanel;
	public final PanelOtherButtonsBottom otherButtonsBottomPanel;
	public final PanelOtherButtonsTop otherButtonsTopPanel;
	public final PanelChapterHover chapterHoverPanel;
	public final PanelViewQuest viewQuestPanel;
	public boolean movingQuest = false;
	public int zoom = 16;
	public long lastShiftPress = 0L;

	public GuiQuestTree(ClientQuestFile q)
	{
		file = q;
		selectedQuests = new HashSet<>();

		chapterPanel = new PanelChapters(this);
		selectedChapter = file.chapters.isEmpty() ? null : file.chapters.get(0);

		questPanel = new PanelQuests(this);
		otherButtonsBottomPanel = new PanelOtherButtonsBottom(this);
		otherButtonsTopPanel = new PanelOtherButtonsTop(this);
		chapterHoverPanel = new PanelChapterHover(this);
		viewQuestPanel = new PanelViewQuest(this);

		selectChapter(null);
	}

	@Nullable
	public Quest getViewedQuest()
	{
		return viewQuestPanel.quest;
	}

	@Override
	public void addWidgets()
	{
		QuestTheme.currentObject = selectedChapter;
		add(chapterPanel);
		add(questPanel);
		add(otherButtonsBottomPanel);
		add(otherButtonsTopPanel);
		add(chapterHoverPanel);
		add(viewQuestPanel);
	}

	@Override
	public void alignWidgets()
	{
		QuestTheme.currentObject = selectedChapter;
		otherButtonsBottomPanel.alignWidgets();
		otherButtonsTopPanel.alignWidgets();
		chapterPanel.alignWidgets();
	}

	@Override
	public boolean onInit()
	{
		return setFullscreen();
	}

	public void selectChapter(@Nullable Chapter chapter)
	{
		if (selectedChapter != chapter)
		{
			movingQuest = false;
			closeQuest();
			selectedChapter = chapter;
			questPanel.refreshWidgets();
			resetScroll();
		}
	}

	public void viewQuest(Quest quest)
	{
		selectedQuests.clear();

		if (viewQuestPanel.quest != quest)
		{
			viewQuestPanel.quest = quest;
			viewQuestPanel.refreshWidgets();
		}
	}

	@Override
	public boolean onClosedByKey(int key)
	{
		if (super.onClosedByKey(key))
		{
			if (getViewedQuest() != null)
			{
				closeQuest();
				return false;
			}

			return true;
		}

		return false;
	}

	@Override
	public void onBack()
	{
		if (getViewedQuest() != null)
		{
			closeQuest();
		}
		else
		{
			super.onBack();
		}
	}

	public void closeQuest()
	{
		selectedQuests.clear();

		if (viewQuestPanel.quest != null)
		{
			viewQuestPanel.quest = null;
			viewQuestPanel.hidePanel = false;
			viewQuestPanel.refreshWidgets();
		}
	}

	public void toggleSelected(Quest quest)
	{
		if (viewQuestPanel.quest != null)
		{
			viewQuestPanel.quest = null;
			viewQuestPanel.refreshWidgets();
		}

		if (!selectedQuests.add(quest))
		{
			selectedQuests.remove(quest);
		}
	}

	public void resetScroll()
	{
		questPanel.alignWidgets();
		questPanel.setScrollX((scrollWidth - questPanel.width) / 2);
		questPanel.setScrollY((scrollHeight - questPanel.height) / 2);
	}

	public static void addObjectMenuItems(List<ContextMenuItem> contextMenu, IOpenableGui gui, QuestObjectBase object)
	{
		ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
		ConfigGroup g = object.createSubGroup(group);
		object.getConfig(g);

		if (!g.getValues().isEmpty())
		{
			List<ContextMenuItem> list = new ArrayList<>();

			for (ConfigValueInstance inst : g.getValues())
			{
				if (inst.getValue() instanceof IIteratingConfig)
				{
					String name = inst.getDisplayName().getFormattedText();

					if (!inst.getCanEdit())
					{
						name = TextFormatting.GRAY + name;
					}

					list.add(new ContextMenuItem(name, inst.getIcon(), null)
					{
						@Override
						public void addMouseOverText(List<String> list)
						{
							list.add(inst.getValue().getStringForGUI().getFormattedText());
						}

						@Override
						public void onClicked(Panel panel, MouseButton button)
						{
							inst.getValue().onClicked(gui, inst, button, () -> new MessageEditObject(object).sendToServer());
						}

						@Override
						public void drawIcon(Theme theme, int x, int y, int w, int h)
						{
							if (inst.getValue() instanceof ConfigBoolean)
							{
								(inst.getValue().getBoolean() ? GuiIcons.ACCEPT : GuiIcons.ACCEPT_GRAY).draw(x, y, w, h);
							}
							else
							{
								super.drawIcon(theme, x, y, w, h);
							}
						}
					});
				}
			}

			if (!list.isEmpty())
			{
				list.sort(null);
				contextMenu.addAll(list);
				contextMenu.add(ContextMenuItem.SEPARATOR);
			}
		}

		contextMenu.add(new ContextMenuItem(I18n.format("selectServer.edit"), ThemeProperties.EDIT_ICON.get(), object::onEditButtonClicked));

		if (object instanceof RandomReward && !QuestObjectBase.isNull(((RandomReward) object).getTable()))
		{
			contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.reward_table.edit"), ThemeProperties.EDIT_ICON.get(), () -> ((RandomReward) object).getTable().onEditButtonClicked()));
		}

		contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), ThemeProperties.DELETE_ICON.get(), () -> ClientQuestFile.INSTANCE.deleteObject(object.id)).setYesNo(I18n.format("delete_item", object.getTitle())));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.reset_progress"), ThemeProperties.RELOAD_ICON.get(), () -> new MessageChangeProgress(ClientQuestFile.INSTANCE.self.getTeamUID(), object.id, isShiftKeyDown() ? ChangeProgress.RESET_DEPS : ChangeProgress.RESET).sendToServer()).setYesNo(I18n.format("ftbquests.gui.reset_progress_q")));

		if (object instanceof QuestObject)
		{
			contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.complete_instantly"), ThemeProperties.CHECK_ICON.get(), () -> new MessageChangeProgress(ClientQuestFile.INSTANCE.self.getTeamUID(), object.id, isShiftKeyDown() ? ChangeProgress.COMPLETE_DEPS : ChangeProgress.COMPLETE).sendToServer()).setYesNo(I18n.format("ftbquests.gui.complete_instantly_q")));
		}

		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.copy_id"), ThemeProperties.WIKI_ICON.get(), () -> setClipboardString(QuestObjectBase.getCodeString(object)))
		{
			@Override
			public void addMouseOverText(List<String> list)
			{
				list.add(QuestObjectBase.getCodeString(object));
			}
		});
	}

	public static void displayError(ITextComponent error)
	{
		Minecraft.getMinecraft().getToastGui().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, new TextComponentTranslation("ftbquests.gui.error"), error));
	}

	@Override
	public boolean keyPressed(int key, char keyChar)
	{
		if (super.keyPressed(key, keyChar))
		{
			return true;
		}
		else if (key == Keyboard.KEY_TAB)
		{
			if (selectedChapter != null && file.chapters.size() > 1)
			{
				List<Chapter> visibleChapters = file.canEdit() ? file.chapters : file.getVisibleChapters(file.self, true);

				if (!visibleChapters.isEmpty())
				{
					selectChapter(visibleChapters.get(MathUtils.mod(visibleChapters.indexOf(selectedChapter) + (isShiftKeyDown() ? -1 : 1), visibleChapters.size())));
				}
			}

			return true;
		}
		else if (keyChar >= '1' && keyChar <= '9')
		{
			int i = keyChar - '1';

			if (i < file.chapters.size())
			{
				selectChapter(file.chapters.get(i));
			}

			return true;
		}
		else if (selectedChapter != null && file.canEdit() && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown())
		{
			switch (key)
			{
				case Keyboard.KEY_A:
					movingQuest = false;
					closeQuest();
					selectedQuests.addAll(selectedChapter.quests);
					break;
				case Keyboard.KEY_D:
					movingQuest = false;
					closeQuest();
					break;
				case Keyboard.KEY_DOWN:
					movingQuest = true;
					for (Quest quest : selectedQuests)
					{
						new MessageMoveQuest(quest.id, quest.x, (byte) (quest.y + 1)).sendToServer();
					}
					movingQuest = false;
					break;
				case Keyboard.KEY_UP:
					movingQuest = true;
					for (Quest quest : selectedQuests)
					{
						new MessageMoveQuest(quest.id, quest.x, (byte) (quest.y - 1)).sendToServer();
					}
					movingQuest = false;
					break;
				case Keyboard.KEY_LEFT:
					movingQuest = true;
					for (Quest quest : selectedQuests)
					{
						new MessageMoveQuest(quest.id, (byte) (quest.x - 1), quest.y).sendToServer();
					}
					movingQuest = false;
					break;
				case Keyboard.KEY_RIGHT:
					movingQuest = true;
					for (Quest quest : selectedQuests)
					{
						new MessageMoveQuest(quest.id, (byte) (quest.x + 1), quest.y).sendToServer();
					}
					movingQuest = false;
					break;
			}

			return true;
		}
		else if (key == Keyboard.KEY_LSHIFT)
		{
			long now = System.currentTimeMillis();

			if (lastShiftPress == 0L)
			{
				lastShiftPress = now;
			}
			else
			{
				if (now - lastShiftPress <= 400L)
				{
					ConfigQuestObject c = new ConfigQuestObject(file, 0, QuestObjectType.CHAPTER.or(QuestObjectType.QUEST));
					GuiSelectQuestObject gui = new GuiSelectQuestObject(c, this, () -> {
						QuestObjectBase o = file.getBase(c.getObject());

						if (o instanceof Chapter)
						{
							selectChapter((Chapter) o);
						}
						else if (o instanceof Quest)
						{
							zoom = 20;
							selectChapter(((Quest) o).chapter);
							viewQuestPanel.hidePanel = false;
							viewQuest((Quest) o);
						}
					});

					gui.focus();
					gui.setTitle(I18n.format("gui.search_box"));
					gui.openGui();
				}

				lastShiftPress = 0L;
			}
		}

		return false;
	}

	@Override
	public void tick()
	{
		if (selectedChapter != null && selectedChapter.invalid)
		{
			selectChapter(null);
		}

		if (selectedChapter == null)
		{
			List<Chapter> visible = file.getVisibleChapters(file.self, !file.canEdit());

			if (!visible.isEmpty())
			{
				selectChapter(visible.get(0));
			}
		}

		super.tick();
	}

	public int getZoom()
	{
		return zoom;
	}

	public void addZoom(int up)
	{
		int z = zoom;
		zoom = MathHelper.clamp(zoom + up * 4, 8, 24);

		if (zoom != z)
		{
			grabbed = 0;
			resetScroll();
			//quests.alignWidgets();
		}
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		QuestTheme.currentObject = selectedChapter;
		super.drawBackground(theme, x, y, w, h);

		int pw = 20;

		Color4I borderColor = ThemeProperties.WIDGET_BORDER.get(selectedChapter);
		Color4I backgroundColor = ThemeProperties.WIDGET_BACKGROUND.get(selectedChapter);

		borderColor.draw(x + pw - 1, y + 1, 1, h - 2);
		backgroundColor.draw(x + 1, y + 1, pw - 2, h - 2);

		borderColor.draw(x + w - pw, y + 1, 1, h - 2);
		backgroundColor.draw(x + w - pw + 1, y + 1, pw - 2, h - 2);

		if (grabbed != 0)
		{
			int mx = getMouseX();
			int my = getMouseY();

			if (scrollWidth > questPanel.width)
			{
				questPanel.setScrollX(Math.max(Math.min(questPanel.getScrollX() + (prevMouseX - mx), scrollWidth - questPanel.width), 0));
			}
			else
			{
				questPanel.setScrollX((scrollWidth - questPanel.width) / 2);
			}

			if (scrollHeight > questPanel.height)
			{
				questPanel.setScrollY(Math.max(Math.min(questPanel.getScrollY() + (prevMouseY - my), scrollHeight - questPanel.height), 0));
			}
			else
			{
				questPanel.setScrollY((scrollHeight - questPanel.height) / 2);
			}

			prevMouseX = mx;
			prevMouseY = my;
		}
	}

	@Override
	public void drawForeground(Theme theme, int x, int y, int w, int h)
	{
		Color4I borderColor = ThemeProperties.WIDGET_BORDER.get(selectedChapter);
		GuiHelper.drawHollowRect(x, y, w, h, borderColor, false);
		super.drawForeground(theme, x, y, w, h);
	}

	@Override
	public Theme getTheme()
	{
		return FTBQuestsTheme.INSTANCE;
	}

	@Override
	public boolean drawDefaultBackground()
	{
		return false;
	}

	public void open(@Nullable QuestObject object)
	{
		Chapter c = chapterHoverPanel.chapter == null ? null : chapterHoverPanel.chapter.chapter;

		if (object instanceof Chapter)
		{
			selectChapter((Chapter) object);
		}
		else if (object instanceof Quest)
		{
			viewQuestPanel.hidePanel = false;
			selectChapter(((Quest) object).chapter);
			viewQuest((Quest) object);
		}
		else if (object instanceof Task)
		{
			viewQuestPanel.hidePanel = false;
			selectChapter(((Task) object).quest.chapter);
			viewQuest(((Task) object).quest);
		}

		openGui();

		if (c != null)
		{
			for (Widget widget : chapterPanel.widgets)
			{
				if (widget instanceof ButtonChapter && c == ((ButtonChapter) widget).chapter)
				{
					chapterHoverPanel.chapter = (ButtonChapter) widget;
					chapterHoverPanel.refreshWidgets();
					chapterHoverPanel.updateMouseOver(getMouseX(), getMouseY());
					break;
				}
			}
		}
	}

	@Override
	public boolean handleClick(String scheme, String path)
	{
		if (scheme.isEmpty() && path.startsWith("#"))
		{
			open(file.get(file.getID(path)));
			return true;
		}

		return super.handleClick(scheme, path);
	}
}