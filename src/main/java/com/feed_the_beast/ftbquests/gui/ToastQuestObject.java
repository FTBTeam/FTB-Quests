package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.SimpleToast;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
	public IFormattableTextComponent getTitle()
	{
		return new TranslationTextComponent(object.getObjectType().translationKey + ".completed");
	}

	@Override
	public IFormattableTextComponent getSubtitle()
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
			handler.play(SimpleSound.master(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1F));
		}
	}
}