package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.IOpenableGui;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiSelectQuestObject extends GuiButtonListBase
{
	public class ButtonQuestObject extends SimpleTextButton
	{
		public final QuestObjectBase object;

		public ButtonQuestObject(Panel panel, @Nullable QuestObjectBase o)
		{
			super(panel, o == null ? I18n.format("ftbquests.null") : o.getObjectType().getColor() + o.getUnformattedTitle(), o == null ? Icon.EMPTY : o.getIcon());
			object = o;
			setSize(200, 14);
		}

		private void addObject(List<String> list, QuestObjectBase o)
		{
			list.add(TextFormatting.GRAY + o.getObjectType().getDisplayName() + ": " + o.getObjectType().getColor() + o.getUnformattedTitle());
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			if (object == null)
			{
				return;
			}

			list.add(object.getTitle());
			list.add(TextFormatting.GRAY + "ID: " + TextFormatting.DARK_GRAY + object);
			list.add(TextFormatting.GRAY + "Type: " + object.getObjectType().getColor() + I18n.format(object.getObjectType().getTranslationKey()));

			if (object instanceof Quest)
			{
				Quest quest = (Quest) object;
				addObject(list, quest.chapter);

				if (quest.rewards.size() == 1)
				{
					addObject(list, quest.rewards.get(0));
				}
				else if (!quest.rewards.isEmpty())
				{
					list.add(TextFormatting.GRAY + I18n.format("ftbquests.rewards"));

					for (QuestReward reward : quest.rewards)
					{
						list.add("  " + QuestObjectType.REWARD.getColor() + reward.getUnformattedTitle());
					}
				}
			}
			else if (object instanceof QuestTask)
			{
				Quest quest = ((QuestTask) object).quest;
				addObject(list, quest.chapter);
				addObject(list, quest);

				if (quest.rewards.size() == 1)
				{
					addObject(list, quest.rewards.get(0));
				}
				else if (!quest.rewards.isEmpty())
				{
					list.add(TextFormatting.GRAY + I18n.format("ftbquests.rewards"));

					for (QuestReward reward : quest.rewards)
					{
						list.add("  " + QuestObjectType.REWARD.getColor() + reward.getUnformattedTitle());
					}
				}
			}
			else if (object instanceof RewardTable)
			{
				((RewardTable) object).addMouseOverText(list, true, true);
			}
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			callbackGui.openGui();
			config.setObject(object);
			callback.run();
		}
	}

	private final ConfigQuestObject config;
	private final IOpenableGui callbackGui;
	private final Runnable callback;

	public GuiSelectQuestObject(ConfigQuestObject c, IOpenableGui g, Runnable cb)
	{
		setTitle(I18n.format("ftbquests.gui.select_quest_object"));
		setHasSearchBox(true);
		focus();
		setBorder(1, 1, 1);
		config = c;
		callbackGui = g;
		callback = cb;
	}

	@Override
	public void addButtons(Panel panel)
	{
		List<QuestObjectBase> list = new ArrayList<>();

		for (QuestObjectBase objectBase : ClientQuestFile.INSTANCE.getAllObjects())
		{
			if (config.isValid(objectBase))
			{
				list.add(objectBase);
			}
		}

		list.sort((o1, o2) -> {
			int i = Integer.compare(o1.getObjectType().ordinal(), o2.getObjectType().ordinal());
			return i == 0 ? o1.getUnformattedTitle().compareToIgnoreCase(o2.getUnformattedTitle()) : i;
		});

		if (config.isValid(0))
		{
			panel.add(new ButtonQuestObject(panel, null));
		}

		for (QuestObjectBase objectBase : list)
		{
			panel.add(new ButtonQuestObject(panel, objectBase));
		}
	}

	@Override
	public Theme getTheme()
	{
		return FTBQuestsTheme.INSTANCE;
	}
}