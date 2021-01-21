package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.SimpleToast;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;

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
	public MutableComponent getTitle()
	{
		return new TranslatableComponent(object.getObjectType().translationKey + ".completed");
	}

	@Override
	public MutableComponent getSubtitle()
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
	public void playSound(SoundManager handler)
	{
		if (object instanceof Chapter)
		{
			handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1F));
		}
	}
}