package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftbquests.events.QuestRewardEvent;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestList;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class QuestReward extends QuestObject
{
	public final Quest quest;

	public QuestReward(Quest q, int id)
	{
		super(id);
		quest = q;
	}

	@Override
	public QuestList getQuestList()
	{
		return quest.getQuestList();
	}

	@Nullable
	public static QuestReward createReward(Quest quest, int id, JsonObject json)
	{
		QuestReward reward = null;

		if (json.has("item"))
		{
			ItemStack stack = ItemStackSerializer.deserialize(json.get("item"));

			if (!stack.isEmpty())
			{
				reward = new ItemReward(quest, id, stack);
			}
		}
		else if (json.has("xp"))
		{
			reward = new ExperienceReward(quest, id, json.get("xp").getAsInt());
		}
		else if (json.has("xp_levels"))
		{
			reward = new ExperienceLevelReward(quest, id, json.get("xp_levels").getAsInt());
		}
		else
		{
			QuestRewardEvent event = new QuestRewardEvent(quest, id, json);
			event.post();
			reward = event.getReward();
		}

		if (reward != null)
		{
			if (json.has("team_reward"))
			{
				reward.teamReward = json.get("team_reward").getAsBoolean();
			}
		}

		return reward;
	}

	public boolean teamReward = false;

	public abstract void reward(EntityPlayerMP player);

	public abstract Icon getIcon();

	public abstract JsonObject toJson();

	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return toJson().toString();
	}
}