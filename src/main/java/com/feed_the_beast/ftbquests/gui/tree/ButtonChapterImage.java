package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfig;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.ChapterImage;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonChapterImage extends Button
{
	public GuiQuestTree treeGui;
	public ChapterImage chapterImage;

	public ButtonChapterImage(Panel panel, ChapterImage i)
	{
		super(panel, "", i.image);
		treeGui = (GuiQuestTree) panel.getGui();
		setSize(20, 20);
		chapterImage = i;
	}

	@Override
	public boolean checkMouseOver(int mouseX, int mouseY)
	{
		if (treeGui.questPanel.mouseOverQuest != null || treeGui.movingObjects || treeGui.viewQuestPanel.isMouseOver() || treeGui.chapterHoverPanel.isMouseOverAnyWidget())
		{
			return false;
		}

		return super.checkMouseOver(mouseX, mouseY);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		if (treeGui.file.canEdit() && button.isRight())
		{
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			contextMenu.add(new ContextMenuItem(I18n.format("selectServer.edit"), ThemeProperties.EDIT_ICON.get(), () -> {
				ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
				chapterImage.getConfig(group.getGroup("chapter").getGroup("image"));
				new GuiEditConfig(group, (group1, sender) -> new MessageEditObject(chapterImage.chapter).sendToServer()).openGui();
			}));

			contextMenu.add(new ContextMenuItem(I18n.format("gui.move"), ThemeProperties.MOVE_UP_ICON.get(chapterImage.chapter), () -> {
				treeGui.movingObjects = true;
				treeGui.selectedObjects.clear();
				treeGui.toggleSelected(chapterImage);
			})
			{
				@Override
				public void addMouseOverText(List<String> list)
				{
					list.add(TextFormatting.DARK_GRAY + I18n.format("ftbquests.gui.move_tooltip"));
				}
			});

			contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), ThemeProperties.DELETE_ICON.get(), () -> {
				chapterImage.chapter.images.remove(chapterImage);
				new MessageEditObject(chapterImage.chapter).sendToServer();
			}).setYesNo(I18n.format("delete_item", chapterImage.image.toString())));

			getGui().openContextMenu(contextMenu);
		}
		else if (button.isLeft())
		{
			if (!chapterImage.click.isEmpty())
			{
				GuiHelper.playClickSound();
				handleClick(chapterImage.click);
			}
		}
		else if (treeGui.file.canEdit() && button.isMiddle())
		{
			if (!treeGui.selectedObjects.contains(chapterImage))
			{
				treeGui.toggleSelected(chapterImage);
			}

			treeGui.movingObjects = true;
		}
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		for (String s : chapterImage.hover)
		{
			if (s.startsWith("{") && s.endsWith("}"))
			{
				list.add(I18n.format(s.substring(1, s.length() - 1)));
			}
			else
			{
				list.add(s);
			}
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate((int) (x + w / 2D), (int) (y + h / 2D), 10);
		GlStateManager.rotate((float) chapterImage.rotation, 0, 0, 1);
		GlStateManager.scale(w / 2D, h / 2D, 1);
		chapterImage.image.draw(-1, -1, 2, 2);
		GlStateManager.popMatrix();
	}
}