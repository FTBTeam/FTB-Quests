package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.FTBQuestsTheme;
import com.feed_the_beast.ftbquests.net.edit.MessageMoveChapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonChapter extends ButtonTab
{
	public int index;
	public QuestChapter chapter;
	public List<String> description;

	public ButtonChapter(Panel panel, int idx, QuestChapter c)
	{
		super(panel, c.getDisplayName().getFormattedText(), c.getIcon());
		index = idx;
		chapter = c;
		description = new ArrayList<>();

		for (String v : chapter.description)
		{
			description.add(TextFormatting.GRAY + v);
		}
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		treeGui.selectChapter(chapter);

		if (treeGui.questFile.canEdit() && button.isRight())
		{
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.move"), GuiIcons.LEFT, () -> new MessageMoveChapter(chapter.id, true).sendToServer()).setEnabled(() -> chapter.getIndex() > 0).setCloseMenu(false));
			contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.move"), GuiIcons.RIGHT, () -> new MessageMoveChapter(chapter.id, false).sendToServer()).setEnabled(() -> chapter.getIndex() < treeGui.questFile.chapters.size() - 1).setCloseMenu(false));
			contextMenu.add(ContextMenuItem.SEPARATOR);
			GuiQuestTree.addObjectMenuItems(contextMenu, getGui(), chapter);
			getGui().openContextMenu(contextMenu);
		}
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		String title = getTitle();

		if (treeGui.questFile.self != null)
		{
			int p = chapter.getRelativeProgress(treeGui.questFile.self);

			if (p > 0 && p < 100)
			{
				title += " " + TextFormatting.DARK_GRAY + p + "%";
			}
		}

		list.add(title);
		list.addAll(description);

		if (treeGui.questFile.self == null)
		{
			return;
		}

		int r = 0;

		for (Quest quest : chapter.quests)
		{
			if (quest.isComplete(treeGui.questFile.self))
			{
				for (QuestReward reward : quest.rewards)
				{
					if (!treeGui.questFile.isRewardClaimed(reward))
					{
						r++;
					}
				}
			}
		}

		if (r > 0)
		{
			list.add("");
			list.add(I18n.format("ftbquests.gui.unclaimed_rewards") + ": " + TextFormatting.GOLD + r);
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		if (treeGui.selectedChapter != chapter)
		{
			treeGui.borderColor.draw(x, y + h - 1, w + 1, 1);
			treeGui.backgroundColor.draw(x, y + 1, w, h - 2);
		}
		else
		{
			treeGui.borderColor.draw(x + w, y + h - 1, 1, 1);
		}

		treeGui.borderColor.draw(x + w, y + 1, 1, h - 2);
		icon.draw(x + (w - 16) / 2, y + (h - 16) / 2, 16, 16);

		if (isMouseOver())
		{
			treeGui.backgroundColor.draw(x, y + 1, w, h - (treeGui.selectedChapter == chapter ? 1 : 2));
		}

		if (chapter.quests.isEmpty())
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 500);
			GuiIcons.CLOSE.draw(x + w - 10, y + 2, 8, 8);
			GlStateManager.popMatrix();
			return;
		}

		if (treeGui.questFile.self == null)
		{
			return;
		}

		boolean hasRewards = false;

		for (Quest quest : chapter.quests)
		{
			if (quest.isComplete(treeGui.questFile.self))
			{
				for (QuestReward reward : quest.rewards)
				{
					if (!treeGui.questFile.isRewardClaimed(reward))
					{
						hasRewards = true;
						break;
					}
				}

				if (hasRewards)
				{
					break;
				}
			}
		}

		if (hasRewards)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 500);
			FTBQuestsTheme.ALERT.draw(x + w - 7, y + 2, 6, 6);
			GlStateManager.popMatrix();
		}
		else if (chapter.isComplete(treeGui.questFile.self))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 500);
			FTBQuestsTheme.COMPLETED.draw(x + w - 8, y + 1, 8, 8);
			GlStateManager.popMatrix();
		}
	}
}