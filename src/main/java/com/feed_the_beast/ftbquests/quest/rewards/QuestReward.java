package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestList;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public abstract class QuestReward extends QuestObject
{
	public final Quest quest;
	public boolean teamReward = false;

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

	@Override
	public final void delete()
	{
		super.delete();
		quest.rewards.remove(this);
	}

	public abstract void reward(EntityPlayerMP player);

	public abstract Icon getIcon();

	public abstract void writeData(NBTTagCompound nbt);

	@SideOnly(Side.CLIENT)
	public abstract String getDisplayName();
}