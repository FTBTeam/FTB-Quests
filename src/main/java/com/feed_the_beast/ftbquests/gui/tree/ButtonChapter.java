package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.FTBQuestsTheme;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonChapter extends ButtonTab
{
	public QuestChapter chapter;

	public ButtonChapter(Panel panel, QuestChapter c)
	{
		super(panel, GuiQuestTree.fixI18n(null, c.getDisplayName().getFormattedText()), c.getIcon());
		chapter = c;
	}

	@Override
	public void onClicked(MouseButton button)
	{
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		if (chapter == treeGui.selectedChapter || treeGui.selectedChapter != null && chapter == treeGui.selectedChapter.group)
		{
			treeGui.backgroundColor.draw(x + 1, y, w - 2, h);
		}

		if (treeGui.chapterHoverPanel.chapter == this)
		{
			return;
		}

		int is = width < 18 ? 8 : 16;
		icon.draw(x + (w - is) / 2, y + (h - is) / 2, is, is);

		if (chapter.quests.isEmpty() && !chapter.hasChildren())
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 450F);
			GuiIcons.CLOSE.draw(x + w - 10, y + 2, 8, 8);
			GlStateManager.popMatrix();
			return;
		}

		if (treeGui.file.self == null)
		{
			return;
		}

		if (chapter.hasUnclaimedRewards(Minecraft.getMinecraft().player.getUniqueID(), treeGui.file.self, true))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 450F);
			FTBQuestsTheme.ALERT.draw(x + w - 7, y + 2, 6, 6);
			GlStateManager.popMatrix();
		}
		else if (chapter.isComplete(treeGui.file.self))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 450F);
			FTBQuestsTheme.COMPLETED.draw(x + w - 8, y + 1, 8, 8);
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