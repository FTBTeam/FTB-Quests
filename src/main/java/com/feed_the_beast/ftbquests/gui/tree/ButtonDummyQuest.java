package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.net.edit.MessageMoveQuest;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

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

		if (button.isRight() && treeGui.questFile.canEdit())
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			for (QuestTaskType type : QuestTaskType.getRegistry())
			{
				contextMenu.add(new ContextMenuItem(type.getDisplayName().getFormattedText(), type.getIcon(), () -> {
					GuiHelper.playClickSound();
					Quest quest = new Quest(treeGui.selectedChapter);
					quest.x = x;
					quest.y = y;
					new MessageCreateObject(quest, null).sendToServer();
					type.getGuiProvider().openCreationGui(this, quest, task -> {
						Quest q = treeGui.selectedChapter.getQuestAt(x, y);
						QuestTask t = type.provider.create(q);
						NBTTagCompound nbt = new NBTTagCompound();
						task.writeData(nbt);
						t.readData(nbt);
						NBTTagCompound extra = new NBTTagCompound();
						extra.setString("type", type.getTypeForNBT());
						new MessageCreateObject(t, extra).sendToServer();
					});
				}));
			}

			getGui().openContextMenu(contextMenu);
			return true;
		}
		else if (button.isLeft() && treeGui.movingQuest && treeGui.getSelectedQuest() != null && treeGui.questFile.canEdit())
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
			list.add(I18n.format("ftbquests.gui.move"));
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		if (!treeGui.questFile.canEdit())
		{
			return;
		}

		GlStateManager.color(1F, 1F, 1F, 1F);

		int s = treeGui.zoom * 3 / 2;
		int sx = x + (w - s) / 2;
		int sy = y + (h - s) / 2;

		if (treeGui.getSelectedQuest() != null && treeGui.movingQuest)
		{
			treeGui.getSelectedQuest().shape.shape.draw(sx, sy, s, s, Color4I.WHITE.withAlpha(20));
		}

		if (isMouseOver())
		{
			Color4I.WHITE.withAlpha(30).draw(sx, sy, s, s);

			GlStateManager.pushMatrix();
			GlStateManager.translate(sx, sy, 0);
			GlStateManager.scale(treeGui.zoom / 24D, treeGui.zoom / 24D, 1D);
			theme.drawString("X" + this.x, 2, 2);
			theme.drawString("Y" + this.y, 2, 12);
			GlStateManager.popMatrix();
		}
	}
}