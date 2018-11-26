package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftblib.lib.OtherMods;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiSelectFluid;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiSelectItemStack;
import com.feed_the_beast.ftbquests.FTBQuestsCommon;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.ChoiceReward;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.ItemReward;
import com.feed_the_beast.ftbquests.quest.reward.RandomReward;
import com.feed_the_beast.ftbquests.quest.reward.XPLevelsReward;
import com.feed_the_beast.ftbquests.quest.reward.XPReward;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.FluidTask;
import com.feed_the_beast.ftbquests.quest.task.ItemTask;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.ItemHandlerHelper;
import org.lwjgl.input.Keyboard;

public class FTBQuestsClient extends FTBQuestsCommon
{
	public static KeyBinding KEY_QUESTS;

	@Override
	public void preInit()
	{
		KEY_QUESTS = new KeyBinding("key.ftbquests.quests", KeyConflictContext.IN_GAME, Loader.isModLoaded(OtherMods.FTBGUIDES) ? KeyModifier.CONTROL : KeyModifier.NONE, Keyboard.KEY_G, FTBLib.KEY_CATEGORY);
		ClientRegistry.registerKeyBinding(KEY_QUESTS);
	}

	@Override
	public QuestFile getQuestFile(boolean clientSide)
	{
		return clientSide ? ClientQuestFile.INSTANCE : ServerQuestFile.INSTANCE;
	}

	@Override
	public void setTaskGuiProviders()
	{
		FTBQuestsTasks.ITEM.setGuiProvider((gui, quest, callback) -> new GuiSelectItemStack(gui, stack -> {
			if (!stack.isEmpty())
			{
				ItemTask itemTask = new ItemTask(quest);
				itemTask.items.add(ItemHandlerHelper.copyStackWithSize(stack, 1));
				itemTask.count = stack.getCount();
				callback.accept(itemTask);
			}
		}).openGui());

		FTBQuestsTasks.FLUID.setGuiProvider((gui, quest, callback) -> new GuiSelectFluid(gui, () -> FluidRegistry.WATER, fluid -> {
			if (fluid != null)
			{
				FluidTask fluidTask = new FluidTask(quest);
				fluidTask.fluid = fluid;
				callback.accept(fluidTask);
			}
		}).openGui());
	}

	@Override
	public void setRewardGuiProviders()
	{
		FTBQuestsRewards.ITEM.setGuiProvider((gui, quest, callback) -> new GuiSelectItemStack(gui, stack -> {
			if (!stack.isEmpty())
			{
				ItemReward reward = new ItemReward(quest);
				reward.stack = stack;
				callback.accept(reward);
			}
		}).openGui());

		FTBQuestsRewards.XP.setGuiProvider((gui, quest, callback) -> new GuiEditConfigValue("value", new ConfigInt(100, 1, Integer.MAX_VALUE), (value, set) -> {
			gui.openGui();
			if (set)
			{
				XPReward reward = new XPReward(quest);
				reward.xp = value.getInt();
				callback.accept(reward);
			}
		}).openGui());

		FTBQuestsRewards.XP_LEVELS.setGuiProvider((gui, quest, callback) -> new GuiEditConfigValue("value", new ConfigInt(1, 1, Integer.MAX_VALUE), (value, set) -> {
			gui.openGui();
			if (set)
			{
				XPLevelsReward reward = new XPLevelsReward(quest);
				reward.xpLevels = value.getInt();
				callback.accept(reward);
			}
		}).openGui());

		FTBQuestsRewards.CHOICE.setGuiProvider((gui, quest, callback) -> callback.accept(new ChoiceReward(quest)));
		FTBQuestsRewards.RANDOM.setGuiProvider((gui, quest, callback) -> callback.accept(new RandomReward(quest)));
	}
}