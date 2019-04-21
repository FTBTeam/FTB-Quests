package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.TextField;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.gui.WidgetVerticalSpace;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

/**
 * @author LatvianModder
 */
public class PanelQuestLeft extends Panel
{
	public final GuiQuestTree treeGui;
	private int maxScroll = 0;

	public PanelQuestLeft(Panel panel)
	{
		super(panel);
		treeGui = (GuiQuestTree) panel.getGui();
	}

	@Override
	public void addWidgets()
	{
		Quest selectedQuest = treeGui.getSelectedQuest();

		if (!treeGui.movingQuest && selectedQuest != null)
		{
			setPos(2, treeGui.chapterPanel.height + 16);
			TextField tasksTextField = new TextField(this).setText(TextFormatting.BLUE + I18n.format("ftbquests.tasks") + ":");
			add(tasksTextField);

			for (QuestTask task : selectedQuest.tasks)
			{
				add(new ButtonTask(this, task));
			}

			if (treeGui.file.canEdit())
			{
				add(new ButtonAddTask(this, selectedQuest));
			}

			if (!selectedQuest.isComplete(ClientQuestFile.INSTANCE.self))
			{
				add(new WidgetVerticalSpace(this, 2));
				add(new ButtonQuickComplete(this));
			}

			boolean addedText = false;

			for (QuestObject dependency : selectedQuest.dependencies)
			{
				if (!dependency.invalid)
				{
					if (!addedText)
					{
						addedText = true;
						add(new WidgetVerticalSpace(this, 2));
						add(new TextField(this).setText(TextFormatting.AQUA + I18n.format("ftbquests.gui.requires") + ":"));
					}

					ITextComponent component = dependency.getDisplayName().createCopy();
					component.getStyle().setColor(TextFormatting.GRAY);
					component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, dependency.toString()));
					component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("gui.open")));
					add(new TextField(this).setText(component));
				}
			}

			addedText = false;

			for (QuestChapter chapter : treeGui.file.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.hasDependency(selectedQuest))
					{
						if (!addedText)
						{
							addedText = true;
							add(new WidgetVerticalSpace(this, 2));
							add(new TextField(this).setText(TextFormatting.YELLOW + I18n.format("ftbquests.gui.required_by") + ":"));
						}

						ITextComponent component = quest.getDisplayName().createCopy();
						component.getStyle().setColor(TextFormatting.GRAY);
						component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, quest.toString()));
						component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("gui.open")));
						add(new TextField(this).setText(component));
					}
				}
			}

			setWidth(20);

			for (Widget widget : widgets)
			{
				setWidth(Math.max(width, widget.width + 6));
			}

			if (width > 150)
			{
				setWidth(150);
			}

			setHeight(Math.min(treeGui.height - treeGui.chapterPanel.height - 18, align(new WidgetLayout.Vertical(3, 3, 3))));

			for (Widget widget : widgets)
			{
				widget.setWidth(width - 6);
				widget.setX(3);
			}

			if (selectedQuest.canRepeat)
			{
				add(new LabelCanRepeatQuest(this).setPosAndSize(width - 11, tasksTextField.posY, 8, 8));
			}

			maxScroll = 0;

			for (Widget widget : widgets)
			{
				maxScroll = Math.max(maxScroll, widget.posY + widget.height + 3);
			}
		}

		if (widgets.isEmpty())
		{
			setPosAndSize(-100, 0, 0, 0);
		}
	}

	@Override
	public void alignWidgets()
	{
	}

	@Override
	public boolean mousePressed(MouseButton button)
	{
		return super.mousePressed(button) || isMouseOver();
	}

	@Override
	public boolean mouseScrolled(int scroll)
	{
		if (isMouseOver())
		{
			setScrollY(MathHelper.clamp(getScrollY() - scroll / 10, 0, maxScroll - height));
			return true;
		}

		return super.mouseScrolled(scroll);
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(0F, 0F, 500F);
		super.draw(theme, x, y, w, h);
		GlStateManager.popMatrix();
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		if (w > 0)
		{
			GuiHelper.drawHollowRect(x, y, w, h, treeGui.borderColor, false);
			theme.drawGui(x + 1, y + 1, w - 2, h - 2, WidgetType.DISABLED);
		}
	}
}