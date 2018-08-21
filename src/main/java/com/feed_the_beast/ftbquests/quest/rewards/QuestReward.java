package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author LatvianModder
 */
public abstract class QuestReward extends QuestObject
{
	public final Quest quest;
	public boolean teamReward;

	public QuestReward(Quest q)
	{
		quest = q;
		teamReward = false;
	}

	public abstract ItemStack getRewardItem();

	@Override
	public final QuestFile getQuestFile()
	{
		return quest.chapter.file;
	}

	@Override
	public final QuestObjectType getObjectType()
	{
		return QuestObjectType.REWARD;
	}

	@Override
	public final String getID()
	{
		return quest.chapter.id + ':' + quest.id + '#' + id;
	}

	@Override
	public void deleteSelf()
	{
		super.deleteSelf();
		quest.rewards.remove(this);
	}

	@Override
	public void deleteChildren()
	{
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return QuestRewardType.getType(getClass()).getDisplayName();
	}

	@Override
	public final void getExtraConfig(ConfigGroup config)
	{
		super.getExtraConfig(config);

		config.add("team_reward", new ConfigBoolean(teamReward)
		{
			@Override
			public boolean getBoolean()
			{
				return teamReward;
			}

			@Override
			public void setBoolean(boolean v)
			{
				teamReward = v;
			}
		}, new ConfigBoolean(false)).setDisplayName(new TextComponentTranslation("ftbquests.reward.team_reward")).setOrder((byte) -99);
	}

	@Override
	public Icon getIcon()
	{
		Icon icon = super.getIcon();
		return icon.isEmpty() ? ItemIcon.getItemIcon(new ItemStack(Items.GOLD_INGOT)) : icon;
	}
}