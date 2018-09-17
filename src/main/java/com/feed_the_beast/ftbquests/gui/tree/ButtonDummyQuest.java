package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiSelectItemStack;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.net.edit.MessageMoveQuest;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.tasks.ItemTask;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
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

			if (isCtrlKeyDown())
			{
				new GuiSelectItemStack(new ConfigValueInstance("item", ConfigGroup.DEFAULT, new ConfigItemStack(ItemStack.EMPTY)
				{
					@Override
					public void setStack(ItemStack stack)
					{
						NBTTagCompound nbt = new NBTTagCompound();
						Quest quest = new Quest(treeGui.selectedChapter, nbt);
						quest.x = x;
						quest.y = y;
						ItemTask itemTask = new ItemTask(quest, new NBTTagCompound());
						itemTask.items.add(stack);
						itemTask.count = 1L;
						itemTask.id = StringUtils.toSnakeCase(stack.getDisplayName());
						quest.tasks.add(itemTask);
						quest.writeData(nbt);
						nbt.setString("id", itemTask.id);
						new MessageCreateObject(QuestObjectType.QUEST, treeGui.selectedChapter.getID(), nbt).sendToServer();
					}
				}), this).openGui();
				return true;
			}

			new GuiEditConfigValue("title", new ConfigString(""), (value, set) ->
			{
				treeGui.openGui();

				if (set)
				{
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setByte("x", x);
					nbt.setByte("y", y);
					nbt.setString("title", value.getString());
					nbt.setString("id", StringUtils.toSnakeCase(value.getString()));
					new MessageCreateObject(QuestObjectType.QUEST, treeGui.selectedChapter.getID(), nbt).sendToServer();
				}
			}).openGui();

			getGui().openContextMenu(contextMenu);
			return true;
		}
		else if (button.isLeft() && treeGui.movingQuest && treeGui.selectedQuest != null && treeGui.questFile.canEdit())
		{
			GuiHelper.playClickSound();
			new MessageMoveQuest(treeGui.selectedQuest.getID(), x, y).sendToServer();
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
		if (treeGui.movingQuest && treeGui.selectedQuest != null)
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

		int s = treeGui.zoom * 3 / 2;
		int sx = x + (w - s) / 2;
		int sy = y + (h - s) / 2;

		if (treeGui.selectedQuest != null && treeGui.movingQuest)
		{
			treeGui.selectedQuest.shape.draw(sx, sy, s, s, Color4I.WHITE.withAlpha(30));
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