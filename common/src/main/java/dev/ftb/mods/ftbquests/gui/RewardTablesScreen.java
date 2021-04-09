package dev.ftb.mods.ftbquests.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbguilibrary.config.StringConfig;
import dev.ftb.mods.ftbguilibrary.config.gui.EditConfigFromStringScreen;
import dev.ftb.mods.ftbguilibrary.icon.Color4I;
import dev.ftb.mods.ftbguilibrary.misc.ButtonListBaseScreen;
import dev.ftb.mods.ftbguilibrary.utils.MouseButton;
import dev.ftb.mods.ftbguilibrary.utils.TooltipList;
import dev.ftb.mods.ftbguilibrary.widget.ContextMenuItem;
import dev.ftb.mods.ftbguilibrary.widget.GuiIcons;
import dev.ftb.mods.ftbguilibrary.widget.Panel;
import dev.ftb.mods.ftbguilibrary.widget.SimpleTextButton;
import dev.ftb.mods.ftbguilibrary.widget.Theme;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.MessageCreateObject;
import dev.ftb.mods.ftbquests.net.MessageEditObject;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.RandomReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class RewardTablesScreen extends ButtonListBaseScreen {
	private class RewardTableButton extends SimpleTextButton {
		private final RewardTable table;

		public RewardTableButton(Panel panel, RewardTable t) {
			super(panel, t.getTitle(), t.getIcon());
			table = t;
			setHeight(14);

			if (table.lootCrate != null) {
				title = title.copy().withStyle(ChatFormatting.YELLOW);
			}
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();

			if (button.isLeft()) {
				table.onEditButtonClicked(this);
				return;
			}

			List<ContextMenuItem> contextMenu = new ArrayList<>();
			ClientQuestFile.INSTANCE.questScreen.addObjectMenuItems(contextMenu, RewardTablesScreen.this, table);
			contextMenu.add(new ContextMenuItem(new TranslatableComponent("item.ftbquests.lootcrate"), GuiIcons.ACCEPT, () -> {
				if (table.lootCrate == null) {
					table.lootCrate = new LootCrate(table);
					Matcher matcher = Pattern.compile("[^a-z0-9_]").matcher(table.getTitle().getString().toLowerCase());
					Matcher matcher1 = Pattern.compile("_{2,}").matcher(matcher.replaceAll("_"));
					table.lootCrate.stringID = matcher1.replaceAll("_");

					switch (table.lootCrate.stringID) {
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
				} else {
					table.lootCrate = null;
					title = table.getTitle();
				}

				new MessageEditObject(table).sendToServer();
			}) {
				@Override
				public void drawIcon(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
					(table.lootCrate != null ? GuiIcons.ACCEPT : GuiIcons.ACCEPT_GRAY).draw(matrixStack, x, y, w, h);
				}
			});
			getGui().openContextMenu(contextMenu);
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			super.addMouseOverText(list);

			int usedIn = 0;

			for (ChapterGroup group : table.file.chapterGroups) {
				for (Chapter chapter : group.chapters) {
					for (Quest quest : chapter.quests) {
						for (Reward reward : quest.rewards) {
							if (reward instanceof RandomReward && ((RandomReward) reward).table == table) {
								usedIn++;
							}
						}
					}
				}
			}

			if (usedIn > 0) {
				list.add(new TranslatableComponent("ftbquests.reward_table.used_in", usedIn).withStyle(ChatFormatting.GRAY));
			}

			table.addMouseOverText(list, true, true);
		}
	}

	public RewardTablesScreen() {
		setTitle(new TranslatableComponent("ftbquests.reward_tables"));
		setHasSearchBox(true);
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel) {
		SimpleTextButton button = new SimpleTextButton(panel, new TranslatableComponent("gui.add"), GuiIcons.ADD) {
			@Override
			public void onClicked(MouseButton button) {
				playClickSound();
				StringConfig c = new StringConfig();
				EditConfigFromStringScreen.open(c, "", "", accepted -> {
					if (accepted) {
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

		for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables) {
			panel.add(new RewardTableButton(panel, table));
		}
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}
}