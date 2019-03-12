package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObjectDirect;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.RandomReward;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class GuiRewardTables extends GuiButtonListBase
{
	private class ButtonRewardTable extends SimpleTextButton
	{
		private final RewardTable table;

		public ButtonRewardTable(Panel panel, RewardTable t)
		{
			super(panel, t.getDisplayName().getFormattedText(), t.getIcon());
			table = t;
			setHeight(14);

			if (table.lootCrate != null)
			{
				title = TextFormatting.YELLOW + title;
			}
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			GuiQuestTree.addObjectMenuItems(contextMenu, GuiRewardTables.this, table);
			contextMenu.add(new ContextMenuItem(I18n.format("item.ftbquests.lootcrate.name"), GuiIcons.ACCEPT, () -> {
				if (table.lootCrate == null)
				{
					table.lootCrate = new LootCrate(table);
					Matcher matcher = Pattern.compile("[^a-z0-9_]").matcher(table.getDisplayName().getUnformattedText().trim().toLowerCase());
					Matcher matcher1 = Pattern.compile("_{2,}").matcher(matcher.replaceAll("_"));
					table.lootCrate.stringID = matcher1.replaceAll("_");

					switch (table.lootCrate.stringID)
					{
						case "common":
							table.lootCrate.color = Color4I.rgb(0x92999A);
							table.lootCrate.drops.passive = 350;
							table.lootCrate.drops.monster = 10;
							table.lootCrate.drops.boss = 0;
							break;
						case "uncommon":
							table.lootCrate.color = Color4I.rgb(0x37AA69);
							table.lootCrate.drops.passive = 200;
							table.lootCrate.drops.monster = 90;
							table.lootCrate.drops.boss = 0;
							break;
						case "rare":
							table.lootCrate.color = Color4I.rgb(0x0094FF);
							table.lootCrate.drops.passive = 50;
							table.lootCrate.drops.monster = 200;
							table.lootCrate.drops.boss = 0;
							break;
						case "epic":
							table.lootCrate.color = Color4I.rgb(0x8000FF);
							table.lootCrate.drops.passive = 9;
							table.lootCrate.drops.monster = 10;
							table.lootCrate.drops.boss = 10;
							break;
						case "legendary":
							table.lootCrate.color = Color4I.rgb(0xFFC147);
							table.lootCrate.glow = true;
							table.lootCrate.drops.passive = 1;
							table.lootCrate.drops.monster = 1;
							table.lootCrate.drops.boss = 190;
							break;
					}

					title = TextFormatting.YELLOW + table.getDisplayName().getFormattedText();
				}
				else
				{
					table.lootCrate = null;
					title = table.getDisplayName().getFormattedText();
				}

				new MessageEditObjectDirect(table).sendToServer();
			})
			{
				@Override
				public void drawIcon(Theme theme, int x, int y, int w, int h)
				{
					(table.lootCrate != null ? GuiIcons.ACCEPT : GuiIcons.ACCEPT_GRAY).draw(x, y, w, h);
				}
			});
			getGui().openContextMenu(contextMenu);
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			super.addMouseOverText(list);

			int usedIn = 0;

			for (QuestChapter chapter : table.file.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					for (QuestReward reward : quest.rewards)
					{
						if (reward instanceof RandomReward && ((RandomReward) reward).table == table)
						{
							usedIn++;
						}
					}
				}
			}

			if (usedIn > 0)
			{
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.reward_table.used_in", usedIn));
			}

			table.addMouseOverText(list, true, true);
		}
	}

	public GuiRewardTables()
	{
		setTitle(I18n.format("ftbquests.reward_tables"));
		setHasSearchBox(true);
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel)
	{
		SimpleTextButton button = new SimpleTextButton(panel, I18n.format("gui.add"), GuiIcons.ADD)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				GuiHelper.playClickSound();

				new GuiEditConfigValue("id", new ConfigString(""), (value, set) -> {
					GuiRewardTables.this.openGui();

					if (set)
					{
						RewardTable table = new RewardTable(ClientQuestFile.INSTANCE);
						table.title = value.getString();
						new MessageCreateObject(table, null).sendToServer();
					}
				}).openGui();
			}
		};

		button.setHeight(14);
		panel.add(button);

		for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables)
		{
			panel.add(new ButtonRewardTable(panel, table));
		}
	}

	@Override
	public Theme getTheme()
	{
		return FTBQuestsTheme.INSTANCE;
	}
}