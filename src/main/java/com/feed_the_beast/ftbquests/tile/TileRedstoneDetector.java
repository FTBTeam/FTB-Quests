package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author LatvianModder
 */
public class TileRedstoneDetector extends TileWithTeam implements IHasConfig
{
	public int task = 0;
	public int requiredRedstone = 1;
	public int currentRedstone = 0;
	public boolean notifications = true;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		super.writeData(nbt, type);
		nbt.setInteger("task", task);
		nbt.setByte("required_redstone", (byte) requiredRedstone);
		nbt.setByte("current_redstone", (byte) currentRedstone);
		nbt.setBoolean("notifications", notifications);
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		super.readData(nbt, type);
		task = nbt.getInteger("task");
		requiredRedstone = nbt.getByte("required_redstone");
		currentRedstone = nbt.getByte("current_redstone");
		notifications = nbt.getBoolean("notifications");
	}

	public void checkRedstone()
	{
		int prev = currentRedstone;
		currentRedstone = world.getRedstonePowerFromNeighbors(pos);

		if (prev != currentRedstone && currentRedstone >= requiredRedstone && !world.isRemote)
		{
			Task t = ServerQuestFile.INSTANCE.getTask(task);

			if (t != null)
			{
				QuestFile file = FTBQuests.PROXY.getQuestFile(world);
				QuestData data = file == null ? null : file.getData(team);

				if (data != null && !t.isComplete(data) && t.quest.canStartTasks(data))
				{
					t.forceProgress(data, ChangeProgress.COMPLETE, notifications);
				}
			}
		}
	}

	@Override
	public void editConfig(EntityPlayerMP player, boolean editor)
	{
		if (!editor)
		{
			return;
		}

		ConfigGroup group0 = ConfigGroup.newGroup("tile");
		group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.detector.redstone.name"));
		ConfigGroup config = group0.getGroup("ftbquests.detector.redstone");

		config.add("team", createTeamConfig(), ConfigNull.INSTANCE).setDisplayName(new TextComponentTranslation("ftbquests.team"));

		config.add("task", new ConfigQuestObject(ServerQuestFile.INSTANCE, task, CustomTask.PREDICATE)
		{
			@Override
			public void setObject(int v)
			{
				task = v;
			}
		}, new ConfigQuestObject(ServerQuestFile.INSTANCE, 0, CustomTask.PREDICATE)).setDisplayName(new TextComponentTranslation("tile.ftbquests.detector.task"));

		config.addInt("required_redstone", () -> requiredRedstone, v -> requiredRedstone = v, 1, 1, 15);
		config.addBool("notifications", () -> notifications, v -> notifications = v, true).setDisplayName(new TextComponentTranslation("tile.ftbquests.detector.notifications"));

		FTBLibAPI.editServerConfig(player, group0, this);
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		markDirty();
	}

	@Override
	public void setIDFromPlacer(EntityLivingBase placer)
	{
		super.setIDFromPlacer(placer);
		checkRedstone();
	}
}