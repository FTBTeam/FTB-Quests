package com.feed_the_beast.ftbquests.gui.chest;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiContainerWrapper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.PanelScrollBar;
import com.feed_the_beast.ftblib.lib.gui.ScrollBar;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.icon.ImageIcon;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

/**
 * @author LatvianModder
 */
public class GuiQuestChest extends GuiBase implements IContainerListener
{
	public static final ImageIcon TEXTURE = new ImageIcon(new ResourceLocation(FTBQuests.MOD_ID, "textures/gui/chest.png"));
	private static final ImageIcon BACKGROUND = TEXTURE.withUVfromCoords(0, 0, 176, 189, 256, 256);
	private static final ImageIcon SCROLL_BAR = TEXTURE.withUVfromCoords(177, 0, 8, 9, 256, 256);

	public final ContainerQuestChest container;
	private final Panel tasks;
	private final PanelScrollBar scrollBar;
	private final Button transferAll, claimAllRewards, inputSlot;
	private final ButtonReward outputSlots[];

	public GuiQuestChest(ContainerQuestChest c)
	{
		setSize(176, 189);
		container = c;

		c.addListener(this);

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

							if (data.getRelativeProgress() == 0L && data.task.quest.canStartTasks(ClientQuestFile.INSTANCE.self))
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

							if (data.getRelativeProgress() == 100L)
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
			public void drawScrollBar(Theme theme, int x, int y, int w, int h)
			{
				SCROLL_BAR.draw(x, y, w, h);
			}

			@Override
			public void drawBackground(Theme theme, int x, int y, int w, int h)
			{
			}
		};

		scrollBar.setCanAlwaysScroll(true);
		scrollBar.setCanAlwaysScrollPlane(true);
		scrollBar.setPosAndSize(160, 9, 8, 68);

		transferAll = new ButtonTransferAll(this);
		transferAll.setPosAndSize(28, 86, 12, 12);

		claimAllRewards = new ButtonClaimAllRewards(this);
		claimAllRewards.setPosAndSize(154, 86, 12, 12);

		inputSlot = new ButtonInputSlot(this);
		inputSlot.setPosAndSize(8, 84, 16, 16);

		outputSlots = new ButtonReward[6];

		for (int i = 0; i < outputSlots.length; i++)
		{
			outputSlots[i] = new ButtonReward(this);
			outputSlots[i].setPosAndSize(44 + i * 18, 84, 16, 16);
		}
	}

	@Override
	public void addWidgets()
	{
		add(tasks);
		add(scrollBar);
		add(transferAll);
		add(claimAllRewards);
		add(inputSlot);

		for (ButtonReward b : outputSlots)
		{
			add(b);
		}

		updateRewards();
	}

	public void updateRewards()
	{
		for (ButtonReward b : outputSlots)
		{
			b.reward = null;
		}

		if (ClientQuestFile.existsWithTeam())
		{
			int index = 0;

			for (QuestChapter chapter : ClientQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.isComplete(ClientQuestFile.INSTANCE.self))
					{
						for (QuestReward reward : quest.rewards)
						{
							if (!ClientQuestFile.INSTANCE.isRewardClaimed(reward))
							{
								outputSlots[index].reward = reward;
								index++;

								if (index == 6)
								{
									return;
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public GuiScreen getWrapper()
	{
		return new GuiContainerWrapper(this, container).disableSlotDrawing();
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		GlStateManager.color(1F, 1F, 1F, 1F);
		BACKGROUND.draw(x, y, w, h);
	}

	@Override
	public Theme getTheme()
	{
		return QuestsTheme.INSTANCE;
	}

	@Override
	public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList)
	{
		if (outputSlots != null)
		{
			updateRewards();
		}
	}

	@Override
	public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack)
	{
		if (outputSlots != null)
		{
			updateRewards();
		}
	}

	@Override
	public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue)
	{
	}

	@Override
	public void sendAllWindowProperties(Container containerIn, IInventory inventory)
	{
	}
}