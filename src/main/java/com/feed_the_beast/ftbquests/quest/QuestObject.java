package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestObject extends QuestObjectBase
{
	public boolean disableToast = false;

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);

		if (disableToast)
		{
			nbt.putBoolean("disable_toast", true);
		}
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		disableToast = nbt.getBoolean("disable_toast");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeBoolean(disableToast);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		disableToast = buffer.readBoolean();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addBool("disable_toast", disableToast, v -> disableToast = v, false).setNameKey("ftbquests.disable_completion_toast").setCanEdit(getQuestChapter() == null || !getQuestChapter().alwaysInvisible).setOrder(10);
	}

	@Override
	public abstract void changeProgress(PlayerData data, ChangeProgress type);

	public abstract int getRelativeProgressFromChildren(PlayerData data);

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

	public boolean isVisible(PlayerData data)
	{
		return true;
	}

	public void onCompleted(PlayerData data, List<ServerPlayerEntity> onlineMembers, List<ServerPlayerEntity> notifiedPlayers)
	{
	}

	@Override
	public abstract IFormattableTextComponent getAltTitle();

	protected void verifyDependenciesInternal(int original, int depth)
	{
	}
}