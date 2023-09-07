package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.ButtonListBaseScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.CreateObjectMessage;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.RandomReward;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;

/**
 * @author LatvianModder
 */
public class RewardTablesScreen extends ButtonListBaseScreen {
	private final QuestScreen questScreen;

	public RewardTablesScreen(QuestScreen questScreen) {
		this.questScreen = questScreen;

		setTitle(Component.translatable("ftbquests.reward_tables"));
		setHasSearchBox(true);
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel) {
		SimpleTextButton button = new SimpleTextButton(panel, Component.translatable("gui.add"), Icons.ADD) {
			@Override
			public void onClicked(MouseButton button) {
				playClickSound();
				StringConfig c = new StringConfig();
				EditConfigFromStringScreen.open(c, "", "", accepted -> {
					if (accepted) {
						RewardTable table = new RewardTable(0L, ClientQuestFile.INSTANCE);
						table.setRawTitle(c.getValue());
						new CreateObjectMessage(table, null).sendToServer();
					}

					openGui();
				});
			}
		};

		button.setHeight(14);
		panel.add(button);

		ClientQuestFile.INSTANCE.getRewardTables()
				.forEach(table -> panel.add(new RewardTableButton(panel, table)));
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	@Override
	public boolean keyPressed(Key key) {
		if (key.esc()) {
			onBack();
			return true;
		} else {
			return super.keyPressed(key);
		}
	}

	private class RewardTableButton extends SimpleTextButton {
		private final RewardTable table;

		public RewardTableButton(Panel panel, RewardTable table) {
			super(panel, table.getTitle(), table.getIcon());

			this.table = table;
			setHeight(14);

			if (this.table.getLootCrate() != null) {
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

			ContextMenuBuilder.create(table, questScreen)
					.insertAtBottom(List.of(new ContextMenuItem(Component.translatable("item.ftbquests.lootcrate"), Icons.ACCEPT, this::toggleLootCrate) {
						@Override
						public void drawIcon(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
							(table.getLootCrate() != null ? Icons.ACCEPT : Icons.ACCEPT_GRAY).draw(graphics, x, y, w, h);
						}
					}))
					.openContextMenu(RewardTablesScreen.this);
		}

		private void toggleLootCrate() {
			LootCrate crate = table.toggleLootCrate();

			if (crate != null) {
				title = table.getMutableTitle().withStyle(ChatFormatting.YELLOW);
			} else {
				title = table.getTitle();
			}

			new EditObjectMessage(table).sendToServer();
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			super.addMouseOverText(list);

			MutableInt usedIn = new MutableInt(0);
			ClientQuestFile.INSTANCE.forAllQuests(quest -> quest.getRewards().stream()
					.filter(reward -> reward instanceof RandomReward rr && rr.getTable() == table)
					.forEach(reward -> usedIn.increment()));

			if (usedIn.intValue() > 0) {
				list.add(Component.translatable("ftbquests.reward_table.used_in", usedIn).withStyle(ChatFormatting.GRAY));
			}

			table.addMouseOverText(list, true, true);
		}
	}
}
