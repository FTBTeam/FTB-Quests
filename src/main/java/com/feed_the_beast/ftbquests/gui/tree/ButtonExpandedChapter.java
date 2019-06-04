package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.FTBQuestsTheme;
import com.feed_the_beast.ftbquests.net.edit.MessageMoveChapter;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonExpandedChapter extends SimpleTextButton
{
	public final GuiQuestTree treeGui;
	public final QuestChapter chapter;
	public List<String> description;

	public ButtonExpandedChapter(Panel panel, QuestChapter c)
	{
		super(panel, c.getDisplayName().getFormattedText(), c.getIcon());
		treeGui = (GuiQuestTree) getGui();
		chapter = c;

		if (treeGui.file.self != null)
		{
			int p = c.getRelativeProgress(treeGui.file.self);

			if (p > 0 && p < 100)
			{
				setTitle(getTitle() + " " + TextFormatting.DARK_GREEN + p + "%");
			}
		}

		description = new ArrayList<>();

		for (String v : chapter.description)
		{
			description.add(GuiQuestTree.fixI18n(TextFormatting.GRAY, v));
		}
	}

	@Override
	public void onClicked(MouseButton button)
	{
		if (treeGui.file.canEdit() || !chapter.quests.isEmpty())
		{
			GuiHelper.playClickSound();

			if (treeGui.selectedChapter != chapter)
			{
				treeGui.open(chapter);
			}
		}

		if (treeGui.file.canEdit() && button.isRight())
		{
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			contextMenu.add(new ContextMenuItem(I18n.format("gui.move"), GuiIcons.UP, () -> new MessageMoveChapter(chapter.id, true).sendToServer()).setEnabled(() -> chapter.getIndex() > 0).setCloseMenu(false));
			contextMenu.add(new ContextMenuItem(I18n.format("gui.move"), GuiIcons.DOWN, () -> new MessageMoveChapter(chapter.id, false).sendToServer()).setEnabled(() -> chapter.getIndex() < treeGui.file.chapters.size() - 1).setCloseMenu(false));
			contextMenu.add(ContextMenuItem.SEPARATOR);
			GuiQuestTree.addObjectMenuItems(contextMenu, getGui(), chapter);
			getGui().openContextMenu(contextMenu);
		}
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		list.addAll(description);
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		theme.drawGui(x, y, w, h, WidgetType.NORMAL);

		if (isMouseOver())
		{
			theme.drawButton(x, y, w, h, WidgetType.MOUSE_OVER);
		}
		else if (parent.widgets.size() > 1)
		{
			if (parent.widgets.get(0) == this)
			{
				theme.drawButton(x, y, w, h, WidgetType.NORMAL);
			}
			else
			{
				//treeGui.borderColor.draw(x, y, 1, h);
				treeGui.borderColor.draw(x + w - 1, y, 1, h);

				if (parent.widgets.get(parent.widgets.size() - 1) == this)
				{
					treeGui.borderColor.draw(x, y + h - 1, w - 1, h);
				}
			}
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		super.draw(theme, x, y, w, h);

		int w2 = 20;

		if (chapter.hasUnclaimedRewards(Minecraft.getMinecraft().player.getUniqueID(), treeGui.file.self, true))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 450F);
			FTBQuestsTheme.ALERT.draw(x + w2 - 7, y + 3, 6, 6);
			GlStateManager.popMatrix();
		}
		else if (chapter.isComplete(treeGui.file.self))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 450F);
			FTBQuestsTheme.COMPLETED.draw(x + w2 - 8, y + 2, 8, 8);
			GlStateManager.popMatrix();
		}
	}

	@Nullable
	@Override
	public Object getIngredientUnderMouse()
	{
		return icon.getIngredient();
	}
}