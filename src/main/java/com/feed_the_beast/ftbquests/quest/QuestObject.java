package com.feed_the_beast.ftbquests.quest;

import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestObject extends QuestObjectBase
{
	private Short2IntOpenHashMap cachedRelativeProgress;

	public QuestObject()
	{
		cachedRelativeProgress = new Short2IntOpenHashMap();
		cachedRelativeProgress.defaultReturnValue(-1);
	}

	@Override
	public abstract void changeProgress(ITeamData data, EnumChangeProgress type);

	public abstract int getRelativeProgressFromChildren(ITeamData data);

	public final int getRelativeProgress(ITeamData data)
	{
		int i = cachedRelativeProgress.get(data.getTeamUID());

		if (i == -1)
		{
			i = getRelativeProgressFromChildren(data);
			cachedRelativeProgress.put(data.getTeamUID(), i);
		}

		return i;
	}

	public static int getRelativeProgressFromChildren(int progressSum, int count)
	{
		if (count <= 0 || progressSum <= 0)
		{
			return 0;
		}
		else if (progressSum >= count * 100)
		{
			return 100;
		}

		return Math.max(1, (int) (progressSum / (double) count));
	}

	public final boolean isStarted(ITeamData data)
	{
		return getRelativeProgress(data) > 0;
	}

	public final boolean isComplete(ITeamData data)
	{
		return getRelativeProgress(data) >= 100;
	}

	public boolean isVisible(ITeamData data)
	{
		return true;
	}

	public void onCompleted(ITeamData data, List<EntityPlayerMP> notifyPlayers)
	{
	}

	@Override
	public abstract String getAltTitle();

	@Override
	public void clearCachedData()
	{
		super.clearCachedData();
		clearCachedProgress((short) 0);
	}

	public void clearCachedProgress(short id)
	{
		if (id == 0)
		{
			cachedRelativeProgress.clear();
		}
		else
		{
			cachedRelativeProgress.remove(id);
		}
	}

	protected boolean verifyDependenciesInternal(QuestObject original, boolean firstLoop)
	{
		return true;
	}
}