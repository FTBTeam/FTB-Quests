package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.quests.GuiQuests;
import com.feed_the_beast.ftbquests.net.MessageCreateObject;
import com.feed_the_beast.ftbquests.net.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.ChapterGroup;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.reward.RandomReward;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigString;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.misc.GuiButtonListBase;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

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
			super(panel, t.getTitle(), t.getIcon());
			table = t;
			setHeight(14);

			if (table.lootCrate != null)
			{
				title = title.copy().withStyle(ChatFormatting.YELLOW);
			}
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();

			if (button.isLeft())
			{
				table.onEditButtonClicked(this);
				return;
			}

			List<ContextMenuItem> contextMenu = new ArrayList<>();
			GuiQuests.addObjectMenuItems(contextMenu, GuiRewardTables.this, table);
			contextMenu.add(new ContextMenuItem(new TranslatableComponent("item.ftbquests.lootcrate"), GuiIcons.ACCEPT, () -> {
				if (table.lootCrate == null)
				{
					table.lootCrate = new LootCrate(table);
					Matcher matcher = Pattern.compile("[^a-z0-9_]").matcher(table.getUnformattedTitle().toLowerCase());
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

					title = table.getMutableTitle().withStyle(ChatFormatting.YELLOW);
				}
				else
				{
					table.lootCrate = null;
					title = table.getTitle();
				}

				new MessageEditObject(table).sendToServer();
			})
			{
				@Override
				public void drawIcon(PoseStack matrixStack, Theme theme, int x, int y, int w, int h)
				{
					(table.lootCrate != null ? GuiIcons.ACCEPT : GuiIcons.ACCEPT_GRAY).draw(matrixStack, x, y, w, h);
				}
			});
			getGui().openContextMenu(contextMenu);
		}

		@Override
		public void addMouseOverText(TooltipList list)
		{
			super.addMouseOverText(list);

			int usedIn = 0;

			for (ChapterGroup group : table.file.chapterGroups)
			{
				for (Chapter chapter : group.chapters)
				{
					for (Quest quest : chapter.quests)
					{
						for (Reward reward : quest.rewards)
						{
							if (reward instanceof RandomReward && ((RandomReward) reward).table == table)
							{
								usedIn++;
							}
						}
					}
				}
			}

			if (usedIn > 0)
			{
				list.add(new TranslatableComponent("ftbquests.reward_table.used_in", usedIn).withStyle(ChatFormatting.GRAY));
			}

			table.addMouseOverText(list, true, true);
		}
	}

	public GuiRewardTables()
	{
		setTitle(new TranslatableComponent("ftbquests.reward_tables"));
		setHasSearchBox(true);
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel)
	{
		SimpleTextButton button = new SimpleTextButton(panel, new TranslatableComponent("gui.add"), GuiIcons.ADD)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				playClickSound();
				ConfigString c = new ConfigString();
				GuiEditConfigFromString.open(c, "", "", accepted -> {
					if (accepted)
					{
						RewardTable table = new RewardTable(ClientQuestFile.INSTANCE);
						table.title = c.value;
						new MessageCreateObject(table, null).sendToServer();
					}

					openGui();
				});
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