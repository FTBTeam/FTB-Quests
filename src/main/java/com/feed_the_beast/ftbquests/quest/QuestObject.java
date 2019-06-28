package com.feed_the_beast.ftbquests.quest;

import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestObject extends QuestObjectBase
{
	@Override
	public abstract void changeProgress(QuestData data, ChangeProgress type);

	public abstract int getRelativeProgressFromChildren(QuestData data);

	public final int getRelativeProgress(QuestData data)
	{
		if (!cacheProgress())
		{
			return getRelativeProgressFromChildren(data);
		}

		if (data.progressCache == null)
		{
			data.progressCache = new Int2ByteOpenHashMap();
			data.progressCache.defaultReturnValue((byte) -1);
		}

		int i = data.progressCache.get(id);

		if (i == -1)
		{
			i = getRelativeProgressFromChildren(data);
			data.progressCache.put(id, (byte) i);
		}

		return i;
	}

	public boolean cacheProgress()
	{
		return true;
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

	public final boolean isStarted(QuestData data)
	{
		return getRelativeProgress(data) > 0;
	}

	public final boolean isComplete(QuestData data)
	{
		return getRelativeProgress(data) >= 100;
	}

	public boolean isVisible(QuestData data)
	{
		return true;
	}

	public void onCompleted(QuestData data, List<EntityPlayerMP> notifyPlayers)
	{
	}

	@Override
	public abstract String getAltTitle();

	protected boolean verifyDependenciesInternal(QuestObject original, boolean firstLoop)
	{
		return true;
	}
}