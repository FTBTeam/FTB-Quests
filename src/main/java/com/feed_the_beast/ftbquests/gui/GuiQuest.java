package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.GuiLang;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.PanelScrollBar;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.gui.misc.ThemeVanilla;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GuiQuest extends GuiBase
{
	private class QuestTitle extends Widget
	{
		private String text = "";

		private QuestTitle(Panel panel)
		{
			super(panel);
		}

		@Override
		public void draw()
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(getAX() + getGui().width / 2, getAY() + 11, 0);
			GlStateManager.scale(2F, 2F, 1F);
			drawString(text, 0, 0, CENTERED);
			GlStateManager.popMatrix();
		}
	}

	private class QuestShortDescription extends Widget
	{
		private final List<String> text = new ArrayList<>();

		private QuestShortDescription(Panel panel)
		{
			super(panel);
		}

		@Override
		public void draw()
		{
			for (int i = 0; i < text.size(); i++)
			{
				drawString(text.get(i), getGui().width / 2, 1 + getAY() + i * 12, CENTERED);
			}
		}
	}

	private class QuestLongDescription extends Widget
	{
		private final List<String> text = new ArrayList<>();

		private QuestLongDescription(Panel panel)
		{
			super(panel);
		}

		@Override
		public void draw()
		{
			for (int i = 0; i < text.size(); i++)
			{
				drawString(text.get(i), getGui().width / 2, 1 + getAY() + i * 12, ThemeVanilla.CONTENT_COLOR_DARK, CENTERED);
			}
		}
	}

	public class ButtonTask extends Button
	{
		public QuestTask task;

		public ButtonTask(Panel panel, QuestTask t)
		{
			super(panel, "", t.getIcon());
			setPosAndSize(0, 20, 20, 20);
			task = t;
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			task.addText(list);

			if (GuiScreen.isShiftKeyDown())
			{
				list.add(task.getCompletionString(ClientQuestList.INSTANCE));
			}
		}

		@Override
		public Icon getIcon()
		{
			return icon;
		}

		@Override
		public void draw()
		{
			int ax = getAX();
			int ay = getAY();

			getButtonBackground().draw(ax, ay, width, height);

			if (!icon.isEmpty())
			{
				icon.draw(ax + (width - 16) / 2, ay + (height - 16) / 2, 16, 16);
			}
		}
	}

	public class ButtonReward extends Button
	{
		public QuestReward reward;

		public ButtonReward(Panel panel, QuestReward r)
		{
			super(panel, r.toString(), r.getIcon());
			setPosAndSize(0, 20, 20, 20);
			reward = r;
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
		}

		@Override
		public Icon getIcon()
		{
			return icon;
		}

		@Override
		public WidgetType getWidgetType()
		{
			if (!quest.isComplete(ClientQuestList.INSTANCE))
			{
				return WidgetType.DISABLED;
			}

			return super.getWidgetType();
		}

		@Override
		public void draw()
		{
			int ax = getAX();
			int ay = getAY();

			getButtonBackground().draw(ax, ay, width, height);

			if (!icon.isEmpty())
			{
				icon.draw(ax + (width - 16) / 2, ay + (height - 16) / 2, 16, 16);
			}
		}
	}

	public final Quest quest;
	public final Panel mainPanel;
	public final Button back;
	public final PanelScrollBar scrollBar;
	public final QuestTitle title;
	public final QuestShortDescription shortDescription;
	public final QuestLongDescription longDescription;
	public final Panel tasks, rewards;

	public GuiQuest(Quest q)
	{
		quest = q;

		mainPanel = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				add(title);
				add(shortDescription);
				add(longDescription);
				add(tasks);
				add(rewards);
			}

			@Override
			public void alignWidgets()
			{
				setPosAndSize(0, 3, getGui().width, getGui().height - 6);

				title.text = StringUtils.bold(quest.title.createCopy(), true).getFormattedText();
				title.setSize(width, 35);

				shortDescription.text.clear();

				for (String s : listFormattedStringToWidth(quest.description.getFormattedText(), width - 60))
				{
					if (!s.trim().isEmpty())
					{
						shortDescription.text.add(s);
					}
				}

				shortDescription.setSize(width, shortDescription.text.size() * 12 + (shortDescription.text.isEmpty() ? 0 : 15));

				longDescription.text.clear();

				for (ITextComponent component : quest.text)
				{
					for (String s : listFormattedStringToWidth(component.getFormattedText(), width - 80))
					{
						if (!s.trim().isEmpty())
						{
							longDescription.text.add(s);
						}
					}
				}

				longDescription.setSize(width, longDescription.text.size() * 12 + (longDescription.text.isEmpty() ? 0 : 15));

				tasks.alignWidgets();
				rewards.alignWidgets();

				scrollBar.setElementSize(align(WidgetLayout.VERTICAL) + 10);
				scrollBar.setSrollStepFromOneElementSize(12);
			}
		};

		mainPanel.addFlags(DEFAULTS);

		back = new Button(this, GuiLang.BACK.translate(), GuiIcons.LEFT)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				GuiHelper.playClickSound();
				FTBQuestsClient.questGui = FTBQuestsClient.questTreeGui;
				FTBQuestsClient.questGui.openGui();
			}
		};

		scrollBar = new PanelScrollBar(this, mainPanel);
		title = new QuestTitle(mainPanel);
		shortDescription = new QuestShortDescription(mainPanel);
		longDescription = new QuestLongDescription(mainPanel);

		tasks = new Panel(mainPanel)
		{
			@Override
			public void addWidgets()
			{
				for (QuestTask task : quest.tasks)
				{
					add(new ButtonTask(this, task));
				}
			}

			@Override
			public void alignWidgets()
			{
				if (widgets.isEmpty())
				{
					setSize(width, 0);
				}
				else
				{
					setSize(align(new WidgetLayout.Horizontal(0, 4, 0)), 40);
					setX((getGui().width - width) / 2);
				}
			}

			@Override
			public void drawPanelBackground(int ax, int ay)
			{
				drawString(TextFormatting.RED + "Tasks:", ax + width / 2, ay + 9, CENTERED); //LANG
			}
		};

		rewards = new Panel(mainPanel)
		{
			@Override
			public void addWidgets()
			{
				for (QuestReward reward : quest.rewards)
				{
					add(new ButtonReward(this, reward));
				}
			}

			@Override
			public void alignWidgets()
			{
				if (widgets.isEmpty())
				{
					setSize(width, 0);
				}
				else
				{
					setSize(align(new WidgetLayout.Horizontal(0, 4, 0)), 40);
					setX((getGui().width - width) / 2);
				}
			}

			@Override
			public void drawPanelBackground(int ax, int ay)
			{
				drawString(TextFormatting.BLUE + "Rewards:", ax + width / 2, ay + 9, CENTERED); //LANG
			}
		};
	}

	@Override
	public void addWidgets()
	{
		add(mainPanel);
		add(back);
		add(scrollBar);
	}

	@Override
	public void alignWidgets()
	{
		scrollBar.setPosAndSize(width - 15, 5, 10, height - 10);
		back.setPosAndSize(4, 4, 16, 16);
		mainPanel.alignWidgets();
	}

	@Override
	public boolean onInit()
	{
		setSize(getScreen().getScaledWidth() - 8, getScreen().getScaledHeight() - 8);
		return true;
	}

	@Override
	@Nullable
	public GuiScreen getPrevScreen()
	{
		return null;
	}
}