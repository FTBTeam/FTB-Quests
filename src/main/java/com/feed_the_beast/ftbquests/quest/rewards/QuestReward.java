package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestList;
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
	public boolean teamReward = false;

	public QuestReward(Quest q, NBTTagCompound nbt)
	{
		super(q.chapter.list.getID(nbt));
		quest = q;
	}

	@Override
	public final QuestList getQuestList()
	{
		return quest.getQuestList();
	}

	@Override
	public final QuestObjectType getObjectType()
	{
		return QuestObjectType.REWARD;
	}

	@Override
	public final void delete()
	{
		super.delete();
		quest.rewards.remove(this);
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation("ftbquests.reward." + getName());
	}

	public abstract void reward(EntityPlayerMP player);
}