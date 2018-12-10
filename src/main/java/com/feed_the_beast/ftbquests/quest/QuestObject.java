package com.feed_the_beast.ftbquests.quest;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestObject extends QuestObjectBase
{
	public abstract long getProgress(ITeamData data);

	public abstract long getMaxProgress();

	public abstract void completeInstantly(ITeamData data, boolean dependencies);

	public abstract int getRelativeProgress(ITeamData data);

	public static int fixRelativeProgress(int progress, int max)
	{
		if (max <= 0 || progress >= max * 100)
		{
			return 100;
		}
		else if (progress <= 0)
		{
			return 0;
		}

		return Math.max(1, (int) (progress / (double) max));
	}

	public abstract boolean isComplete(ITeamData data);

	public void onCompleted(ITeamData data, List<EntityPlayerMP> onlineMembers)
	{
	}

	@Override
	public abstract ITextComponent getAltDisplayName();
}