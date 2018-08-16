package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftbquests.FTBQuestsCommon;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class FTBQuestsClient extends FTBQuestsCommon
{
	public static final KeyBinding KEY_QUESTS = new KeyBinding("key.ftbquests.quests", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_G, FTBLib.KEY_CATEGORY);

	@Override
	public void preInit()
	{
		ClientRegistry.registerKeyBinding(KEY_QUESTS);
	}

	@Override
	public QuestFile getQuestFile(boolean clientSide)
	{
		return clientSide ? ClientQuestFile.INSTANCE : ServerQuestFile.INSTANCE;
	}
}