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
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.CreateObjectMessage;
import dev.ftb.mods.ftbquests.net.DeleteObjectMessage;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.RandomReward;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.registry.ModItems;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RewardTablesScreen extends AbstractButtonListScreen {
	private final QuestScreen questScreen;
	private final SimpleTextButton addButton;
	private final List<RewardTable> rewardTablesCopy; // deep local copy of reward tables
	private final IntSet editedIndexes = new IntOpenHashSet();
	private final IntSet pendingDeleteIndexes = new IntOpenHashSet();
	private boolean changed = false;

	public RewardTablesScreen(QuestScreen questScreen) {
		super();

		this.questScreen = questScreen;
		this.rewardTablesCopy = ClientQuestFile.INSTANCE.getRewardTables().stream()
				.map(table -> QuestObjectBase.copy(table,
						() -> new RewardTable(table.id, ClientQuestFile.INSTANCE)))
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
		List<RewardTableButton> buttons = new ArrayList<>();
		for (int i = 0; i < rewardTablesCopy.size(); i++) {
			RewardTable table = rewardTablesCopy.get(i);
			buttons.add(new RewardTableButton(panel, table, i));
		}
		panel.addAll(buttons.stream().sorted(Comparator.comparing(btn -> btn.table)).toList());
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
		IntSet toCreate = new IntOpenHashSet();
		for (int idx = 0; idx < rewardTablesCopy.size(); idx++) {
			if (rewardTablesCopy.get(idx).getId() == 0L && !pendingDeleteIndexes.contains(idx)) {
				toCreate.add(idx);
			}
		}
		editedIndexes.removeAll(pendingDeleteIndexes);

		int nAdded = sendToServer(toCreate, RewardTablesScreen::makeCreationPacket, true);
		int nEdited = sendToServer(editedIndexes, EditObjectMessage::forQuestObject, false);
		int nDeleted = sendToServer(pendingDeleteIndexes, DeleteObjectMessage::forQuestObject, false);

		FTBQuests.LOGGER.debug("Sent {} new, {} edited, {} deleted reward tables to server", nAdded, nEdited, nDeleted);

		questScreen.run();
	}

	private static CreateObjectMessage makeCreationPacket(RewardTable table) {
		ClientQuestFile file = ClientQuestFile.INSTANCE;
		CompoundTag extra = Util.make(new CompoundTag(), tag -> file.getTranslationManager().addInitialTranslation(
				tag, file.getLocale(), TranslationKey.TITLE, table.getRawTitle())
		);
		return CreateObjectMessage.create(table, extra);
	}

	private <T extends CustomPacketPayload> int sendToServer(IntSet indexes, Function<RewardTable, T> func, boolean addNew) {
		int sent = 0;
		for (int idx : indexes) {
			if (idx >= 0 && idx < rewardTablesCopy.size()) {
				RewardTable table = rewardTablesCopy.get(idx);
				if (addNew && table.id == 0 || !addNew && table.id != 0) {
					// id == 0 means table is only locally added, no need to sync an edit/delete for it
					NetworkManager.sendToServer(func.apply(table));
					sent++;
				}
			}
		}
		return sent;
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
		private final int idx;

		public RewardTableButton(Panel panel, RewardTable table, int idx) {
			super(panel, table.getTitle(), table.getIcon());

			this.table = table;
			this.idx = idx;
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
					new ContextMenuItem(Component.translatable(pendingDeleteIndexes.contains(idx) ? "ftbquests.gui.restore" : "gui.remove"), Icons.BIN,
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
			if (pendingDeleteIndexes.contains(idx)) {
				Color4I.RED.withAlpha(64).draw(graphics, x, y, w, h);
			} else if (rewardTablesCopy.get(idx).getId() == 0) {
				Color4I.GREEN.withAlpha(64).draw(graphics, x, y, w, h);
			}
			Color4I.GRAY.withAlpha(40).draw(graphics, x, y + h, w, 1);
		}

		@Override
		public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			super.draw(graphics, theme, x, y, w, h);

			if (pendingDeleteIndexes.contains(idx)) {
				Color4I.GRAY.draw(graphics, x + 20, y + h / 2, theme.getStringWidth(title), 1);
			}  else if (rewardTablesCopy.get(idx).getId() == 0) {
				Icons.ADD.draw(graphics, x + 24 + theme.getStringWidth(title), y + 2, 12, 12);
			}
		}

		private void editRewardTable() {
			new EditRewardTableScreen(RewardTablesScreen.this, table, editedReward -> {
				rewardTablesCopy.set(idx, editedReward);
				changed = true;
				editedIndexes.add(idx);
				editedReward.clearCachedData();
				refreshWidgets();
			}).openGui();
		}

		private void deleteRewardTable() {
			if (pendingDeleteIndexes.contains(idx)) {
				pendingDeleteIndexes.remove(idx);
			} else {
				pendingDeleteIndexes.add(idx);
			}
			changed = true;
			refreshWidgets();
		}

		private void toggleLootCrate() {
			LootCrate crate = table.toggleLootCrate();

			if (crate != null) {
				title = this.table.getMutableTitle().withStyle(ChatFormatting.YELLOW);
			} else {
				title = this.table.getTitle();
			}

			changed = true;
			editedIndexes.add(idx);
			refreshWidgets();
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			super.addMouseOverText(list);

			if (getMouseX() > getX() + width - 13) {
				list.add(Component.translatable(pendingDeleteIndexes.contains(idx) ? "ftbquests.gui.restore" : "gui.remove"));
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
