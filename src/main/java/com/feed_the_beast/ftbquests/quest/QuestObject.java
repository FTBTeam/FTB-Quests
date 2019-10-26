package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestObject extends QuestObjectBase
{
	public boolean disableToast = false;

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);

		if (disableToast)
		{
			nbt.setBoolean("disable_toast", true);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		disableToast = nbt.getBoolean("disable_toast");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeBoolean(disableToast);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		disableToast = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addBool("disable_toast", () -> disableToast, v -> disableToast = v, false).setDisplayName(new TextComponentTranslation("ftbquests.disable_completion_toast")).setCanEdit(getQuestChapter() == null || !getQuestChapter().alwaysInvisible).setOrder(10);
	}

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

	public void onCompleted(QuestData data, List<EntityPlayerMP> onlineMembers, List<EntityPlayerMP> notifiedPlayers)
	{
	}

	@Override
	public abstract String getAltTitle();

	protected boolean verifyDependenciesInternal(QuestObject original, boolean firstLoop)
	{
		return true;
	}
}