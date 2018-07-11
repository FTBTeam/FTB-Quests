package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.events.QuestRewardEvent;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestList;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
	public static QuestReward createReward(Quest quest, int id, NBTTagCompound nbt)
	{
		QuestReward reward = null;

		if (nbt.hasKey("item"))
		{
			ItemStack stack = new ItemStack(nbt.getCompoundTag("item"));

			if (!stack.isEmpty())
			{
				reward = new ItemReward(quest, id, stack);
			}
		}
		else if (nbt.hasKey("xp"))
		{
			reward = new ExperienceReward(quest, id, nbt.getInteger("xp"));
		}
		else if (nbt.hasKey("xp_levels"))
		{
			reward = new ExperienceLevelReward(quest, id, nbt.getInteger("xp_levels"));
		}
		else
		{
			QuestRewardEvent event = new QuestRewardEvent(quest, id, nbt);
			event.post();
			reward = event.getReward();
		}

		if (reward != null)
		{
			reward.teamReward = nbt.getBoolean("team_reward");
		}

		return reward;
	}

	public boolean teamReward = false;

	public abstract void reward(EntityPlayerMP player);

	public abstract Icon getIcon();

	public abstract void writeData(NBTTagCompound nbt);

	@SideOnly(Side.CLIENT)
	public abstract String getDisplayName();
}