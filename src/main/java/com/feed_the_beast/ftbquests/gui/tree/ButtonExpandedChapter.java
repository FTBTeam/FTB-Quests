package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import com.feed_the_beast.ftbquests.net.MessageMoveChapter;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetType;
import com.mojang.blaze3d.platform.GlStateManager;
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
	public final GuiQuests treeGui;
	public final Chapter chapter;
	public List<String> description;

	public ButtonExpandedChapter(Panel panel, Chapter c)
	{
		super(panel, c.getTitle(), c.getIcon());
		treeGui = (GuiQuests) getGui();
		chapter = c;

		if (treeGui.file.self != null)
		{
			int p = treeGui.file.self.getRelativeProgress(c);

			if (p > 0 && p < 100)
			{
				setTitle(getTitle() + " " + TextFormatting.DARK_GREEN + p + "%");
			}
		}

		description = new ArrayList<>();

		for (String v : chapter.subtitle)
		{
			description.add(TextFormatting.GRAY + FTBQuestsClient.addI18nAndColors(v));
		}
	}

	@Override
	public boolean checkMouseOver(int mouseX, int mouseY)
	{
		if (treeGui.viewQuestPanel.isMouseOver())
		{
			return false;
		}

		return super.checkMouseOver(mouseX, mouseY);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		if (treeGui.file.canEdit() || !chapter.quests.isEmpty())
		{
			playClickSound();

			if (treeGui.selectedChapter != chapter)
			{
				treeGui.open(chapter, false);
			}
		}

		if (treeGui.file.canEdit() && button.isRight())
		{
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			contextMenu.add(new ContextMenuItem(I18n.format("gui.move"), ThemeProperties.MOVE_UP_ICON.get(), () -> new MessageMoveChapter(chapter.id, true).sendToServer()).setEnabled(() -> chapter.getIndex() > 0).setCloseMenu(false));
			contextMenu.add(new ContextMenuItem(I18n.format("gui.move"), ThemeProperties.MOVE_DOWN_ICON.get(), () -> new MessageMoveChapter(chapter.id, false).sendToServer()).setEnabled(() -> chapter.getIndex() < treeGui.file.chapters.size() - 1).setCloseMenu(false));
			contextMenu.add(ContextMenuItem.SEPARATOR);
			GuiQuests.addObjectMenuItems(contextMenu, treeGui, chapter);
			treeGui.openContextMenu(contextMenu);
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
			ThemeProperties.WIDGET_BACKGROUND.get().draw(x, y, w - 1, h);
		}

		if (parent.widgets.size() > 1)
		{
			if (parent.widgets.get(0) == this)
			{
				theme.drawButton(x, y, w, h, WidgetType.NORMAL);
			}
			else
			{
				Color4I borderColor = ThemeProperties.WIDGET_BORDER.get(treeGui.selectedChapter);
				//treeGui.borderColor.draw(x, y, 1, h);
				borderColor.draw(x + w - 1, y, 1, h);

				if (parent.widgets.get(parent.widgets.size() - 1) == this)
				{
					borderColor.draw(x, y + h - 1, w - 1, h);
				}
			}
		}
		else
		{
			GuiHelper.drawHollowRect(x, y, w, h, ThemeProperties.WIDGET_BORDER.get(), false);
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		super.draw(theme, x, y, w, h);

		int w2 = 20;

		if (treeGui.file.self.hasUnclaimedRewards(chapter))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0, 0, 450);
			ThemeProperties.ALERT_ICON.get().draw(x + w2 - 7, y + 3, 6, 6);
			GlStateManager.popMatrix();
		}
		else if (treeGui.file.self.isComplete(chapter))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0, 0, 450);
			ThemeProperties.CHECK_ICON.get().draw(x + w2 - 8, y + 2, 8, 8);
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