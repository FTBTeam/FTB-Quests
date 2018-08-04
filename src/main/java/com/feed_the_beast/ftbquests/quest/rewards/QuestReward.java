package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author LatvianModder
 */
public abstract class QuestReward extends QuestObject implements IStringSerializable
{
	public final Quest quest;
	public final ConfigBoolean teamReward;

	public QuestReward(Quest q, NBTTagCompound nbt)
	{
		super(q.chapter.file.getID(nbt));
		quest = q;
		teamReward = new ConfigBoolean(false);
	}

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
	public void deleteSelf()
	{
		super.deleteSelf();
		quest.rewards.remove(this);

		for (IProgressData data : quest.chapter.file.getAllData())
		{
			data.unclaimReward(id);
		}
	}

	@Override
	public void deleteChildren()
	{
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation("ftbquests.reward." + getName());
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("team_reward", teamReward, new ConfigBoolean(false)).setDisplayName(new TextComponentTranslation("ftbquests.reward.team_reward")).setOrder((byte) -128);
	}

	public abstract void reward(EntityPlayerMP player);
}