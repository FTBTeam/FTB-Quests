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
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiEditRewardTable extends GuiButtonListBase
{
	private class ButtonRewardTableSettings extends SimpleTextButton
	{
		private ButtonRewardTableSettings(Panel panel)
		{
			super(panel, I18n.format("gui.settings"), GuiIcons.SETTINGS);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
			rewardTable.getConfig(rewardTable.createSubGroup(group));
			new GuiEditConfig(group, IConfigCallback.DEFAULT).openGui();
		}
	}

	private class ButtonSaveRewardTable extends SimpleTextButton
	{
		private ButtonSaveRewardTable(Panel panel)
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
			rewardTable.writeData(nbt);
			originalTable.readData(nbt);
			callback.run();
		}
	}

	private class ButtonAddWeightedReward extends SimpleTextButton
	{
		private ButtonAddWeightedReward(Panel panel)
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
						type.getGuiProvider().openCreationGui(this, rewardTable.fakeQuest, reward -> {
							rewardTable.rewards.add(new WeightedReward(reward, 1));
							openGui();
						});
					}));
				}
			}

			getGui().openContextMenu(contextMenu);
		}
	}

	private class ButtonWeightedReward extends SimpleTextButton implements IConfigValueEditCallback
	{
		private final WeightedReward reward;

		private ButtonWeightedReward(Panel panel, WeightedReward r)
		{
			super(panel, r.reward.getDisplayName().getFormattedText(), r.reward.getIcon());
			reward = r;
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			super.addMouseOverText(list);
			reward.reward.addMouseOverText(list);
			list.add(I18n.format("ftbquests.reward_table.weight") + ": " + reward.weight + TextFormatting.DARK_GRAY + " [" + WeightedReward.chanceString(reward.weight, rewardTable.getTotalWeight(true)) + "]");
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			contextMenu.add(new ContextMenuItem(I18n.format("selectServer.edit"), GuiIcons.SETTINGS, () -> {
				ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
				reward.reward.getConfig(reward.reward.createSubGroup(group));
				new GuiEditConfig(group, IConfigCallback.DEFAULT).openGui();
			}));

			contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.reward_table.set_weight"), GuiIcons.SETTINGS, () -> new GuiEditConfigValue("value", new ConfigInt(reward.weight, 1, Integer.MAX_VALUE), this).openGui()));
			contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> {
				rewardTable.rewards.remove(reward);
				GuiEditRewardTable.this.refreshWidgets();
			}).setYesNo(I18n.format("delete_item", reward.reward.getDisplayName().getFormattedText())));
			GuiEditRewardTable.this.openContextMenu(contextMenu);
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

		@Override
		@Nullable
		public Object getIngredientUnderMouse()
		{
			return reward.reward.getJEIFocus();
		}
	}

	private final RewardTable originalTable;
	private final RewardTable rewardTable;
	private final Runnable callback;

	public GuiEditRewardTable(RewardTable r, Runnable c)
	{
		originalTable = r;
		rewardTable = new RewardTable(originalTable.file);
		NBTTagCompound nbt = new NBTTagCompound();
		originalTable.writeData(nbt);
		rewardTable.readData(nbt);
		callback = c;
		setTitle(I18n.format("ftbquests.reward_table"));
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel)
	{
		panel.add(new ButtonRewardTableSettings(panel));
		panel.add(new ButtonSaveRewardTable(panel));
		panel.add(new ButtonAddWeightedReward(panel));
		panel.add(new WidgetVerticalSpace(panel, 1));

		for (WeightedReward r : rewardTable.rewards)
		{
			panel.add(new ButtonWeightedReward(panel, r));
		}
	}

	@Override
	public Theme getTheme()
	{
		return FTBQuestsTheme.INSTANCE;
	}
}