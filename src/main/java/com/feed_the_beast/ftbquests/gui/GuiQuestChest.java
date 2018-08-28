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
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.net.MessageOpenTask;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.SlotItemHandler;

import java.util.List;
import java.util.UUID;

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
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			int r = (int) (taskData.getRelativeProgress() * width / 100L);

			if (r > 0L)
			{
				TEXTURE.withUVfromCoords(0, r >= width ? 199 : 190, r, 8, 256, 256).draw(x, y, r, 8);
			}

			taskData.task.getIcon().draw(x + 1, y, 8, 8);
			theme.drawString(taskData.task.getDisplayName().getFormattedText(), x + 11, y, theme.getContentColor(getWidgetType()), Theme.SHADOW);
		}
	}

	private class ButtonReward extends Button
	{
		public QuestReward reward = null;

		public ButtonReward(Panel panel)
		{
			super(panel);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			if (reward != null && reward.quest.isComplete(ClientQuestFile.INSTANCE.self) && !ClientQuestFile.INSTANCE.self.isRewardClaimed(ClientUtils.MC.player.getUniqueID(), reward))
			{
				new MessageClaimReward(reward.uid).sendToServer();
			}
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			if (reward == null)
			{
				list.add(TextFormatting.GRAY + I18n.format("tile.ftbquests.chest.output"));
				list.add(TextFormatting.DARK_GRAY + I18n.format("tile.ftbquests.chest.output_desc"));
			}
			else
			{
				List<String> tooltip = reward.stack.getTooltip(ClientUtils.MC.player, ITooltipFlag.TooltipFlags.NORMAL);
				list.add(reward.stack.getRarity().rarityColor + tooltip.get(0));

				for (int i = 1; i < tooltip.size(); i++)
				{
					list.add(TextFormatting.GRAY + tooltip.get(i));
				}
			}
		}

		@Override
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			if (reward != null)
			{
				GuiHelper.drawItem(reward.stack, x, y, true, Icon.EMPTY);
			}

			if (isMouseOver())
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 500);
				Color4I.WHITE.withAlpha(150).draw(x, y, w, h);
				GlStateManager.popMatrix();
			}
		}
	}

	private final ContainerQuestChest container;
	private final Panel tasks;
	private final PanelScrollBar scrollBar;
	private final Button transferAll, openRewards, inputSlot;
	private final ButtonReward outputSlots[];

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
						ClientUtils.MC.playerController.windowClick(container.windowId, slot.slotNumber, 0, ClickType.QUICK_MOVE, ClientUtils.MC.player);
					}
				}
			}

			@Override
			public void draw(Theme theme, int x, int y, int w, int h)
			{
				if (isMouseOver())
				{
					Color4I.WHITE.withAlpha(33).draw(x, y, w, h);
				}
			}
		};

		transferAll.setPosAndSize(28, 86, 12, 12);

		openRewards = new Button(this, I18n.format("ftbquests.rewards"), Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				GuiHelper.playClickSound();
				new GuiRewards().openGui();
			}

			@Override
			public void draw(Theme theme, int x, int y, int w, int h)
			{
				if (isMouseOver())
				{
					Color4I.WHITE.withAlpha(33).draw(x, y, w, h);
				}
			}
		};

		openRewards.setPosAndSize(154, 86, 12, 12);

		inputSlot = new Button(this)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				if (container.enchantItem(ClientUtils.MC.player, 0))
				{
					ClientUtils.MC.playerController.sendEnchantPacket(container.windowId, 0);
				}
			}

			@Override
			public void addMouseOverText(List<String> list)
			{
				list.add(TextFormatting.GRAY + I18n.format("tile.ftbquests.chest.input"));
				list.add(TextFormatting.DARK_GRAY + I18n.format("tile.ftbquests.chest.input_desc"));
			}

			@Override
			public void draw(Theme theme, int x, int y, int w, int h)
			{
				if (isMouseOver())
				{
					Color4I.WHITE.withAlpha(150).draw(x, y, w, h);
				}
			}
		};

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
		add(openRewards);
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
			UUID playerId = ClientUtils.MC.player.getUniqueID();

			for (QuestChapter chapter : ClientQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.isComplete(ClientQuestFile.INSTANCE.self))
					{
						for (QuestReward reward : quest.rewards)
						{
							if (!ClientQuestFile.INSTANCE.self.isRewardClaimed(playerId, reward))
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
}