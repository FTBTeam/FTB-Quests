package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiContainerWrapper;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.PanelScrollBar;
import com.feed_the_beast.ftblib.lib.gui.ScrollBar;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ImageIcon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.MessageOpenTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.SlotItemHandler;

import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiQuestChest extends GuiBase
{
	private static final ImageIcon TEXTURE = new ImageIcon(new ResourceLocation(FTBQuests.MOD_ID, "textures/gui/chest.png"));
	private static final ImageIcon BACKGROUND = TEXTURE.withUVfromCoords(0, 0, 176, 189, 256, 256);
	private static final ImageIcon SCROLL_BAR = TEXTURE.withUVfromCoords(177, 0, 8, 9, 256, 256);

	private class ButtonTask extends Button
	{
		private final QuestTaskData taskData;

		public ButtonTask(Panel panel, QuestTaskData d)
		{
			super(panel);
			taskData = d;
			setSize(panel.width, 8);
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			if (taskData.task.quest.chapter.file.chapters.size() > 1)
			{
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.chapter") + ": " + TextFormatting.YELLOW + taskData.task.quest.chapter.getDisplayName().getFormattedText());
			}

			list.add(TextFormatting.GRAY + I18n.format("ftbquests.quest") + ": " + TextFormatting.YELLOW + taskData.task.quest.getDisplayName().getFormattedText());
			list.add(TextFormatting.GRAY + I18n.format("ftbquests.progress") + ": " + TextFormatting.BLUE + taskData.getProgressString() + " / " + taskData.task.getMaxProgressString());
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			new MessageOpenTask(taskData.task.getID()).sendToServer();
		}

		@Override
		public void draw()
		{
			int x = getAX();
			int y = getAY();

			double r = taskData.getRelativeProgress();

			if (r > 0D)
			{
				TEXTURE.withUVfromCoords(0, r >= 1D ? 199 : 190, (int) (148 * r), 8, 256, 256).draw(x, y, (int) (width * r), 8);
			}

			taskData.task.getIcon().draw(x + 1, y, 8, 8);
			drawString(taskData.task.getDisplayName().getFormattedText(), x + 11, y, getTheme().getContentColor(getWidgetType()), SHADOW);
		}
	}

	private final ContainerQuestChest container;
	private final Panel tasks;
	private final PanelScrollBar scrollBar;
	private final Button transferAll, openRewards;

	public GuiQuestChest(ContainerQuestChest c)
	{
		setSize(176, 189);
		container = c;

		tasks = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				if (ClientQuestFile.existsWithTeam())
				{
					for (QuestTask task : ClientQuestFile.INSTANCE.allTasks)
					{
						if (task.canInsertItem())
						{
							QuestTaskData data = ClientQuestFile.INSTANCE.self.getQuestTaskData(task);

							if (data.getRelativeProgress() < 1D && data.task.quest.canStartTasks(ClientQuestFile.INSTANCE.self))
							{
								add(new ButtonTask(this, data));
							}
						}
					}

					for (QuestTask task : ClientQuestFile.INSTANCE.allTasks)
					{
						if (task.canInsertItem())
						{
							QuestTaskData data = ClientQuestFile.INSTANCE.self.getQuestTaskData(task);

							if (data.getRelativeProgress() >= 1D)
							{
								add(new ButtonTask(this, data));
							}
						}
					}
				}
			}

			@Override
			public void alignWidgets()
			{
				scrollBar.setMaxValue(align(new WidgetLayout.Vertical(0, 1, 1)));
			}
		};

		tasks.setPosAndSize(8, 9, 148, 68);

		scrollBar = new PanelScrollBar(this, ScrollBar.Plane.VERTICAL, tasks)
		{
			@Override
			public int getSliderSize()
			{
				return getMaxValue() <= 0 ? 0 : 9;
			}

			@Override
			public Icon getIcon()
			{
				return SCROLL_BAR;
			}

			@Override
			public Icon getBackground()
			{
				return Icon.EMPTY;
			}
		};

		scrollBar.setCanAlwaysScroll(true);
		scrollBar.setCanAlwaysScrollPlane(true);
		scrollBar.setPosAndSize(160, 9, 8, 68);

		transferAll = new Button(this, I18n.format("tile.ftbquests.chest.transfer_all"), Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				GuiHelper.playClickSound();

				for (Slot slot : container.inventorySlots)
				{
					if (slot.getHasStack() && !(slot instanceof SlotItemHandler))
					{
						ClientUtils.MC.playerController.windowClick(container.windowId, slot.getSlotIndex(), 0, ClickType.QUICK_MOVE, ClientUtils.MC.player);
					}
				}
			}

			@Override
			public void draw()
			{
				if (isMouseOver())
				{
					Color4I.WHITE.withAlpha(33).draw(getAX(), getAY(), width, height);
				}
			}
		};

		transferAll.setPosAndSize(28, 86, 12, 12);

		openRewards = new Button(this)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				GuiHelper.playClickSound();
			}
		};

		openRewards.setPosAndSize(154, 86, 12, 12);
	}

	@Override
	public void addWidgets()
	{
		add(tasks);
		add(scrollBar);
		add(transferAll);
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		int mx = getAX() - getMouseX();
		int my = getAY() - getMouseY();

		for (Slot slot : container.inventorySlots)
		{
			if (mx >= slot.xPos && my >= slot.yPos && mx < slot.xPos + 16 && my < slot.yPos + 16)
			{
				int i = slot.getSlotIndex();

				if (i == 0)
				{
					list.add(TextFormatting.GRAY + I18n.format("tile.ftbquests.chest.input"));
					list.add(TextFormatting.DARK_GRAY + I18n.format("tile.ftbquests.chest.input_desc"));
				}
				else if (i >= 1 && i <= 7)
				{
					list.add(TextFormatting.GRAY + I18n.format("tile.ftbquests.chest.output"));
					list.add(TextFormatting.DARK_GRAY + I18n.format("tile.ftbquests.chest.output_desc"));
				}
			}
		}

		super.addMouseOverText(list);
	}

	@Override
	public GuiScreen getWrapper()
	{
		return new GuiContainerWrapper(this, container).disableSlotDrawing();
	}

	@Override
	public void drawBackground()
	{
		int x = getAX();
		int y = getAY();

		GlStateManager.color(1F, 1F, 1F, 1F);
		BACKGROUND.draw(x, y, width, height);
	}

	@Override
	public Theme getTheme()
	{
		return QuestsTheme.INSTANCE;
	}
}