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
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ImageIcon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.MessageGetScreen;
import com.feed_the_beast.ftbquests.net.edit.MessageCompleteInstantly;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.feed_the_beast.ftbquests.net.edit.MessageResetProgress;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class GuiTask extends GuiBase
{
	private static final ImageIcon TEXTURE = new ImageIcon(new ResourceLocation(FTBQuests.MOD_ID, "textures/gui/task.png"));
	private static final ImageIcon BACKGROUND = TEXTURE.withUVfromCoords(0, 0, 176, 214, 256, 256);
	private static final ImageIcon TAB = TEXTURE.withUVfromCoords(177, 0, 21, 20, 256, 256);

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

		@Override
		public void draw()
		{
			int x = getAX();
			int y = getAY();
			GlStateManager.color(1F, 1F, 1F, 1F);
			TAB.draw(x, y, width, height);
			getIcon().draw(x + 3, y + 2, 16, 16);
		}
	}

	public GuiTask(ContainerTask c)
	{
		setSize(176, 214);
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

				if (questFile.canEdit() || questFile.self != null && questFile.allowTakeQuestBlocks && container.data.task.quest.isVisible(questFile.self) && !container.data.task.isComplete(questFile.self))
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

				if (!container.data.task.quest.playerRewards.isEmpty() || !container.data.task.quest.teamRewards.isEmpty())
				{
					add(new Tab(this, I18n.format("ftbquests.rewards") + ":", "", GuiIcons.MONEY_BAG, button -> new GuiRewards().openGui())
					{
						@Override
						public void addMouseOverText(List<String> list)
						{
							super.addMouseOverText(list);

							for (ItemStack stack : container.data.task.quest.playerRewards)
							{
								list.add("- " + stack.getCount() + "x " + stack.getRarity().rarityColor + stack.getDisplayName());
							}

							for (ItemStack stack : container.data.task.quest.teamRewards)
							{
								list.add(TextFormatting.BLUE + "- " + stack.getCount() + "x " + stack.getRarity().rarityColor + stack.getDisplayName());
							}
						}
					});
				}

				if (questFile.canEdit())
				{
					add(new Tab(this, I18n.format("selectServer.edit"), "", GuiIcons.SETTINGS, button -> new MessageEditObject(container.data.task.getID()).sendToServer()));

					if (container.data.getProgress() > 0L)
					{
						add(new Tab(this, I18n.format("ftbquests.gui.reset_progress"), I18n.format("ftbquests.gui.reset_progress_q"), GuiIcons.REFRESH, button -> {
							new MessageResetProgress(container.data.task.getID()).sendToServer();
							container.data.resetProgress();
							tabs.refreshWidgets();
						}));
					}

					if (container.data.getProgress() < container.data.task.getMaxProgress())
					{
						add(new Tab(this, I18n.format("ftbquests.gui.complete_instantly"), I18n.format("ftbquests.gui.complete_instantly_q"), GuiIcons.CHECK, button -> {
							new MessageCompleteInstantly(container.data.task.getID()).sendToServer();
							container.data.completeInstantly();
							tabs.refreshWidgets();
						}));
					}
				}

				List<Tab> extra = new ArrayList<>();
				container.data.addTabs(extra);
				addAll(extra);
			}

			@Override
			public void alignWidgets()
			{
				setHeight(align(new WidgetLayout.Vertical(0, 1, 0)));
			}
		};

		tabs.setPosAndSize(-17, 8, 20, 0);
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
		return new GuiContainerWrapper(this, container).disableSlotDrawing();
	}

	@Override
	public void drawBackground()
	{
		int x = getAX();
		int y = getAY();

		GlStateManager.color(1F, 1F, 1F, 1F);
		BACKGROUND.draw(x, y, width, height);

		String top1 = container.data.task.quest.getDisplayName().getUnformattedText();
		String top2 = container.data.task.getDisplayName().getUnformattedText();

		if (top1.isEmpty() || top1.equals(top2))
		{
			top1 = top2;
			top2 = "";
		}

		top1 = TextFormatting.BOLD + top1;
		drawString(top1, x + (width - getStringWidth(top1)) / 2, y + 14);

		if (!top2.isEmpty())
		{
			top2 = TextFormatting.GRAY + top2;
			drawString(top2, x + (width - getStringWidth(top2)) / 2, y + 30);
		}

		container.data.task.drawGUI(container.data, x + (width - 64) / 2, y + 42, 64, 64);

		String bottomText = container.data.getProgressString() + " / " + container.data.task.getMaxProgressString();

		if (container.data.getProgress() >= container.data.task.getMaxProgress())
		{
			drawString(TextFormatting.GREEN + bottomText, x + (width - getStringWidth(bottomText)) / 2, y + 112);
		}
		else
		{
			drawString(bottomText, x + (width - getStringWidth(bottomText)) / 2, y + 112);
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