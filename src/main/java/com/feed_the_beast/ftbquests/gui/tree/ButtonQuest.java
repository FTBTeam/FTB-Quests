package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestProgress;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
import com.feed_the_beast.ftbquests.net.edit.MessageEditDependency;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonQuest extends Button
{
	public GuiQuestTree treeGui;
	public Quest quest;
	public String description;
	public List<ButtonQuest> dependencies = null;

	public ButtonQuest(Panel panel, Quest q)
	{
		super(panel, q.getDisplayName().getFormattedText(), q.getIcon());
		treeGui = (GuiQuestTree) panel.getGui();
		setSize(20, 20);
		quest = q;
		description = TextFormatting.GRAY + quest.description;

		if (StringUtils.unformatted(description).isEmpty())
		{
			description = "";
		}
	}

	@Override
	public boolean checkMouseOver(int mouseX, int mouseY)
	{
		if (treeGui.questLeft.isMouseOver() || treeGui.questRight.isMouseOver())
		{
			return false;
		}

		return super.checkMouseOver(mouseX, mouseY);
	}

	public List<ButtonQuest> getDependencies()
	{
		if (dependencies == null)
		{
			dependencies = new ArrayList<>();

			for (QuestObject object : quest.getDependencies())
			{
				if (object instanceof Quest)
				{
					for (Widget widget : treeGui.quests.widgets)
					{
						if (widget instanceof ButtonQuest && object == ((ButtonQuest) widget).quest)
						{
							dependencies.add((ButtonQuest) widget);
						}
					}
				}
			}

			dependencies = dependencies.isEmpty() ? Collections.emptyList() : dependencies;
		}

		return dependencies;
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();

		if (treeGui.questFile.canEdit() && button.isRight())
		{
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.move"), GuiIcons.UP, () -> {
				treeGui.movingQuest = true;
				treeGui.selectQuest(quest);

			}));

			if (treeGui.selectedQuest != null && treeGui.selectedQuest != quest && !treeGui.selectedQuest.canRepeat)
			{
				if (quest.hasDependency(treeGui.selectedQuest))
				{
					contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.rem_dep"), GuiIcons.REMOVE, () -> new MessageEditDependency(quest.getID(), treeGui.selectedQuest.getID(), false).sendToServer()));
				}
				else
				{
					contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.set_dep"), QuestsTheme.ADD, () -> new MessageEditDependency(quest.getID(), treeGui.selectedQuest.getID(), true).sendToServer()).setEnabled(treeGui.selectedQuest != null && treeGui.selectedQuest != quest));
				}
			}

			contextMenu.add(ContextMenuItem.SEPARATOR);
			treeGui.addObjectMenuItems(contextMenu, getGui(), quest);
			getGui().openContextMenu(contextMenu);
		}
		else if (button.isLeft())
		{
			if (treeGui.movingQuest && treeGui.selectedQuest == quest)
			{
				treeGui.movingQuest = false;
				treeGui.selectedQuest = null;
				treeGui.selectQuest(quest);
			}
			else
			{
				treeGui.open(quest);
			}
		}
		else if (treeGui.questFile.canEdit() && button.isMiddle())
		{
			treeGui.movingQuest = true;
			treeGui.selectQuest(quest);
		}
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		list.add(getTitle() + ClientQuestProgress.getCompletionSuffix(treeGui.questFile.self, quest));

		if (!description.isEmpty())
		{
			list.add(description);
		}

		if (treeGui.questFile.self != null && quest.isComplete(treeGui.questFile.self))
		{
			int r = 0;

			for (QuestReward reward : quest.rewards)
			{
				if (!treeGui.questFile.isRewardClaimed(reward))
				{
					r++;
				}
			}

			if (r > 0 || quest.canRepeat)
			{
				list.add("");
			}

			if (r > 0)
			{
				list.add(I18n.format("ftbquests.gui.unclaimed_rewards") + ": " + TextFormatting.GOLD + r);
			}

			/*
			if (quest.canRepeat)
			{
				list.add(I18n.format("ftbquests.gui.times_completed") + ": " + TextFormatting.GOLD + treeGui.questFile.self.getTimesCompleted(quest));
			}
			*/
		}
	}

	@Override
	public WidgetType getWidgetType()
	{
		if (treeGui.selectedQuest == quest)
		{
			return WidgetType.MOUSE_OVER;
		}

		return treeGui.questFile.editingMode || quest.getVisibility(treeGui.questFile.self).isVisible() ? super.getWidgetType() : WidgetType.DISABLED;
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		Color4I col;
		int r = 0;

		if (treeGui.questFile.self == null || !quest.canStartTasks(treeGui.questFile.self))
		{
			col = treeGui.questFile.colCantStart;
		}
		else
		{
			int progress = quest.getRelativeProgress(treeGui.questFile.self);

			if (progress >= 100)
			{
				for (QuestReward reward : quest.rewards)
				{
					if (!treeGui.questFile.isRewardClaimed(reward))
					{
						r++;
					}
				}

				col = treeGui.questFile.colCompleted;
			}
			else if (progress > 0)
			{
				col = treeGui.questFile.colStarted;
			}
			else
			{
				col = treeGui.questFile.colNotStarted;
			}
		}

		int s = treeGui.zoom * 3 / 2;
		int sx = x + (w - s) / 2;
		int sy = y + (h - s) / 2;

		quest.shape.draw(sx, sy, s, s, col);

		if (isMouseOver())
		{
			quest.shape.draw(sx, sy, s, s);
		}

		if (treeGui.selectedQuest == quest)
		{
			quest.shape.draw(sx, sy, s, s);
		}

		if (!icon.isEmpty())
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate((int) (x + (w - treeGui.zoom) / 2F), (int) (y + (h - treeGui.zoom) / 2F), 0F);
			icon.draw(0, 0, treeGui.zoom, treeGui.zoom);
			GlStateManager.popMatrix();
		}

		if (r > 0)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 500);
			int s1 = treeGui.zoom / 2;
			int os1 = s1 / 4;
			QuestsTheme.ALERT.draw(x + w - s1 - os1, y + os1, s1, s1);
			GlStateManager.popMatrix();
		}
	}
}