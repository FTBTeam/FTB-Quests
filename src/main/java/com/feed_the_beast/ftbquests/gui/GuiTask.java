package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiContainerWrapper;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.net.MessageGetScreen;
import com.feed_the_beast.ftbquests.net.edit.MessageResetProgress;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class GuiTask extends GuiBase
{
	public final ContainerTask container;
	public final ClientQuestFile questFile;
	public final boolean hasTile;
	public final Panel tabs;
	public final String taskName;
	public final Icon taskIcon;

	public static class Tab extends Button
	{
		public String yesNoText;
		public final Consumer<MouseButton> callback;

		public Tab(Panel panel, String title, String yn, Icon icon, Consumer<MouseButton> c)
		{
			super(panel, title, icon);
			setSize(20, 20);
			yesNoText = yn;
			callback = c;
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();

			if (yesNoText.isEmpty())
			{
				callback.accept(button);
			}
			else
			{
				getGui().openYesNo(yesNoText, "", () -> callback.accept(button));
			}
		}
	}

	public GuiTask(ContainerTask c)
	{
		container = c;
		questFile = ClientQuestFile.INSTANCE;
		hasTile = container.screen != null && !container.screen.isInvalid();

		tabs = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				add(new Tab(this, I18n.format("gui.back"), "", GuiIcons.LEFT, button ->
				{
					questFile.questGui = new GuiQuest(questFile.questTreeGui, container.data.task.quest);
					questFile.questGui.openGui();
				}));

				if (questFile.canEdit() || questFile.self != null && questFile.allowTakeQuestBlocks.getBoolean() && container.data.task.quest.isVisible(questFile.self) && !container.data.task.isComplete(questFile.self))
				{
					add(new Tab(this, I18n.format("tile.ftbquests.screen.name"), "", ItemIcon.getItemIcon(new ItemStack(FTBQuestsItems.SCREEN)), button ->
					{
						if (questFile.canEdit() && button.isRight())
						{
							List<ContextMenuItem> contextMenu = new ArrayList<>();
							contextMenu.add(new ContextMenuItem("Screen", Icon.EMPTY, () -> {}).setEnabled(false));
							contextMenu.add(new ContextMenuItem("1 x 1", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.getID(), 0).sendToServer()));
							contextMenu.add(new ContextMenuItem("3 x 3", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.getID(), 1).sendToServer()));
							contextMenu.add(new ContextMenuItem("5 x 5", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.getID(), 2).sendToServer()));
							contextMenu.add(new ContextMenuItem("7 x 7", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.getID(), 3).sendToServer()));
							contextMenu.add(new ContextMenuItem("9 x 9", Icon.EMPTY, () -> new MessageGetScreen(container.data.task.getID(), 4).sendToServer()));
							getGui().openContextMenu(contextMenu);
						}
						else
						{
							new MessageGetScreen(container.data.task.getID(), 0).sendToServer();
						}
					}));
				}

				if (questFile.canEdit())
				{
					add(new Tab(this, I18n.format("ftbquests.gui.reset_progress"), I18n.format("ftbquests.gui.reset_progress_q"), GuiIcons.REFRESH, button -> new MessageResetProgress(container.data.task.getID(), button.isRight()).sendToServer()));
				}
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

		double progress = MathHelper.clamp(container.data.getRelativeProgress(), 0D, 1D);

		String s = String.format("%s / %s [%d%%]", container.data.getProgressString(), container.data.task.getMaxProgressString(), (int) (progress * 100D));
		sw = getStringWidth(s);

		Color4I.DARK_GRAY.draw(ax + (width - sw - 8) / 2, ay + 60, sw + 8, 13);
		Color4I.LIGHT_BLUE.draw(ax + (width - sw - 6) / 2, ay + 61, (int) ((sw + 6) * progress), 11);

		drawString(s, ax + width / 2, ay + 63, Color4I.WHITE, CENTERED);

		if (!container.data.task.canInsertItem())
		{
			pushFontUnicode(true);
			drawString(I18n.format("ftbquests.task.no_items"), ax + width / 2, ay + 37, Color4I.LIGHT_RED, CENTERED);
			popFontUnicode();
		}
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
	public Theme getTheme()
	{
		return QuestsTheme.INSTANCE;
	}
}