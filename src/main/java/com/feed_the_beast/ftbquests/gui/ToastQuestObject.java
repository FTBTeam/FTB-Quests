package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.misc.SimpleToast;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;

/**
 * @author LatvianModder
 */
public class ToastQuestObject extends SimpleToast
{
	private final QuestObject object;

	public ToastQuestObject(QuestObject q)
	{
		object = q;
	}

	@Override
	public String getTitle()
	{
		return I18n.format(object.getObjectType().getTranslationKey() + ".completed");
	}

	@Override
	public String getSubtitle()
	{
		return object.getTitle();
	}

	@Override
	public boolean isImportant()
	{
		return object instanceof Chapter;
	}

	@Override
	public Icon getIcon()
	{
		return object.getIcon();
	}

	@Override
	public void playSound(SoundHandler handler)
	{
		if (object instanceof Chapter)
		{
			handler.playSound(PositionedSoundRecord.getRecord(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1F));
		}
	}
}