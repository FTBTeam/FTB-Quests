package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiContainerWrapper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleButton;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuestsConfig;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.block.QuestBlockData;
import com.feed_the_beast.ftbquests.net.MessageGetBlock;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiTaskBase extends GuiBase
{
	public final ContainerTaskBase container;
	public final boolean hasTile;
	public final Panel tabs;

	public static class Tab extends Button
	{
		public Tab(Panel panel, String title, Icon icon)
		{
			super(panel, title, icon);
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
				if (ClientQuestList.INSTANCE.editingMode)
				{
				}

				addTabs(this);
			}

			@Override
			public void alignWidgets()
			{
				align(WidgetLayout.VERTICAL);
			}
		};
	}

	public void addTabs(Panel panel)
	{
	}

	@Override
	public void addWidgets()
	{
		if (!hasTile && FTBQuestsConfig.general.allow_take_quest_blocks)
		{
			add(new SimpleButton(this, I18n.format("ftbquests.gui.task.get_block"), ItemIcon.getItemIcon(new ItemStack(FTBQuestsItems.QUEST_BLOCK)), (widget, button) -> {
				if (container.player.inventory.getItemStack().isEmpty() && container.data.task.quest.isVisible(ClientQuestList.INSTANCE) && !container.data.task.isComplete(ClientQuestList.INSTANCE))
				{
					ItemStack stack = new ItemStack(FTBQuestsItems.QUEST_BLOCK);
					QuestBlockData data = QuestBlockData.get(stack);
					data.setTask(container.data.task.id);
					data.setOwner(ClientQuestList.INSTANCE.teamId);
					container.player.inventory.setItemStack(stack);
					new MessageGetBlock(container.data.task.id).sendToServer();
				}
			}).setPosAndSize(8, 8, 20, 20));
		}
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

		String s = container.data.task.getDisplayName();
		int sw = getStringWidth(s);

		if (!container.data.task.getIcon().isEmpty())
		{
			sw += 11;
			Color4I.DARK_GRAY.draw(ax + (width - sw - 8) / 2, ay + 11, sw + 8, 14);
			container.data.task.getIcon().draw(ax + (width - sw) / 2, ay + 14, 8, 8);
			drawString(s, ax + width / 2 + 6, ay + 14, Color4I.WHITE, CENTERED);
		}
		else
		{
			Color4I.DARK_GRAY.draw(ax + (width - sw - 8) / 2, ay + 11, sw + 8, 13);
			drawString(s, ax + width / 2, ay + 14, Color4I.WHITE, CENTERED);
		}

		int max = container.data.task.getMaxProgress();
		int progress = Math.min(max, container.data.task.getProgress(ClientQuestList.INSTANCE));

		s = max == 0 ? "0/0 [0%]" : String.format("%d/%d [%d%%]", progress, max, (int) (progress * 100D / (double) max));
		sw = getStringWidth(s);

		Color4I.DARK_GRAY.draw(ax + (width - sw - 8) / 2, ay + 60, sw + 8, 13);
		Color4I.LIGHT_BLUE.draw(ax + (width - sw - 6) / 2, ay + 61, max == 0 ? 0 : (sw + 6) * progress / max, 11);

		drawString(s, ax + width / 2, ay + 63, Color4I.WHITE, CENTERED);

		if (container.getNonPlayerSlots() <= 0)
		{
			pushFontUnicode(true);
			drawString(I18n.format("ftbquests.gui.task.no_items"), ax + width / 2, ay + 37, Color4I.LIGHT_RED, CENTERED);
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