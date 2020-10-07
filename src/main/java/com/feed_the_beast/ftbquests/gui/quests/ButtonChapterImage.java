package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.ChapterImage;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonChapterImage extends Button
{
	public GuiQuests treeGui;
	public ChapterImage chapterImage;

	public ButtonChapterImage(Panel panel, ChapterImage i)
	{
		super(panel, StringTextComponent.EMPTY, i.image);
		treeGui = (GuiQuests) panel.getGui();
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

		if (chapterImage.click.isEmpty() && !treeGui.file.canEdit())
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

			contextMenu.add(new ContextMenuItem(new TranslationTextComponent("selectServer.edit"), ThemeProperties.EDIT_ICON.get(), () -> {
				ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
				chapterImage.getConfig(group.getGroup("chapter").getGroup("image"));
				group.savedCallback = accepted -> {
					if (accepted)
					{
						new MessageEditObject(chapterImage.chapter).sendToServer();
					}
					run();
				};
				new GuiEditConfig(group).openGui();
			}));

			contextMenu.add(new ContextMenuItem(new TranslationTextComponent("gui.move"), ThemeProperties.MOVE_UP_ICON.get(chapterImage.chapter), () -> {
				treeGui.movingObjects = true;
				treeGui.selectedObjects.clear();
				treeGui.toggleSelected(chapterImage);
			})
			{
				@Override
				public void addMouseOverText(TooltipList list)
				{
					list.add(new TranslationTextComponent("ftbquests.gui.move_tooltip").mergeStyle(TextFormatting.DARK_GRAY));
				}
			});

			contextMenu.add(new ContextMenuItem(new TranslationTextComponent("selectServer.delete"), ThemeProperties.DELETE_ICON.get(), () -> {
				chapterImage.chapter.images.remove(chapterImage);
				new MessageEditObject(chapterImage.chapter).sendToServer();
			}).setYesNo(new TranslationTextComponent("delete_item", chapterImage.image.toString())));

			getGui().openContextMenu(contextMenu);
		}
		else if (button.isLeft())
		{
			if (!chapterImage.click.isEmpty())
			{
				playClickSound();
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
	public void addMouseOverText(TooltipList list)
	{
		for (String s : chapterImage.hover)
		{
			if (s.startsWith("{") && s.endsWith("}"))
			{
				list.add(new TranslationTextComponent(s.substring(1, s.length() - 1)));
			}
			else
			{
				list.add(new StringTextComponent(s));
			}
		}
	}

	@Override
	public void draw(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
		matrixStack.push();
		matrixStack.translate((int) (x + w / 2D), (int) (y + h / 2D), 10);
		matrixStack.rotate(Vector3f.ZP.rotationDegrees((float) chapterImage.rotation));
		matrixStack.scale(w / 2F, h / 2F, 1);
		chapterImage.image.draw(matrixStack, -1, -1, 2, 2);
		matrixStack.pop();
	}
}