package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.BlankPanel;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.ColorWidget;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.TextField;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.gui.WidgetVerticalSpace;
import com.feed_the_beast.ftblib.lib.gui.misc.CompactGridLayout;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class PanelViewQuest extends Panel
{
	public final GuiQuestTree gui;
	public Quest quest = null;
	public boolean hidePanel = false;
	private String title = "";
	private Icon icon = Icon.EMPTY;
	public Button buttonClose;
	public BlankPanel panelContent;
	public BlankPanel panelTasks;
	public BlankPanel panelRewards;
	public BlankPanel panelText;

	public PanelViewQuest(GuiQuestTree g)
	{
		super(g);
		gui = g;
		setPosAndSize(-1, -1, 0, 0);
		setOnlyRenderWidgetsInside(true);
		setOnlyInteractWithWidgetsInside(true);
	}

	@Override
	public void addWidgets()
	{
		setPosAndSize(-1, -1, 1, 1);

		if (quest == null || hidePanel)
		{
			return;
		}

		setScrollX(0);
		setScrollY(0);

		title = GuiQuestTree.fixI18n(TextFormatting.GRAY, quest.getDisplayName().getFormattedText());
		icon = quest.getIcon();

		int w = Math.max(parent.width / 5 * 2, gui.getTheme().getStringWidth(title) + 30);

		add(panelContent = new BlankPanel(this, "ContentPanel"));
		panelContent.add(panelTasks = new BlankPanel(panelContent, "TasksPanel"));
		panelContent.add(panelRewards = new BlankPanel(panelContent, "RewardsPanel"));
		panelContent.add(panelText = new BlankPanel(panelContent, "TextPanel"));

		if (!quest.tasks.isEmpty())
		{
			for (QuestTask task : quest.tasks)
			{
				panelTasks.add(new ButtonTask(panelTasks, task));
			}
		}
		else if (!gui.file.canEdit())
		{
			TextFieldDisabledButton noTasks = new TextFieldDisabledButton(panelTasks, TextFormatting.GRAY + I18n.format("ftbquests.gui.no_tasks"));
			noTasks.setSize(noTasks.width + 8, 18);
			panelTasks.add(noTasks);
		}

		if (!quest.rewards.isEmpty())
		{
			for (QuestReward reward : quest.rewards)
			{
				panelRewards.add(new ButtonReward(panelRewards, reward));
			}
		}
		else if (!gui.file.canEdit())
		{
			TextFieldDisabledButton noRewards = new TextFieldDisabledButton(panelRewards, TextFormatting.GRAY + I18n.format("ftbquests.gui.no_rewards"));
			noRewards.setSize(noRewards.width + 8, 18);
			panelRewards.add(noRewards);
		}

		if (gui.file.canEdit())
		{
			panelTasks.add(new ButtonAddTask(panelTasks, quest));
			panelRewards.add(new ButtonAddReward(panelRewards, quest));
		}

		int ww = 0;

		for (Widget widget : panelTasks.widgets)
		{
			ww = Math.max(ww, widget.width);
		}

		for (Widget widget : panelRewards.widgets)
		{
			ww = Math.max(ww, widget.width);
		}

		ww = MathHelper.clamp(ww, 70, 140);
		w = Math.max(w, ww * 2 + 10);

		if (w % 2 == 0)
		{
			w++;
		}

		setWidth(w);
		panelContent.setPosAndSize(0, 16, w, 0);
		int w2 = panelContent.width / 2;

		add(buttonClose = new ButtonCloseViewQuest(this));
		buttonClose.setPosAndSize(w - 14, 2, 12, 12);

		TextField textFieldTasks = new TextField(panelContent)
		{
			@Override
			public TextField resize(Theme theme)
			{
				return this;
			}
		};

		textFieldTasks.setPosAndSize(2, 2, w2 - 3, 13);
		textFieldTasks.addFlags(Theme.CENTERED | Theme.CENTERED_V);
		textFieldTasks.setText(TextFormatting.BLUE + I18n.format("ftbquests.tasks"));
		panelContent.add(textFieldTasks);

		TextField textFieldRewards = new TextField(panelContent)
		{
			@Override
			public TextField resize(Theme theme)
			{
				return this;
			}
		};

		textFieldRewards.setPosAndSize(w2 + 2, 2, w2 - 3, 13);
		textFieldRewards.addFlags(Theme.CENTERED | Theme.CENTERED_V);
		textFieldRewards.setText(TextFormatting.GOLD + I18n.format("ftbquests.rewards"));
		panelContent.add(textFieldRewards);

		panelTasks.setPosAndSize(2, 16, w2 - 3, 0);
		panelRewards.setPosAndSize(w2 + 2, 16, w2 - 3, 0);

		int at = panelTasks.align(new CompactGridLayout(20));
		int ar = panelRewards.align(new CompactGridLayout(20));

		int h = Math.max(at, ar);
		panelTasks.setHeight(h);
		panelRewards.setHeight(h);

		int tox = (panelTasks.width - panelTasks.getContentWidth()) / 2;
		int rox = (panelRewards.width - panelRewards.getContentWidth()) / 2;
		int toy = (panelTasks.height - panelTasks.getContentHeight()) / 2;
		int roy = (panelRewards.height - panelRewards.getContentHeight()) / 2;

		for (Widget widget : panelTasks.widgets)
		{
			widget.setX(widget.posX + tox);
			widget.setY(widget.posY + toy);
		}

		for (Widget widget : panelRewards.widgets)
		{
			widget.setX(widget.posX + rox);
			widget.setY(widget.posY + roy);
		}

		panelText.setPosAndSize(3, 16 + h + 12, panelContent.width - 6, 0);

		String desc = GuiQuestTree.fixI18n(TextFormatting.GRAY, quest.description);

		if (!desc.isEmpty())
		{
			panelText.add(new TextField(panelText).addFlags(Theme.CENTERED).setMaxWidth(panelText.width).setSpacing(9).setText(TextFormatting.ITALIC + desc));
		}

		boolean showText = !quest.hideTextUntilComplete || gui.file.self != null && quest.isComplete(gui.file.self);

		if (showText && !quest.text.isEmpty())
		{
			if (!desc.isEmpty())
			{
				panelText.add(new WidgetVerticalSpace(panelText, 7));
			}

			String[] text = new String[quest.text.size()];

			for (int i = 0; i < text.length; i++)
			{
				text[i] = GuiQuestTree.fixI18n(null, quest.text.get(i));
			}

			panelText.add(new TextField(panelText).setMaxWidth(panelText.width).setSpacing(9).setText(StringUtils.addFormatting(StringJoiner.with('\n').joinStrings(text))));
		}

		if (showText && !quest.guidePage.isEmpty())
		{
			if (!desc.isEmpty())
			{
				panelText.add(new WidgetVerticalSpace(panelText, 7));
			}

			panelText.add(new ButtonOpenInGuide(panelText, quest));
		}

		if (panelText.widgets.isEmpty())
		{
			panelContent.add(new ColorWidget(panelContent, gui.borderColor, null).setPosAndSize(w2, 0, 1, h + 40));
			panelText.setHeight(0);
			setHeight(Math.min(panelContent.getContentHeight(), parent.height - 10));
		}
		else
		{
			panelContent.add(new ColorWidget(panelContent, gui.borderColor, null).setPosAndSize(w2, 0, 1, 16 + h + 6));
			panelContent.add(new ColorWidget(panelContent, gui.borderColor, null).setPosAndSize(1, 16 + h + 6, panelContent.width - 2, 1));
			panelText.setHeight(panelText.align(new WidgetLayout.Vertical(0, 0, 1)));
			setHeight(Math.min(panelContent.getContentHeight() + 20, parent.height - 10));
		}

		setPos((parent.width - width) / 2, (parent.height - height) / 2);
		panelContent.setHeight(height - 17);

		/* Put this somewhere
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
						add(new TextField(this, TextFormatting.YELLOW + I18n.format("ftbquests.gui.required_by") + ":"));
					}

					ITextComponent component = quest.getDisplayName().createCopy();
					component.getStyle().setColor(TextFormatting.GRAY);
					component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, quest.toString()));
					component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("gui.open")));
					add(new TextField(this).setText(component));
				}
			}
		}
		*/
	}

	@Override
	public void alignWidgets()
	{
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		if (quest != null && !hidePanel)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 500F);
			super.draw(theme, x, y, w, h);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		Color4I.DARK_GRAY.withAlpha(120).draw(gui.getX(), gui.getY(), gui.width, gui.height);
		theme.drawContextMenuBackground(x, y, w, h);
		theme.drawString(title, x + w / 2, y + 4, Color4I.WHITE, Theme.CENTERED);
		icon.draw(x + 2, y + 2, 12, 12);
		((GuiQuestTree) getGui()).borderColor.draw(x + 1, y + 15, w - 2, 1);
	}

	@Override
	public boolean mousePressed(MouseButton button)
	{
		return super.mousePressed(button) || isMouseOver();
	}
}