package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiContainerWrapper;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ImageIcon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.net.MessageCompleteInstantly;
import com.feed_the_beast.ftbquests.net.MessageResetProgress;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
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
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			GlStateManager.color(1F, 1F, 1F, 1F);
			TAB.draw(x, y, w, h);
			drawIcon(theme, x + 3, y + 2, 16, 16);
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
				add(new Tab(this, I18n.format("gui.back"), "", GuiIcons.LEFT, button -> onBack()));

				if (!container.data.task.quest.rewards.isEmpty())
				{
					add(new Tab(this, I18n.format("ftbquests.rewards") + ":", "", GuiIcons.MONEY_BAG, button -> {
						if (container.data.task.isComplete(container.data.teamData))
						{
							for (QuestReward reward : container.data.task.quest.rewards)
							{
								new MessageClaimReward(reward.uid).sendToServer();
							}
						}
					})
					{
						@Override
						public void addMouseOverText(List<String> list)
						{
							super.addMouseOverText(list);

							for (QuestReward reward : container.data.task.quest.rewards)
							{
								list.add(TextFormatting.GRAY + (ClientQuestFile.INSTANCE.isRewardClaimed(reward) ? TextFormatting.STRIKETHROUGH.toString() : "") + "- " + reward.stack.getCount() + "x " + TextFormatting.getTextWithoutFormattingCodes(reward.stack.getDisplayName()) + (reward.team ? (TextFormatting.BLUE + " [" + I18n.format("ftbquests.reward.team_reward") + "]") : ""));
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
						add(new Tab(this, I18n.format("ftbquests.gui.complete_instantly"), I18n.format("ftbquests.gui.complete_instantly_q"), QuestsTheme.COMPLETED, button -> {
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
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		GlStateManager.color(1F, 1F, 1F, 1F);
		BACKGROUND.draw(x, y, w, h);

		String top1 = container.data.task.quest.getDisplayName().getUnformattedText();
		String top2 = container.data.task.getDisplayName().getUnformattedText();

		if (top1.isEmpty() || top1.equals(top2))
		{
			top1 = top2;
			top2 = "";
		}

		top1 = TextFormatting.BOLD + top1;
		theme.drawString(top1, x + (w - theme.getStringWidth(top1)) / 2, y + 14);

		if (!top2.isEmpty())
		{
			top2 = TextFormatting.GRAY + top2;
			theme.drawString(top2, x + (w - theme.getStringWidth(top2)) / 2, y + 30);
		}

		container.data.task.drawGUI(container.data, x + (w - 64) / 2, y + 42, 64, 64);

		if (!container.data.task.hideProgressNumbers())
		{
			String bottomText = container.data.getProgressString() + " / " + container.data.task.getMaxProgressString();

			if (container.data.getProgress() >= container.data.task.getMaxProgress())
			{
				theme.drawString(TextFormatting.GREEN + bottomText, x + (w - theme.getStringWidth(bottomText)) / 2, y + 112);
			}
			else
			{
				theme.drawString(bottomText, x + (w - theme.getStringWidth(bottomText)) / 2, y + 112);
			}
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

	@Override
	public void onBack()
	{
		questFile.questGui = new GuiQuest(questFile.questTreeGui, container.data.task.quest);
		questFile.questGui.openGui();
	}
}