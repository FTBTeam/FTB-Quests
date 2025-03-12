package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class SelectQuestObjectScreen<T extends QuestObjectBase> extends AbstractButtonListScreen {
	private final ConfigQuestObject<T> config;
	private final ConfigCallback callback;
	private Function<T,Component> formatter = this::defaultFormatter;

	public SelectQuestObjectScreen(ConfigQuestObject<T> config, ConfigCallback callback) {
		setTitle(Component.translatable("ftbquests.gui.select_quest_object"));
		setHasSearchBox(true);
		showBottomPanel(false);
		showCloseButton(true);
		focus();
		setBorder(1, 1, 1);

		this.config = config;
		this.callback = callback;
	}

	public SelectQuestObjectScreen<T> withFormatter(@Nullable Function<T,Component> formatter) {
		this.formatter = Objects.requireNonNullElse(formatter, this::defaultFormatter);
		return this;
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			callback.save(false);
			return true;
		}

		return false;
	}

	@Override
	public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		super.drawBackground(graphics, theme, x, y, w, h);

		GuiHelper.drawHollowRect(graphics, mainPanel.getX() - 1, mainPanel.getY() - 1, mainPanel.width + 2, mainPanel.height + 2,
				Color4I.DARK_GRAY.withAlpha(40), false);
	}

	@Override
	public void addButtons(Panel panel) {
		List<T> list = new ArrayList<>();

		ClientQuestFile file = ClientQuestFile.INSTANCE;
		for (QuestObjectBase objectBase : file.getAllObjects()) {
			if (config.predicate.test(objectBase) &&
					(file.canEdit() || (!(objectBase instanceof QuestObject qo) || qo.isSearchable(file.selfTeamData)))) {
				list.add((T) objectBase);
			}
		}

		list.sort((o1, o2) -> {
			int i = Integer.compare(o1.getObjectType().ordinal(), o2.getObjectType().ordinal());
			return i == 0 ? o1.getTitle().getString().compareToIgnoreCase(o2.getTitle().getString()) : i;
		});

		if (config.predicate.test(null)) {
			panel.add(new QuestObjectButton(panel, null));
		}

		for (T objectBase : list) {
			panel.add(new QuestObjectButton(panel, objectBase));
		}

		int width = panel.getWidgets().stream().map(Widget::getWidth).max(Integer::compare).orElse(200);
		panel.getWidgets().forEach(w -> w.setWidth(width));
	}

	@Override
	public boolean onInit() {
		setWidth(mainPanel.getWidgets().isEmpty() ? 200 : mainPanel.getWidgets().getFirst().width + 20);
		return super.onInit();
	}

	@Override
	protected void doCancel() {
		callback.save(false);
	}

	@Override
	protected void doAccept() {
		callback.save(true);
	}

	private Component defaultFormatter(T qo) {
		return ConfigQuestObject.formatEntry(qo);
	}

	private class QuestObjectButton extends SimpleTextButton {
		public final T object;

		public QuestObjectButton(Panel panel, @Nullable T questObject) {
			super(panel,
					questObject == null ? Component.translatable("ftbquests.null") : formatter.apply(questObject),
					questObject == null ? Color4I.empty() : questObject.getIcon()
			);
			object = questObject;
			setHeight(getTheme().getFontHeight() + 4);
		}

		private void addObject(TooltipList list, QuestObjectBase o) {
			list.add(QuestObjectType.NAME_MAP.getDisplayName(o.getObjectType()).copy().withStyle(ChatFormatting.GRAY)
					.append(": ")
					.append(o.getMutableTitle().withStyle(o.getObjectType().getColor()))
			);
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			if (object == null) {
				return;
			}

			list.add(object.getTitle());
			list.add(Component.literal("ID: ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal(object.toString()).withStyle(ChatFormatting.DARK_GRAY)));
			list.add(Component.literal("Type: ").withStyle(ChatFormatting.GRAY)
					.append(QuestObjectType.NAME_MAP.getDisplayName(object.getObjectType()).copy().withStyle(object.getObjectType().getColor())));

            switch (object) {
                case Quest quest -> {
                    addObject(list, quest.getChapter());
                    addRewardTooltip(list, quest);
                }
                case QuestLink link -> link.getQuest().ifPresent(quest -> {
                    addObject(list, link.getChapter());
                    list.add(Component.translatable("ftbquests.gui.linked_quest_id", Component.literal(quest.getCodeString()).withStyle(ChatFormatting.DARK_GRAY)).withStyle(ChatFormatting.GRAY));
                    addObject(list, quest.getChapter());
                });
                case Task task -> {
                    Quest quest = task.getQuest();
                    addObject(list, quest.getChapter());
                    addObject(list, quest);
                    addRewardTooltip(list, quest);
                }
                case Reward reward -> {
                    Quest quest = reward.getQuest();
                    addObject(list, quest.getChapter());
                    addObject(list, quest);
                }
                case RewardTable rewardTable -> rewardTable.addMouseOverText(list, true, true);
                default -> {
                }
            }
		}

		private void addRewardTooltip(TooltipList list, Quest quest) {
			if (quest.getRewards().size() == 1) {
				addObject(list, quest.getRewards().stream().findFirst().orElseThrow());
			} else if (!quest.getRewards().isEmpty()) {
				list.add(Component.translatable("ftbquests.rewards").withStyle(ChatFormatting.GRAY));
				for (Reward reward : quest.getRewards()) {
					list.add(Component.literal("  ").append(reward.getMutableTitle().withStyle(QuestObjectType.REWARD.getColor())));
				}
			}
		}

		@Override
		public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			if (isMouseOver) {
				Color4I.WHITE.withAlpha(30).draw(graphics, x, y, w, h);
			}
			Color4I.GRAY.withAlpha(40).draw(graphics, x, y + h, w, 1);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			config.setCurrentValue(object);
			callback.save(true);
		}
	}
}
