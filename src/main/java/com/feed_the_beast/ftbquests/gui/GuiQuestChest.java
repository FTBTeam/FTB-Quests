package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiContainerWrapper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.PanelScrollBar;
import com.feed_the_beast.ftblib.lib.gui.ScrollBar;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ImageIcon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/**
 * @author LatvianModder
 */
public class GuiQuestChest extends GuiBase
{
	private static final ImageIcon TEXTURE = new ImageIcon(new ResourceLocation(FTBQuests.MOD_ID, "textures/gui/chest.png"));
	private static final ImageIcon BACKGROUND = TEXTURE.withUVfromCoords(0, 0, 176, 166, 256, 256);
	private static final ImageIcon SCROLL_BAR = TEXTURE.withUVfromCoords(177, 0, 8, 9, 256, 256);

	public final ContainerQuestChest container;
	public final Panel tasks;
	public final PanelScrollBar scrollBar;

	private static class ButtonTask extends Widget
	{
		private final QuestTaskData taskData;

		public ButtonTask(Panel panel, QuestTaskData d)
		{
			super(panel);
			taskData = d;
			setSize(panel.width, 8);
		}

		@Override
		public void draw()
		{
			int x = getAX();
			int y = getAY();

			double r = taskData.getRelativeProgress();

			if (r > 0D)
			{
				TEXTURE.withUVfromCoords(0, r >= 1D ? 176 : 167, (int) (148 * r), 8, 256, 256).draw(x, y, (int) (width * r), 8);
			}

			taskData.task.getIcon().draw(x + 1, y, 8, 8);
			drawString(taskData.task.getDisplayName().getFormattedText(), x + 11, y, getTheme().getContentColor(getWidgetType()), SHADOW);
		}
	}

	public GuiQuestChest(ContainerQuestChest c)
	{
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
						add(new ButtonTask(this, ClientQuestFile.INSTANCE.self.getQuestTaskData(task)));
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
				return 9;
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
	}

	@Override
	public void addWidgets()
	{
		add(tasks);
		add(scrollBar);
	}

	@Override
	public GuiScreen getWrapper()
	{
		return new GuiContainerWrapper(this, container).disableSlotDrawing();
	}

	@Override
	public boolean mousePressed(MouseButton button)
	{
		if (super.mousePressed(button))
		{
			return true;
		}
		else if (container.enchantItem(ClientUtils.MC.player, button.isLeft() ? 0 : 1))
		{
			ClientUtils.MC.playerController.sendEnchantPacket(container.windowId, button.isLeft() ? 0 : 1);
			return true;
		}

		return false;
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