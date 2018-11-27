package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigValue;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetVerticalSpace;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfig;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.gui.misc.IConfigValueEditCallback;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import com.feed_the_beast.ftbquests.quest.reward.RandomReward;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiEditRandomReward extends GuiButtonListBase
{
	private class ButtonEditRandomReward extends SimpleTextButton
	{
		private ButtonEditRandomReward(Panel panel)
		{
			super(panel, I18n.format("gui.settings"), GuiIcons.SETTINGS);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			new MessageEditObject(randomReward.uid).sendToServer();
		}
	}

	private class ButtonAcceptRandomReward extends SimpleTextButton
	{
		private ButtonAcceptRandomReward(Panel panel)
		{
			super(panel, I18n.format("gui.accept"), GuiIcons.ACCEPT);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			closeGui();
			NBTTagCompound nbt = new NBTTagCompound();
			randomReward.writeData(nbt);
			original.readData(nbt);
			callback.run();
		}
	}

	private class ButtonAddRandomReward extends SimpleTextButton
	{
		private ButtonAddRandomReward(Panel panel)
		{
			super(panel, I18n.format("gui.add"), GuiIcons.ADD);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			for (QuestRewardType type : QuestRewardType.getRegistry())
			{
				if (!type.getExcludeFromListRewards())
				{
					contextMenu.add(new ContextMenuItem(type.getDisplayName().getFormattedText(), type.getIcon(), () -> {
						GuiHelper.playClickSound();
						type.getGuiProvider().openCreationGui(this, randomReward.quest, reward -> {
							randomReward.rewards.add(new RandomReward.WeightedReward(reward, 1));
							openGui();
						});
					}));
				}
			}

			getGui().openContextMenu(contextMenu);
		}
	}

	private class ButtonRandomReward extends SimpleTextButton implements IConfigValueEditCallback
	{
		private final RandomReward.WeightedReward reward;

		private ButtonRandomReward(Panel panel, RandomReward.WeightedReward r)
		{
			super(panel, r.reward.getDisplayName().getFormattedText(), r.reward.getIcon());
			reward = r;
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			super.addMouseOverText(list);
			reward.reward.addMouseOverText(list);

			int totalWeight = 0;

			for (RandomReward.WeightedReward r : randomReward.rewards)
			{
				totalWeight += r.weight;
			}

			int chance = reward.weight * 100 / totalWeight;

			if (chance == 0)
			{
				list.add(I18n.format("ftbquests.reward.ftbquests.random.weight") + ": " + reward.weight + TextFormatting.DARK_GRAY + " [" + String.format("%.2f", reward.weight * 100D / (double) totalWeight) + "%]");
			}
			else
			{
				list.add(I18n.format("ftbquests.reward.ftbquests.random.weight") + ": " + reward.weight + TextFormatting.DARK_GRAY + " [" + (reward.weight * 100 / totalWeight) + "%]");
			}
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			contextMenu.add(new ContextMenuItem(I18n.format("selectServer.edit"), GuiIcons.SETTINGS, () -> {
				ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
				ConfigGroup g = reward.reward.createSubGroup(group);
				reward.reward.getConfig(g);
				new GuiEditConfig(group, IConfigCallback.DEFAULT).openGui();
			}));

			contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.reward.ftbquests.random.setweight"), GuiIcons.SETTINGS, () -> new GuiEditConfigValue("value", new ConfigInt(reward.weight, 1, Integer.MAX_VALUE), this).openGui()));
			contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> {
				randomReward.rewards.remove(reward);
				GuiEditRandomReward.this.refreshWidgets();
			}).setYesNo(I18n.format("delete_item", reward.reward.getDisplayName().getFormattedText())));
			GuiEditRandomReward.this.openContextMenu(contextMenu);
		}

		@Override
		public void onCallback(ConfigValue value, boolean set)
		{
			openGui();

			if (set)
			{
				reward.weight = value.getInt();
			}
		}
	}

	private final RandomReward original;
	private final RandomReward randomReward;
	private final Runnable callback;

	public GuiEditRandomReward(RandomReward r, Runnable c)
	{
		original = r;
		randomReward = new RandomReward(original.quest);
		NBTTagCompound nbt = new NBTTagCompound();
		original.writeData(nbt);
		randomReward.readData(nbt);
		callback = c;
		setTitle(I18n.format("ftbquests.reward.ftbquests.random"));
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel)
	{
		panel.add(new ButtonEditRandomReward(panel));
		panel.add(new ButtonAcceptRandomReward(panel));
		panel.add(new ButtonAddRandomReward(panel));
		panel.add(new WidgetVerticalSpace(panel, 1));

		for (RandomReward.WeightedReward r : randomReward.rewards)
		{
			panel.add(new ButtonRandomReward(panel, r));
		}
	}

	@Override
	public Theme getTheme()
	{
		return FTBQuestsTheme.INSTANCE;
	}
}