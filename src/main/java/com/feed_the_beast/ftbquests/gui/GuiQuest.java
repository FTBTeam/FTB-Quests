package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.PanelScrollBar;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfig;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiSelectItemStack;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestProgress;
import com.feed_the_beast.ftbquests.net.MessageGetScreen;
import com.feed_the_beast.ftbquests.net.MessageOpenTask;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.net.edit.MessageEditReward;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + getGui().width / 2F, y + 11, 0);
			GlStateManager.scale(2F, 2F, 1F);
			theme.drawString(text, 0, 0, Theme.CENTERED);
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
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			int gw2 = getGui().width / 2;

			for (int i = 0; i < text.size(); i++)
			{
				theme.drawString(text.get(i), gw2, 1 + y + i * 12, Theme.CENTERED);
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
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			Color4I col = getTheme().getInvertedContentColor();
			int gw2 = getGui().width / 2;

			for (int i = 0; i < text.size(); i++)
			{
				theme.drawString(text.get(i), gw2, 1 + y + i * 12, col, Theme.CENTERED);
			}
		}
	}

	public class ButtonTask extends Button
	{
		public QuestTask task;

		public ButtonTask(Panel panel, QuestTask t)
		{
			super(panel, t.getDisplayName().getFormattedText(), t.getIcon());
			setPosAndSize(0, 20, 20, 20);
			task = t;
		}

		@Override
		public boolean mousePressed(MouseButton button)
		{
			if (isMouseOver())
			{
				if (button.isRight() || getWidgetType() != WidgetType.DISABLED)
				{
					onClicked(button);
				}

				return true;
			}

			return false;
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();

			if (button.isRight() && questTreeGui.questFile.canEdit())
			{
				List<ContextMenuItem> contextMenu = new ArrayList<>();

				if (questTreeGui.questFile.canEdit() || questTreeGui.questFile.self != null && questTreeGui.questFile.allowTakeQuestBlocks && task.quest.isVisible(questTreeGui.questFile.self) && !task.isComplete(questTreeGui.questFile.self))
				{
					contextMenu.add(new ContextMenuItem(I18n.format("tile.ftbquests.screen.name"), Color4I.BLACK, () ->
					{
						if (questTreeGui.questFile.canEdit())
						{
							List<ContextMenuItem> screenContextMenu = new ArrayList<>();
							screenContextMenu.add(new ContextMenuItem("Screen", Icon.EMPTY, () -> {}).setEnabled(false));
							screenContextMenu.add(new ContextMenuItem("1 x 1", Icon.EMPTY, () -> new MessageGetScreen(task.getID(), 0).sendToServer()));
							screenContextMenu.add(new ContextMenuItem("3 x 3", Icon.EMPTY, () -> new MessageGetScreen(task.getID(), 1).sendToServer()));
							screenContextMenu.add(new ContextMenuItem("5 x 5", Icon.EMPTY, () -> new MessageGetScreen(task.getID(), 2).sendToServer()));
							screenContextMenu.add(new ContextMenuItem("7 x 7", Icon.EMPTY, () -> new MessageGetScreen(task.getID(), 3).sendToServer()));
							screenContextMenu.add(new ContextMenuItem("9 x 9", Icon.EMPTY, () -> new MessageGetScreen(task.getID(), 4).sendToServer()));
							getGui().openContextMenu(screenContextMenu);
						}
						else
						{
							new MessageGetScreen(task.getID(), 0).sendToServer();
						}
					}));

					contextMenu.add(ContextMenuItem.SEPARATOR);
				}

				questTreeGui.addObjectMenuItems(contextMenu, getGui(), task);
				getGui().openContextMenu(contextMenu);
			}
			else if (button.isLeft())
			{
				new MessageOpenTask(task.getID()).sendToServer();
			}
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			list.add(getTitle() + ClientQuestProgress.getCompletionSuffix(questTreeGui.questFile.self, task));
		}

		@Override
		public WidgetType getWidgetType()
		{
			if (task.invalid || questTreeGui.questFile.self == null || !quest.canStartTasks(questTreeGui.questFile.self))
			{
				return WidgetType.DISABLED;
			}

			return super.getWidgetType();
		}

		@Override
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			super.draw(theme, x, y, w, h);

			if (task.invalid || questTreeGui.questFile.self == null)
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 500);
				GuiIcons.CLOSE.draw(x + w - 9, y + 1, 8, 8);
				GlStateManager.popMatrix();
			}
			else if (task.isComplete(questTreeGui.questFile.self))
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 500);
				GuiIcons.CHECK.draw(x + w - 9, y + 1, 8, 8);
				GlStateManager.popMatrix();
			}
		}
	}

	public class ButtonAddTask extends Button
	{
		public ButtonAddTask(Panel panel)
		{
			super(panel, I18n.format("gui.add"), GuiIcons.ADD);
			setPosAndSize(0, 20, 20, 20);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			new GuiSelectTaskType().openGui();
		}
	}

	public class GuiSelectTaskType extends GuiButtonListBase
	{
		public GuiSelectTaskType()
		{
			setTitle(I18n.format("ftbquests.gui.select_task_type"));
		}

		@Override
		public void addButtons(Panel panel)
		{
			for (QuestTaskType type : QuestTaskType.getRegistry())
			{
				QuestTask task = type.provider.create(quest, new NBTTagCompound());

				if (task != null)
				{
					panel.add(new SimpleTextButton(panel, type.getDisplayName().getFormattedText(), task.getIcon())
					{
						@Override
						public void onClicked(MouseButton button)
						{
							GuiHelper.playClickSound();

							ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
							ConfigGroup g = group.getGroup("task." + type.getRegistryName().getNamespace() + '.' + type.getRegistryName().getPath());
							task.getConfig(g);
							task.getExtraConfig(g);

							new GuiEditConfig(group, (g1, sender) -> {
								NBTTagCompound nbt = new NBTTagCompound();
								task.writeData(nbt);
								nbt.setString("type", type.getTypeForNBT());
								new MessageCreateObject(QuestObjectType.TASK, quest.getID(), nbt).sendToServer();
								GuiQuest.this.openGui();
								questTreeGui.questFile.refreshGui(questTreeGui.questFile);
							}).openGui();
						}
					});
				}
			}
		}
	}

	public class ButtonReward extends Button
	{
		public ItemStack reward;
		public boolean teamReward;

		public ButtonReward(Panel panel, ItemStack r, boolean t)
		{
			super(panel, t ? TextFormatting.BLUE + r.getDisplayName() : r.getDisplayName(), ItemIcon.getItemIcon(r));
			setPosAndSize(0, 20, 20, 20);
			reward = r;
			teamReward = t;
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();

			if (button.isRight() && questTreeGui.questFile.canEdit())
			{
				List<ContextMenuItem> contextMenu = new ArrayList<>();
				//contextMenu.add(new ContextMenuItem(I18n.format("selectServer.edit"), GuiIcons.SETTINGS, () -> new MessageEditReward(quest.getID(), quest.playerRewards.indexOf(reward), false, ).sendToServer()));
				contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> new MessageEditReward(quest.getID(), (teamReward ? quest.teamRewards : quest.playerRewards).indexOf(reward), teamReward, ItemStack.EMPTY).sendToServer()).setYesNo(I18n.format("delete_item", reward.getDisplayName())));
				getGui().openContextMenu(contextMenu);
			}
			else if (button.isLeft() && questTreeGui.questFile.self != null && quest.isComplete(questTreeGui.questFile.self))
			{
				new GuiRewards().openGui();
			}
		}
	}

	public class ButtonAddReward extends Button
	{
		public ButtonAddReward(Panel panel)
		{
			super(panel, I18n.format("gui.add"), GuiIcons.ADD);
			setPosAndSize(0, 20, 20, 20);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();

			ConfigValueInstance value = new ConfigValueInstance("item", ConfigGroup.DEFAULT, new ConfigItemStack(new ItemStack(Items.APPLE))
			{
				@Override
				public void setStack(ItemStack stack)
				{
					new MessageEditReward(quest.getID(), quest.playerRewards.size(), false, stack).sendToServer();
				}
			});

			new GuiSelectItemStack(value, this).openGui();
		}
	}

	public final GuiQuestTree questTreeGui;
	public final Quest quest;
	public final Panel mainPanel;
	public final Button back;
	public final PanelScrollBar scrollBar;
	public final QuestTitle title;
	public final QuestShortDescription shortDescription;
	public final QuestLongDescription longDescription;
	public final Panel tasks, rewards;

	public GuiQuest(GuiQuestTree ql, Quest q)
	{
		questTreeGui = ql;
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
				Theme theme = getTheme();
				setPosAndSize(0, 3, getGui().width, getGui().height - 6);

				title.text = TextFormatting.BOLD + quest.getDisplayName().getFormattedText();
				title.setSize(width, 35);

				shortDescription.text.clear();
				shortDescription.text.addAll(theme.listFormattedStringToWidth(quest.description, width - 60));

				shortDescription.setSize(width, shortDescription.text.size() * 12 + (shortDescription.text.isEmpty() ? 0 : 15));

				longDescription.text.clear();

				for (String s0 : quest.text)
				{
					longDescription.text.addAll(theme.listFormattedStringToWidth(s0, width - 80));
				}

				longDescription.setSize(width, longDescription.text.size() * 12 + (longDescription.text.isEmpty() ? 0 : 15));

				tasks.alignWidgets();
				rewards.alignWidgets();

				scrollBar.setMaxValue(align(WidgetLayout.VERTICAL) + 10);
			}
		};

		back = new Button(this, I18n.format("gui.back"), GuiIcons.LEFT)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				GuiHelper.playClickSound();
				questTreeGui.questFile.questGui = questTreeGui.questFile.questTreeGui;
				questTreeGui.questFile.questGui.openGui();
			}

			@Override
			public void drawBackground(Theme theme, int x, int y, int w, int h)
			{
			}
		};

		scrollBar = new PanelScrollBar(this, mainPanel);
		scrollBar.setScrollStep(12);
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

				if (questTreeGui.questFile.canEdit())
				{
					add(new ButtonAddTask(this));
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
			public void drawBackground(Theme theme, int x, int y, int w, int h)
			{
				theme.drawString(TextFormatting.RED + I18n.format("ftbquests.tasks"), x + w / 2, y + 9, Theme.CENTERED);
			}
		};

		rewards = new Panel(mainPanel)
		{
			@Override
			public void addWidgets()
			{
				for (ItemStack stack : quest.playerRewards)
				{
					add(new ButtonReward(this, stack, false));
				}

				for (ItemStack stack : quest.teamRewards)
				{
					add(new ButtonReward(this, stack, true));
				}

				if (questTreeGui.questFile.canEdit())
				{
					add(new ButtonAddReward(this));
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
			public void drawBackground(Theme theme, int x, int y, int w, int h)
			{
				theme.drawString(TextFormatting.BLUE + I18n.format("ftbquests.rewards"), x + w / 2, y + 9, Theme.CENTERED);
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

	@Override
	public Theme getTheme()
	{
		return QuestsTheme.INSTANCE;
	}
}