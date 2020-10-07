package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiRewardNotifications;
import com.feed_the_beast.ftbquests.net.MessageClaimAllRewards;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author LatvianModder
 */
public class ButtonModpack extends ButtonTab
{
	private final boolean unclaimedRewards;

	public ButtonModpack(Panel panel)
	{
		super(panel, StringTextComponent.EMPTY, ClientQuestFile.INSTANCE.getIcon());
		title = treeGui.file.getTitle();
		unclaimedRewards = treeGui.file.self.hasUnclaimedRewards();
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();

		if (ClientQuestFile.exists())
		{
			new GuiRewardNotifications().openGui();
			new MessageClaimAllRewards().sendToServer();
		}
	}

	@Override
	public void addMouseOverText(TooltipList list)
	{
		super.addMouseOverText(list);

		if (unclaimedRewards)
		{
			list.blankLine();
			list.add(new TranslationTextComponent("ftbquests.gui.collect_rewards").mergeStyle(TextFormatting.GOLD));
		}
	}

	@Override
	public void draw(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
		super.draw(matrixStack, theme, x, y, w, h);

		if (unclaimedRewards)
		{
			float s = w / 2F;//(int) (treeGui.getZoom() / 2 * quest.size);
			matrixStack.push();
			matrixStack.translate(x + w - s, y, 500);
			matrixStack.scale(s, s, 1F);
			ThemeProperties.ALERT_ICON.get(treeGui.file).draw(matrixStack, 0, 0, 1, 1);
			matrixStack.pop();
		}
	}
}