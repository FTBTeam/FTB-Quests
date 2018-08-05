package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiContainerWrapper;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.net.MessageGetBlock;
import com.feed_the_beast.ftbquests.net.MessageGetScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiTaskBase extends GuiBase
{
	public final ContainerTaskBase container;
	public final boolean hasTile;
	public final Panel tabs;
	public final String taskName;
	public final Icon taskIcon;

	public static class Tab extends Button
	{
		public Tab(Panel panel, String title, Icon icon)
		{
			super(panel, title, icon);
			setSize(20, 20);
		}

		@Override
		public void onClicked(MouseButton button)
		{
		}
	}

	public GuiTaskBase(ContainerTaskBase c)
	{
		container = c;
		hasTile = container.tile != null && !container.tile.isInvalid();

		tabs = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				if (ClientQuestFile.INSTANCE.allowTakeQuestBlocks.getBoolean() && container.data.task.quest.isVisible(ClientQuestFile.INSTANCE) && !container.data.task.isComplete(ClientQuestFile.INSTANCE))
				{
					add(new Tab(this, I18n.format("ftbquests.task.get_block"), ItemIcon.getItemIcon(new ItemStack(FTBQuestsItems.QUEST_BLOCK)))
					{
						@Override
						public void onClicked(MouseButton button)
						{
							GuiHelper.playClickSound();
							new MessageGetBlock(container.data.task.id).sendToServer();
						}
					});
				}

				if (ClientQuestFile.INSTANCE.editingMode)
				{
					add(new Tab(this, I18n.format("tile.ftbquests.screen.name"), ItemIcon.getItemIcon(new ItemStack(FTBQuestsItems.SCREEN)))
					{
						@Override
						public void onClicked(MouseButton button)
						{
							GuiHelper.playClickSound();
							List<ContextMenuItem> contextMenu = new ArrayList<>();
							contextMenu.add(new ContextMenuItem("1 x 1", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.id, 0, 0).sendToServer()));
							contextMenu.add(new ContextMenuItem("3 x 3", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.id, 1, 0).sendToServer()));
							contextMenu.add(new ContextMenuItem("5 x 5", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.id, 2, 0).sendToServer()));
							contextMenu.add(new ContextMenuItem("7 x 7", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.id, 3, 0).sendToServer()));
							contextMenu.add(new ContextMenuItem("9 x 9", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.id, 4, 0).sendToServer()));
							contextMenu.add(ContextMenuItem.SEPARATOR);
							contextMenu.add(new ContextMenuItem("1 x 1", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.id, 0, 1).sendToServer()));
							contextMenu.add(new ContextMenuItem("3 x 3", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.id, 1, 1).sendToServer()));
							contextMenu.add(new ContextMenuItem("5 x 5", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.id, 2, 1).sendToServer()));
							contextMenu.add(new ContextMenuItem("7 x 7", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.id, 3, 1).sendToServer()));
							contextMenu.add(new ContextMenuItem("9 x 9", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.id, 4, 1).sendToServer()));
							getGui().openContextMenu(contextMenu);
						}
					});
				}

				addTabs(this);
			}

			@Override
			public void alignWidgets()
			{
				setHeight(align(WidgetLayout.VERTICAL));
			}
		};

		tabs.setPosAndSize(-19, 8, 20, 0);
		taskName = container.data.task.getDisplayName().getFormattedText();
		taskIcon = container.data.task.getIcon();
	}

	public void addTabs(Panel panel)
	{
	}

	@Override
	public void addWidgets()
	{
		add(tabs);
	}

	@Override
	public GuiScreen getWrapper()
	{
		return new GuiContainerWrapper(this, container);
	}

	@Override
	public void drawBackground()
	{
		super.drawBackground();

		int ax = getAX();
		int ay = getAY();

		int sw = getStringWidth(taskName);

		if (!taskIcon.isEmpty())
		{
			sw += 11;
			Color4I.DARK_GRAY.draw(ax + (width - sw - 8) / 2, ay + 11, sw + 8, 14);
			taskIcon.draw(ax + (width - sw) / 2, ay + 14, 8, 8);
			drawString(taskName, ax + width / 2 + 6, ay + 14, Color4I.WHITE, CENTERED);
		}
		else
		{
			Color4I.DARK_GRAY.draw(ax + (width - sw - 8) / 2, ay + 11, sw + 8, 13);
			drawString(taskName, ax + width / 2, ay + 14, Color4I.WHITE, CENTERED);
		}

		int max = container.data.task.getMaxProgress();
		int progress = Math.min(max, container.data.task.getProgress(ClientQuestFile.INSTANCE));

		String s = max == 0 ? "0/0 [0%]" : String.format("%d/%d [%d%%]", progress, max, (int) (progress * 100D / (double) max));
		sw = getStringWidth(s);

		Color4I.DARK_GRAY.draw(ax + (width - sw - 8) / 2, ay + 60, sw + 8, 13);
		Color4I.LIGHT_BLUE.draw(ax + (width - sw - 6) / 2, ay + 61, max == 0 ? 0 : (sw + 6) * progress / max, 11);

		drawString(s, ax + width / 2, ay + 63, Color4I.WHITE, CENTERED);

		if (container.getNonPlayerSlots() <= 0)
		{
			pushFontUnicode(true);
			drawString(I18n.format("ftbquests.task.no_items"), ax + width / 2, ay + 37, Color4I.LIGHT_RED, CENTERED);
			popFontUnicode();
		}
	}

	@Override
	public void drawForeground()
	{
		super.drawForeground();

		int ax = getAX();
		int ay = getAY();
		int x = getMouseX() - getAX();
		int y = getMouseY() - getAY();

		for (int i = 0; i < container.getNonPlayerSlots(); i++)
		{
			Slot slot = container.inventorySlots.get(i);

			if (x >= slot.xPos && y >= slot.yPos && x < slot.xPos + 16 && y < slot.yPos + 16 && !slot.getHasStack())
			{
				container.getEmptySlotIcon(i).draw(ax + slot.xPos, ay + slot.yPos, 16, 16, Color4I.WHITE.withAlpha(70));
			}
		}
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		super.addMouseOverText(list);

		if (container.getNonPlayerSlots() > 0)
		{
			int x = getMouseX() - getAX();
			int y = getMouseY() - getAY();

			for (int i = 0; i < container.getNonPlayerSlots(); i++)
			{
				Slot slot = container.inventorySlots.get(i);

				if (x >= slot.xPos && y >= slot.yPos && x < slot.xPos + 16 && y < slot.yPos + 16 && !slot.getHasStack())
				{
					String text = container.getEmptySlotText(i);

					if (!text.isEmpty())
					{
						list.add(text);
					}

					return;
				}
			}
		}
	}
}