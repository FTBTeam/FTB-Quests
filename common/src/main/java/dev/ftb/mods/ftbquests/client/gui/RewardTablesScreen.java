package dev.ftb.mods.ftbquests.client.gui;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditStringConfigOverlay;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.CreateObjectMessage;
import dev.ftb.mods.ftbquests.net.DeleteObjectMessage;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.RandomReward;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class RewardTablesScreen extends AbstractButtonListScreen {
	private final QuestScreen questScreen;
	private final SimpleTextButton addButton;
	private final List<RewardTable> rewardTablesCopy; // deep local copy of reward tables
	private boolean changed = false;
	private final Set<RewardTable> editedTables = new HashSet<>();

	public RewardTablesScreen(QuestScreen questScreen) {
		super();

		this.questScreen = questScreen;
		this.rewardTablesCopy = ClientQuestFile.INSTANCE.getRewardTables().stream()
				.map(table -> QuestObjectBase.copy(table,
						() -> new RewardTable(table.id, ClientQuestFile.INSTANCE),
						FTBQuestsClient.getClientLevel().registryAccess()))
				.collect(Collectors.toCollection(ArrayList::new));

		setTitle(Component.translatable("ftbquests.reward_tables"));
		setHasSearchBox(true);
		setBorder(1, 1, 1);

		addButton = new SimpleTextButton(topPanel, Component.translatable("gui.add"), Icons.ADD) {
			@Override
			public void onClicked(MouseButton button) {
				playClickSound();

				StringConfig cfg = new StringConfig();
				EditStringConfigOverlay<String> panel = new EditStringConfigOverlay<>(getGui(), cfg, accepted -> {
					if (accepted) {
						RewardTable table = new RewardTable(0L, ClientQuestFile.INSTANCE);
						table.setRawTitle(cfg.getValue());
						rewardTablesCopy.add(table);
						refreshWidgets();
					}
				}).atPosition(posX, posY + height);
				panel.setExtraZlevel(300);  // might to render over an item icon button
				getGui().pushModalPanel(panel);
			}
		};
	}

	@Override
	public void addButtons(Panel panel) {
		rewardTablesCopy.stream().sorted()
				.forEach(table -> panel.add(new RewardTableButton(panel, table)));
	}

	@Override
	protected int getTopPanelHeight() {
		return 25;
	}

	@Override
	protected Panel createTopPanel() {
		return new CustomTopPanel();
	}

	@Override
	public boolean onInit() {
		int maxW = Math.max(
				getTheme().getStringWidth(getTitle()) + 100,
				rewardTablesCopy.stream().map(t -> getTheme().getStringWidth(t.getTitle())).max(Comparator.naturalOrder()).orElse(0)
		);

		setWidth(maxW);
		setHeight(getGui().getScreen().getGuiScaledHeight() * 4 / 5);

		return true;
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			doCancel();
			return true;
		}

		return false;
	}

	@Override
	protected void doCancel() {
		if (changed) {
			openYesNo(Component.translatable("ftblibrary.unsaved_changes"), Component.empty(), questScreen);
		} else {
			questScreen.run();
		}
	}

	@Override
	protected void doAccept() {
		Set<Long> toRemove = ClientQuestFile.INSTANCE.getRewardTables().stream().map(t -> t.id).collect(Collectors.toSet());

		rewardTablesCopy.forEach(table -> {
			if (table.id == 0) {
				// newly-created
				NetworkManager.sendToServer(CreateObjectMessage.create(table, null));
			}
			toRemove.remove(table.id);
		});

		toRemove.forEach(id -> NetworkManager.sendToServer(new DeleteObjectMessage(id)));

		editedTables.forEach(table -> NetworkManager.sendToServer(EditObjectMessage.forQuestObject(table)));

		questScreen.run();
	}

	private class CustomTopPanel extends TopPanel {
		@Override
		public void addWidgets() {
			add(addButton);
		}

		@Override
		public void alignWidgets() {
			addButton.setPosAndSize(width - addButton.width - 2, 1, addButton.width, 20);
		}

		@Override
		public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			super.draw(graphics, theme, x, y, w, h);

			theme.drawString(graphics, getGui().getTitle(), x + 6, y + 6, Theme.SHADOW);
		}
	}

	private class RewardTableButton extends SimpleTextButton {
		private final RewardTable table;

		public RewardTableButton(Panel panel, RewardTable table) {
			super(panel, table.getTitle(), table.getIcon());

			this.table = table;
			setHeight(16);

			if (this.table.getLootCrate() != null) {
				title = title.copy().withStyle(ChatFormatting.YELLOW);
			}
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();

			if (button.isLeft()) {
				if (getMouseX() > getX() + width - 13) {
					deleteRewardTable();
				} else if (getMouseX() > getX() + width - 26) {
					toggleLootCrate();
				} else {
					editRewardTable();
				}
				return;
			}

			List<ContextMenuItem> menu = List.of(
					new ContextMenuItem(Component.translatable("ftbquests.gui.edit"), ItemIcon.getItemIcon(Items.FEATHER),
							b -> editRewardTable()),
					new ContextMenuItem(Component.translatable("gui.remove"), Icons.BIN,
							b -> deleteRewardTable()),
					new ContextMenuItem(getLootCrateText(), ItemIcon.getItemIcon(ModItems.LOOTCRATE.get()),
							b -> toggleLootCrate())
			);
			getGui().openContextMenu(menu);
		}

		@Override
		public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			if (isMouseOver) {
				Color4I.WHITE.withAlpha(30).draw(graphics, x, y, w, h);
				ItemIcon.getItemIcon(ModItems.LOOTCRATE.get()).draw(graphics, x + w - 26, y + 2, 12, 12);
				Icons.BIN.draw(graphics, x + w - 13, y + 2, 12, 12);
			}
			Color4I.GRAY.withAlpha(40).draw(graphics, x, y + h, w, 1);
		}

		private void editRewardTable() {
			new EditRewardTableScreen(RewardTablesScreen.this, table, editedReward -> {
				rewardTablesCopy.replaceAll(t -> t.id == editedReward.id ? editedReward : t);
				changed = true;
				editedTables.add(editedReward);
				editedReward.clearCachedData();
				refreshWidgets();
			}).openGui();
		}

		private void deleteRewardTable() {
			openYesNo(Component.translatable("delete_item", table.getTitle()), Component.empty(), () -> {
				rewardTablesCopy.removeIf(t -> t == table);
				changed = true;
				refreshWidgets();
			});
		}

		private void toggleLootCrate() {
			LootCrate crate = table.toggleLootCrate();

			if (crate != null) {
				title = this.table.getMutableTitle().withStyle(ChatFormatting.YELLOW);
			} else {
				title = this.table.getTitle();
			}

			changed = true;
			refreshWidgets();
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			super.addMouseOverText(list);

			if (getMouseX() > getX() + width - 13) {
				list.add(Component.translatable("gui.remove"));
			} else if (getMouseX() > getX() + width - 26) {
				list.add(getLootCrateText());
			} else {
				MutableInt usedIn = new MutableInt(0);
				ClientQuestFile.INSTANCE.forAllQuests(quest -> quest.getRewards().stream()
						.filter(reward -> reward instanceof RandomReward rr && rr.getTable() != null && rr.getTable().id == table.id)
						.forEach(reward -> usedIn.increment()));
				list.add(Component.translatable("ftbquests.reward_table.used_in", usedIn));

				table.addMouseOverText(list, true, true);
			}
		}

		@NotNull
		private Component getLootCrateText() {
            return Component.translatable("ftbquests.reward_table." +
					(table.getLootCrate() != null ? "disable_loot_crate" : "enable_loot_crate"));
		}
	}
}
