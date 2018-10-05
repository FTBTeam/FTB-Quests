package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.net.edit.MessageDeleteReward;
import com.feed_the_beast.ftbquests.net.edit.MessageEditReward;
import com.feed_the_beast.ftbquests.net.edit.MessageResetReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonReward extends SimpleTextButton
{
	public final QuestReward reward;

	public ButtonReward(Panel panel, QuestReward r)
	{
		super(panel, (r.team ? StringUtils.color(r.getDisplayName(), TextFormatting.BLUE) : r.getDisplayName()).getFormattedText(), r.getIcon());
		reward = r;
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		if (isShiftKeyDown() && isCtrlKeyDown())
		{
			list.add(TextFormatting.DARK_GRAY + reward.toString());
		}

		if (reward.team)
		{
			list.add(TextFormatting.GRAY + I18n.format("ftbquests.reward.team_reward"));
		}

		reward.addMouseOverText(list);
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
	public WidgetType getWidgetType()
	{
		if (!ClientQuestFile.existsWithTeam() || !reward.quest.isComplete(ClientQuestFile.INSTANCE.self))
		{
			return WidgetType.DISABLED;
		}

		return super.getWidgetType();
	}

	@Override
	public void onClicked(MouseButton button)
	{
		if (button.isLeft())
		{
			if (ClientQuestFile.existsWithTeam())
			{
				GuiHelper.playClickSound();
				new MessageClaimReward(reward.uid).sendToServer();
			}
		}
		else if (button.isRight() && ClientQuestFile.exists() && ClientQuestFile.INSTANCE.canEdit())
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			
			/*
			QuestRewardType type = QuestRewardType.getType(reward.getClass());

			ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
			ConfigGroup g = group.getGroup("reward").getGroup(type.getRegistryName().getNamespace()).getGroup(type.getRegistryName().getPath());

			reward.getConfig(g);
			reward.getExtraConfig(g);

			if (!g.getValues().isEmpty())
			{
				List<ContextMenuItem> list = new ArrayList<>();

				for (ConfigValueInstance inst : g.getValues())
				{
					if (inst.getValue() instanceof IIteratingConfig)
					{
						list.add(new ContextMenuItem(inst.getDisplayName().getFormattedText(), inst.getIcon(), null)
						{
							@Override
							public void addMouseOverText(List<String> list)
							{
								list.add(inst.getValue().getStringForGUI().getFormattedText());
							}

							@Override
							public void onClicked(Panel panel, MouseButton button)
							{
								//FIXME: new MessageEditObjectQuick(object.getID(), inst.getName(), button.isLeft()).sendToServer();
							}
						});
					}
				}

				if (!list.isEmpty())
				{
					list.sort(null);
					contextMenu.addAll(list);
					contextMenu.add(ContextMenuItem.SEPARATOR);
				}
			}
			*/

			contextMenu.add(new ContextMenuItem(I18n.format("selectServer.edit"), GuiIcons.SETTINGS, () -> new MessageEditReward(reward.uid).sendToServer()));
			contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> new MessageDeleteReward(reward.uid).sendToServer()).setYesNo(I18n.format("delete_item", reward.getDisplayName().getFormattedText())));
			contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.reset_progress"), GuiIcons.REFRESH, () -> new MessageResetReward(reward.uid).sendToServer()).setYesNo(I18n.format("ftbquests.gui.reset_progress_q")));

			getGui().openContextMenu(contextMenu);
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		super.draw(theme, x, y, w, h);

		if (!ClientQuestFile.existsWithTeam())
		{
			GuiIcons.CLOSE.draw(x + w - 9, y + 1, 8, 8);
		}
		else if (ClientQuestFile.INSTANCE.isRewardClaimed(reward))
		{
			QuestsTheme.COMPLETED.draw(x + w - 9, y + 1, 8, 8);
		}
		else if (reward.quest.isComplete(ClientQuestFile.INSTANCE.self))
		{
			QuestsTheme.ALERT.draw(x + w - 9, y + 1, 8, 8);
		}
	}
}