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
import com.feed_the_beast.ftbquests.net.edit.MessageEditObjectQuick;
import com.feed_the_beast.ftbquests.net.edit.MessageMoveQuest;
import com.feed_the_beast.ftbquests.quest.EnumChangeProgress;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.reward.RandomReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
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
	public QuestChapter selectedChapter;
	public final HashSet<Quest> selectedQuests;
	public final PanelChapters chapterPanel;
	public final PanelQuests questPanel;
	public final PanelOtherButtonsBottom otherButtonsBottomPanel;
	public final PanelOtherButtonsTop otherButtonsTopPanel;
	public final PanelChapterHover chapterHoverPanel;
	public final PanelViewQuest viewQuestPanel;
	public Color4I borderColor, backgroundColor;
	public boolean movingQuest = false;
	public int zoom = 16;
	public long lastShiftPress = 0L;

	public GuiQuestTree(ClientQuestFile q)
	{
		file = q;
		selectedQuests = new HashSet<>();

		chapterPanel = new PanelChapters(this);
		selectedChapter = file.chapters.isEmpty() ? null : file.chapters.get(0);
		borderColor = Color4I.WHITE.withAlpha(88);
		backgroundColor = Color4I.WHITE.withAlpha(33);

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
		otherButtonsBottomPanel.alignWidgets();
		otherButtonsTopPanel.alignWidgets();
		chapterPanel.alignWidgets();
	}

	@Override
	public boolean onInit()
	{
		return setFullscreen();
	}

	public void selectChapter(@Nullable QuestChapter chapter)
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
		object.getConfig(Minecraft.getMinecraft().player, g);

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
							inst.getValue().onClicked(gui, inst, button, () -> new MessageEditObjectQuick(object.id, inst.getID(), inst.getValue()).sendToServer());
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

		contextMenu.add(new ContextMenuItem(I18n.format("selectServer.edit"), GuiIcons.SETTINGS, object::onEditButtonClicked));

		if (object instanceof RandomReward && !QuestObjectBase.isNull(((RandomReward) object).getTable()))
		{
			contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.reward_table.edit"), GuiIcons.SETTINGS, () -> ((RandomReward) object).getTable().onEditButtonClicked()));
		}

		contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> ClientQuestFile.INSTANCE.deleteObject(object.id)).setYesNo(I18n.format("delete_item", object.getTitle())));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.reset_progress"), GuiIcons.REFRESH, () -> new MessageChangeProgress(ClientQuestFile.INSTANCE.self.getTeamUID(), object.id, EnumChangeProgress.RESET).sendToServer()).setYesNo(I18n.format("ftbquests.gui.reset_progress_q")));

		if (object instanceof QuestObject)
		{
			contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.complete_instantly"), FTBQuestsTheme.COMPLETED, () -> new MessageChangeProgress(ClientQuestFile.INSTANCE.self.getTeamUID(), object.id, EnumChangeProgress.COMPLETE).sendToServer()).setYesNo(I18n.format("ftbquests.gui.complete_instantly_q")));
		}

		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.copy_id"), GuiIcons.INFO, () -> setClipboardString(QuestObjectBase.getCodeString(object)))
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
				List<QuestChapter> visibleChapters = file.canEdit() ? file.chapters : file.getVisibleChapters(file.self, true);

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

						if (o instanceof QuestChapter)
						{
							selectChapter((QuestChapter) o);
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
			List<QuestChapter> visible = file.getVisibleChapters(file.self, !file.canEdit());

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
		super.drawBackground(theme, x, y, w, h);

		int pw = 20;

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
		GuiHelper.drawHollowRect(x, y, w, h, borderColor, false);

		if (file.canEdit())
		{
			for (Widget widget : questPanel.widgets)
			{
				if (widget.isMouseOver())
				{
					if (widget instanceof ButtonQuest)
					{
						theme.pushFontUnicode(true);
						theme.drawString("X: " + ((ButtonQuest) widget).quest.x, x + 22, y + h - 18);
						theme.drawString("Y: " + ((ButtonQuest) widget).quest.y, x + 22, y + h - 10);
						theme.popFontUnicode();
						break;
					}
					else if (widget instanceof ButtonDummyQuest)
					{
						theme.pushFontUnicode(true);
						theme.drawString("X: " + ((ButtonDummyQuest) widget).x, x + 22, y + h - 18);
						theme.drawString("Y: " + ((ButtonDummyQuest) widget).y, x + 22, y + h - 10);
						theme.popFontUnicode();
						break;
					}
				}
			}
		}

		//backgroundColor.draw(start, y + 1, w - start - otherButtons.width - 1, chapterPanel.height - 2);
		//borderColor.draw(start, y + chapterPanel.height - 1, w - start - 1, 1);

		/*
		if (file.canEdit())
		{
			Widget widget = quests.getWidget(0);

			if (widget instanceof ButtonQuest || widget instanceof ButtonDummyQuest)
			{
				double bsize = getZoom() * 2D - 2D;
				double mx = getMouseX() - quests.getX();//quests.width / 2D;
				double my = getMouseY() - quests.getY();//quests.height / 2D;
				double cx = (quests.getScrollX() + mx) / bsize - Quest.POS_LIMIT;
				double cy = (quests.getScrollY() + my) / bsize - Quest.POS_LIMIT;

				theme.drawString("X: " + StringUtils.formatDouble(cx), 4, 23);
				theme.drawString("Y: " + StringUtils.formatDouble(cy), 4, 32);
			}
		}
		*/

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
		QuestChapter c = chapterHoverPanel.chapter == null ? null : chapterHoverPanel.chapter.chapter;

		if (object instanceof QuestChapter)
		{
			selectChapter((QuestChapter) object);
		}
		else if (object instanceof Quest)
		{
			viewQuestPanel.hidePanel = false;
			selectChapter(((Quest) object).chapter);
			viewQuest((Quest) object);
		}
		else if (object instanceof QuestTask)
		{
			viewQuestPanel.hidePanel = false;
			selectChapter(((QuestTask) object).quest.chapter);
			viewQuest(((QuestTask) object).quest);
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
			open(file.get(QuestFile.getID(path)));
			return true;
		}

		return super.handleClick(scheme, path);
	}
}