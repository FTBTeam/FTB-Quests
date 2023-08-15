package dev.ftb.mods.ftbquests.gui;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.ButtonListBaseScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SelectQuestObjectScreen<T extends QuestObjectBase> extends ButtonListBaseScreen {
	private final ConfigQuestObject<T> config;
	private final ConfigCallback callback;

	public SelectQuestObjectScreen(ConfigQuestObject<T> c, ConfigCallback cb) {
		setTitle(Component.translatable("ftbquests.gui.select_quest_object"));
		setHasSearchBox(true);
		focus();
		setBorder(1, 1, 1);
		config = c;
		callback = cb;
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

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			callback.save(false);
			return false;
		}

		return false;
	}

	@Override
	public void addButtons(Panel panel) {
		List<T> list = new ArrayList<>();

		ClientQuestFile file = ClientQuestFile.INSTANCE;
		for (QuestObjectBase objectBase : file.getAllObjects()) {
			if (config.predicate.test(objectBase) && (file.canEdit() || (!(objectBase instanceof QuestObject qo) || qo.isVisible(file.self)))) {
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
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	private class QuestObjectButton extends SimpleTextButton {
		public final T object;

		public QuestObjectButton(Panel panel, @Nullable T o) {
			super(panel, o == null ? Component.translatable("ftbquests.null") : o.getMutableTitle().withStyle(o.getObjectType().getColor()), o == null ? Color4I.EMPTY : o.getIcon());
			object = o;
			setSize(200, 14);
		}

		private void addObject(TooltipList list, QuestObjectBase o) {
			list.add(QuestObjectType.NAME_MAP.getDisplayName(o.getObjectType()).copy().withStyle(ChatFormatting.GRAY).append(": ").append(o.getMutableTitle().withStyle(o.getObjectType().getColor())));
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			if (object == null) {
				return;
			}

			list.add(object.getTitle());
			list.add(Component.literal("ID: ").withStyle(ChatFormatting.GRAY).append(Component.literal(object.toString()).withStyle(ChatFormatting.DARK_GRAY)));
			list.add(Component.literal("Type: ").withStyle(ChatFormatting.GRAY).append(QuestObjectType.NAME_MAP.getDisplayName(object.getObjectType()).copy().withStyle(object.getObjectType().getColor())));

			if (object instanceof Quest quest) {
				addObject(list, quest.chapter);

				if (quest.rewards.size() == 1) {
					addObject(list, quest.rewards.get(0));
				} else if (!quest.rewards.isEmpty()) {
					list.add(Component.translatable("ftbquests.rewards").withStyle(ChatFormatting.GRAY));
					for (Reward reward : quest.rewards) {
						list.add(Component.literal("  ").append(reward.getMutableTitle().withStyle(QuestObjectType.REWARD.getColor())));
					}
				}
			} else if (object instanceof QuestLink link) {
				link.getQuest().ifPresent(quest -> {
					addObject(list, link.getChapter());
					list.add(Component.translatable("ftbquests.gui.linked_quest_id", Component.literal(quest.getCodeString()).withStyle(ChatFormatting.DARK_GRAY)).withStyle(ChatFormatting.GRAY));
					addObject(list, quest.chapter);
				});
			} else if (object instanceof Task task) {
				Quest quest = task.quest;
				addObject(list, quest.chapter);
				addObject(list, quest);

				if (quest.rewards.size() == 1) {
					addObject(list, quest.rewards.get(0));
				} else if (!quest.rewards.isEmpty()) {
					list.add(Component.translatable("ftbquests.rewards").withStyle(ChatFormatting.GRAY));

					for (Reward reward : quest.rewards) {
						list.add(Component.literal("  ").append(reward.getMutableTitle().withStyle(QuestObjectType.REWARD.getColor())));
					}
				}
			} else if (object instanceof Reward reward) {
				Quest quest = reward.quest;
				addObject(list, quest.chapter);
				addObject(list, quest);
			} else if (object instanceof RewardTable rewardTable) {
				rewardTable.addMouseOverText(list, true, true);
			}
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			config.setCurrentValue(object);
			callback.save(true);
		}
	}
}
