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
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiVariables;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
import com.feed_the_beast.ftbquests.net.MessageCompleteInstantly;
import com.feed_the_beast.ftbquests.net.MessageResetProgress;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObjectQuick;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestVariable;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GuiQuestTree extends GuiBase
{
	public final ClientQuestFile questFile;
	public int scrollWidth, scrollHeight, prevMouseX, prevMouseY, grabbed;
	public QuestChapter selectedChapter;
	public Quest selectedQuest;
	public final Panel chapterPanel, quests, questLeft, questRight, otherButtons;
	public Color4I borderColor, backgroundColor;
	public boolean movingQuest = false;
	public int zoom = 16;

	public GuiQuestTree(ClientQuestFile q)
	{
		questFile = q;

		chapterPanel = new PanelChapters(this);
		chapterPanel.setHeight(20);

		selectedChapter = questFile.chapters.isEmpty() ? null : questFile.chapters.get(0);
		borderColor = Color4I.WHITE.withAlpha(88);
		backgroundColor = Color4I.WHITE.withAlpha(33);

		quests = new PanelQuests(this);
		questLeft = new PanelQuestLeft(this);
		questRight = new PanelQuestRight(this);
		otherButtons = new PanelOtherButtons(this);

		selectChapter(null);
	}

	@Override
	public void addWidgets()
	{
		add(chapterPanel);
		add(quests);
		add(otherButtons);
		add(questLeft);
		add(questRight);
	}

	@Override
	public void alignWidgets()
	{
		otherButtons.alignWidgets();
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
			selectQuest(null);
			selectedChapter = chapter;
			quests.setScrollX(0);
			quests.setScrollY(0);
			quests.refreshWidgets();
			resetScroll(true);
		}
	}

	public void selectQuest(@Nullable Quest quest)
	{
		if (selectedQuest != quest)
		{
			selectedQuest = quest;
			questLeft.refreshWidgets();
			questRight.refreshWidgets();
		}
	}

	public void resetScroll(boolean realign)
	{
		if (realign)
		{
			quests.alignWidgets();
		}

		quests.setScrollX((scrollWidth - quests.width) / 2);
		quests.setScrollY((scrollHeight - quests.height) / 2);
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
							inst.getValue().onClicked(gui, inst, button, () -> new MessageEditObjectQuick(object.uid, inst.getID(), inst.getValue()).sendToServer());
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
		contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> ClientQuestFile.INSTANCE.deleteObject(object.uid)).setYesNo(I18n.format("delete_item", object.getDisplayName().getFormattedText())));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.reset_progress"), GuiIcons.REFRESH, () -> new MessageResetProgress(object.uid).sendToServer()).setYesNo(I18n.format("ftbquests.gui.reset_progress_q")));

		if (object instanceof QuestObject)
		{
			contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.complete_instantly"), QuestsTheme.COMPLETED, () -> new MessageCompleteInstantly(object.uid).sendToServer()).setYesNo(I18n.format("ftbquests.gui.complete_instantly_q")));
		}
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
			if (selectedChapter != null && !questFile.chapters.isEmpty())
			{
				selectChapter(questFile.chapters.get((selectedChapter.getIndex() + 1) % questFile.chapters.size()));
				return true;
			}
		}
		else if (keyChar >= '1' && keyChar <= '9')
		{
			int i = keyChar - '1';

			if (i < questFile.chapters.size())
			{
				selectChapter(questFile.chapters.get(i));
				return true;
			}
		}
		else if (selectedChapter != null && questFile.canEdit() && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown())
		{
			switch (key)
			{
				case Keyboard.KEY_D:
					movingQuest = false;
					selectQuest(null);
					return true;
			}
		}

		return false;
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		if (selectedChapter != null && selectedChapter.invalid)
		{
			selectChapter(null);
		}

		if (selectedChapter == null && !questFile.chapters.isEmpty())
		{
			selectChapter(questFile.chapters.get(0));
		}

		super.drawBackground(theme, x, y, w, h);

		if (grabbed != 0)
		{
			int mx = getMouseX();
			int my = getMouseY();

			if (scrollWidth > quests.width)
			{
				quests.setScrollX(Math.max(Math.min(quests.getScrollX() + (prevMouseX - mx), scrollWidth - quests.width), 0));
			}
			else
			{
				quests.setScrollX((scrollWidth - quests.width) / 2);
			}

			if (scrollHeight > quests.height)
			{
				quests.setScrollY(Math.max(Math.min(quests.getScrollY() + (prevMouseY - my), scrollHeight - quests.height), 0));
			}
			else
			{
				quests.setScrollY((scrollHeight - quests.height) / 2);
			}

			prevMouseX = mx;
			prevMouseY = my;
		}
	}

	@Override
	public void drawForeground(Theme theme, int x, int y, int w, int h)
	{
		GuiHelper.drawHollowRect(x, y, w, h, borderColor, false);

		int start = 1;

		if (!chapterPanel.widgets.isEmpty())
		{
			Widget last = chapterPanel.widgets.get(chapterPanel.widgets.size() - 1);
			start = last.getX() + last.width + 1;
		}

		backgroundColor.draw(start, y + 1, w - start - otherButtons.width - 1, chapterPanel.height - 2);
		borderColor.draw(start, y + chapterPanel.height - 1, w - start - 1, 1);

		if (selectedQuest != null && !movingQuest)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 500F);
			String txt = selectedQuest.getDisplayName().getFormattedText();
			int txts = theme.getStringWidth(txt);
			GuiHelper.drawHollowRect(2, chapterPanel.height + 1, txts + 6, 14, borderColor, false);
			theme.drawGui(3, chapterPanel.height + 2, txts + 4, 12, WidgetType.DISABLED);
			theme.drawString(txt, 5, chapterPanel.height + 4);
			GlStateManager.popMatrix();
		}

		super.drawForeground(theme, x, y, w, h);
	}

	@Override
	public Theme getTheme()
	{
		return QuestsTheme.INSTANCE;
	}

	@Override
	public boolean drawDefaultBackground()
	{
		return false;
	}

	public void open(@Nullable QuestObject object)
	{
		if (object instanceof QuestVariable)
		{
			new GuiVariables().openGui();
			return;
		}
		else if (object instanceof QuestChapter)
		{
			selectChapter((QuestChapter) object);
		}
		else if (object instanceof Quest)
		{
			selectChapter(((Quest) object).chapter);
			selectQuest((Quest) object);
		}
		else if (object instanceof QuestTask)
		{
			selectChapter(((QuestTask) object).quest.chapter);
			selectQuest(((QuestTask) object).quest);
		}

		openGui();
	}
}