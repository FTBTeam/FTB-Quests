package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateTaskAt;
import com.feed_the_beast.ftbquests.net.edit.MessageMoveQuest;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonDummyQuest extends Widget
{
	public GuiQuestTree treeGui;
	public final byte x, y;

	public ButtonDummyQuest(Panel panel, byte _x, byte _y)
	{
		super(panel);
		treeGui = (GuiQuestTree) panel.getGui();
		setSize(20, 20);
		x = _x;
		y = _y;
	}

	@Override
	public boolean checkMouseOver(int mouseX, int mouseY)
	{
		if (treeGui.questLeft.isMouseOver() || treeGui.questRight.isMouseOver() || treeGui.patreon.isMouseOver())
		{
			return false;
		}

		return super.checkMouseOver(mouseX, mouseY);
	}

	@Override
	public boolean mousePressed(MouseButton button)
	{
		if (!isMouseOver())
		{
			return false;
		}

		if (button.isRight() && treeGui.file.canEdit())
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			for (QuestTaskType type : QuestTaskType.getRegistry())
			{
				contextMenu.add(new ContextMenuItem(type.getDisplayName().getFormattedText(), type.getIcon(), () -> {
					GuiHelper.playClickSound();
					type.getGuiProvider().openCreationGui(this, new Quest(treeGui.selectedChapter), task -> new MessageCreateTaskAt(treeGui.selectedChapter, x, y, task).sendToServer());
				}));
			}

			getGui().openContextMenu(contextMenu);
			return true;
		}
		else if (button.isLeft() && treeGui.movingQuest && treeGui.getSelectedQuest() != null && treeGui.file.canEdit())
		{
			GuiHelper.playClickSound();
			new MessageMoveQuest(treeGui.getSelectedQuest().id, x, y).sendToServer();
			treeGui.movingQuest = false;
			treeGui.selectQuest(null);
			return true;
		}

		treeGui.selectQuest(null);
		return false;
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		if (treeGui.movingQuest && treeGui.getSelectedQuest() != null)
		{
			list.add(I18n.format("gui.move"));
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		if (!treeGui.file.canEdit())
		{
			return;
		}

		GlStateManager.color(1F, 1F, 1F, 1F);

		double s = treeGui.zoomd * 3D / 2D;
		double sx = x + (w - s) / 2D;
		double sy = y + (h - s) / 2D;

		if (treeGui.getSelectedQuest() != null && treeGui.movingQuest)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(sx, sy, 0);
			GlStateManager.scale(s, s, 1D);
			treeGui.getSelectedQuest().shape.shape.draw(0, 0, 1, 1, Color4I.WHITE.withAlpha(20));
			GlStateManager.popMatrix();
		}

		if (isMouseOver())
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(sx, sy, 0);
			GlStateManager.pushMatrix();
			GlStateManager.scale(s, s, 1D);
			Color4I.WHITE.withAlpha(30).draw(0, 0, 1, 1);
			GlStateManager.popMatrix();
			GlStateManager.scale(treeGui.zoomd / 24D, treeGui.zoomd / 24D, 1D);
			theme.drawString("X" + this.x, 2, 2);
			theme.drawString("Y" + this.y, 2, 12);
			GlStateManager.popMatrix();
		}
	}
}