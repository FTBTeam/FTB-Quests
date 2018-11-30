package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;

import java.util.List;

/**
 * @author LatvianModder
 */
public class ToastQuestObject implements IToast
{
	public final QuestObject quest;
	public boolean hasPlayedSound;

	public ToastQuestObject(QuestObject q)
	{
		quest = q;
		hasPlayedSound = false;
	}

	@Override
	public Visibility draw(GuiToast gui, long delta)
	{
		Minecraft mc = gui.getMinecraft();
		mc.getTextureManager().bindTexture(TEXTURE_TOASTS);
		GlStateManager.color(1F, 1F, 1F);
		gui.drawTexturedModalRect(0, 0, 0, 0, 160, 32);

		List<String> list = mc.fontRenderer.listFormattedStringToWidth(quest.getDisplayName().getFormattedText(), 125);
		int i = quest instanceof QuestChapter ? 16746751 : 16776960;

		if (list.size() == 1)
		{
			mc.fontRenderer.drawString(I18n.format(quest.getObjectType().getTranslationKey() + ".completed"), 30, 7, i | -16777216);
			mc.fontRenderer.drawString(quest.getDisplayName().getFormattedText(), 30, 18, -1);
		}
		else
		{
			if (delta < 1500L)
			{
				int k = MathHelper.floor(MathHelper.clamp((float) (1500L - delta) / 300F, 0F, 1F) * 255F) << 24 | 67108864;
				mc.fontRenderer.drawString(I18n.format(quest.getObjectType().getTranslationKey() + ".completed"), 30, 11, i | k);
			}
			else
			{
				int i1 = MathHelper.floor(MathHelper.clamp((float) (delta - 1500L) / 300F, 0F, 1F) * 252F) << 24 | 67108864;
				int l = 16 - list.size() * mc.fontRenderer.FONT_HEIGHT / 2;

				for (String s : list)
				{
					mc.fontRenderer.drawString(s, 30, l, 16777215 | i1);
					l += mc.fontRenderer.FONT_HEIGHT;
				}
			}
		}

		if (!hasPlayedSound && delta > 0L)
		{
			hasPlayedSound = true;

			if (quest instanceof QuestChapter)
			{
				mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1F));
			}
		}

		RenderHelper.enableGUIStandardItemLighting();
		quest.getIcon().draw(8, 8, 16, 16);
		return delta >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
	}
}