package dev.ftb.mods.ftbquests.gui;

import dev.ftb.mods.ftbguilibrary.config.ConfigCallback;
import dev.ftb.mods.ftbguilibrary.icon.Icon;
import dev.ftb.mods.ftbguilibrary.misc.ButtonListBaseScreen;
import dev.ftb.mods.ftbguilibrary.utils.Key;
import dev.ftb.mods.ftbguilibrary.utils.MouseButton;
import dev.ftb.mods.ftbguilibrary.utils.TooltipList;
import dev.ftb.mods.ftbguilibrary.widget.Panel;
import dev.ftb.mods.ftbguilibrary.widget.SimpleTextButton;
import dev.ftb.mods.ftbguilibrary.widget.Theme;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class SelectQuestObjectScreen<T extends QuestObjectBase> extends ButtonListBaseScreen {
	public class QuestObjectButton extends SimpleTextButton {
		public final T object;

		public QuestObjectButton(Panel panel, @Nullable T o) {
			super(panel, o == null ? new TranslatableComponent("ftbquests.null") : o.getMutableTitle().withStyle(o.getObjectType().getColor()), o == null ? Icon.EMPTY : o.getIcon());
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
			list.add(new TextComponent("ID: ").withStyle(ChatFormatting.GRAY).append(new TextComponent(object.toString()).withStyle(ChatFormatting.DARK_GRAY)));
			list.add(new TextComponent("Type: ").withStyle(ChatFormatting.GRAY).append(QuestObjectType.NAME_MAP.getDisplayName(object.getObjectType()).copy().withStyle(object.getObjectType().getColor())));

			if (object instanceof Quest) {
				Quest quest = (Quest) object;
				addObject(list, quest.chapter);

				if (quest.rewards.size() == 1) {
					addObject(list, quest.rewards.get(0));
				} else if (!quest.rewards.isEmpty()) {
					list.add(new TranslatableComponent("ftbquests.rewards").withStyle(ChatFormatting.GRAY));

					for (Reward reward : quest.rewards) {
						list.add(new TextComponent("  ").append(reward.getMutableTitle().withStyle(QuestObjectType.REWARD.getColor())));
					}
				}
			} else if (object instanceof Task) {
				Quest quest = ((Task) object).quest;
				addObject(list, quest.chapter);
				addObject(list, quest);

				if (quest.rewards.size() == 1) {
					addObject(list, quest.rewards.get(0));
				} else if (!quest.rewards.isEmpty()) {
					list.add(new TranslatableComponent("ftbquests.rewards").withStyle(ChatFormatting.GRAY));

					for (Reward reward : quest.rewards) {
						list.add(new TextComponent("  ").append(reward.getMutableTitle().withStyle(QuestObjectType.REWARD.getColor())));
					}
				}
			} else if (object instanceof Reward) {
				Quest quest = ((Reward) object).quest;
				addObject(list, quest.chapter);
				addObject(list, quest);
			} else if (object instanceof RewardTable) {
				((RewardTable) object).addMouseOverText(list, true, true);
			}
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			config.setCurrentValue(object);
			callback.save(true);
		}
	}

	private final ConfigQuestObject<T> config;
	private final ConfigCallback callback;

	public SelectQuestObjectScreen(ConfigQuestObject<T> c, ConfigCallback cb) {
		setTitle(new TranslatableComponent("ftbquests.gui.select_quest_object"));
		setHasSearchBox(true);
		focus();
		setBorder(1, 1, 1);
		config = c;
		callback = cb;
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

		for (QuestObjectBase objectBase : ClientQuestFile.INSTANCE.getAllObjects()) {
			if (config.predicate.test(objectBase)) {
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
}