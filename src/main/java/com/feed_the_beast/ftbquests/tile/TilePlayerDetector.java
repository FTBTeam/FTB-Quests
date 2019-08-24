package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author LatvianModder
 */
public class TilePlayerDetector extends TileBase implements IHasConfig, ITickable
{
	public int task = 0;
	public boolean notifications = true;
	public double radius = 1.49D;
	public double offsetY = 1.01D;
	public double height = 2.98D;
	private AxisAlignedBB cachedAABB;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		nbt.setInteger("task", task);
		nbt.setBoolean("notifications", notifications);
		nbt.setDouble("radius", radius);
		nbt.setDouble("offset_y", offsetY);
		nbt.setDouble("height", height);
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		task = nbt.getInteger("task");
		notifications = nbt.getBoolean("notifications");
		radius = nbt.getDouble("radius");
		offsetY = nbt.getDouble("offset_y");
		height = nbt.getDouble("height");
	}

	@Override
	public void editConfig(EntityPlayerMP player, boolean editor)
	{
		if (!editor)
		{
			return;
		}

		ConfigGroup group0 = ConfigGroup.newGroup("tile");
		group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.detector.player.name"));
		ConfigGroup config = group0.getGroup("ftbquests.detector.player");

		config.add("task", new ConfigQuestObject(ServerQuestFile.INSTANCE, task, CustomTask.PREDICATE)
		{
			@Override
			public void setObject(int v)
			{
				task = v;
			}
		}, new ConfigQuestObject(ServerQuestFile.INSTANCE, 0, CustomTask.PREDICATE)).setDisplayName(new TextComponentTranslation("tile.ftbquests.detector.task"));

		config.addBool("notifications", () -> notifications, v -> notifications = v, true).setDisplayName(new TextComponentTranslation("tile.ftbquests.detector.notifications"));
		config.addDouble("radius", () -> radius, v -> radius = v, 1.49D, 0.25D, 100D);
		config.addDouble("offset_y", () -> offsetY, v -> offsetY = v, 0.01D, -1000D, 1000D);
		config.addDouble("height", () -> height, v -> height = v, 2.98D, 0.1D, 1000D);

		FTBLibAPI.editServerConfig(player, group0, this);
	}

	@Override
	public void update()
	{
		if (world.isRemote || world.getTotalWorldTime() % 5L != 0L)
		{
			return;
		}

		Task t = ServerQuestFile.INSTANCE.getTask(task);

		if (t == null)
		{
			return;
		}

		AxisAlignedBB aabb = getAABB();

		for (EntityPlayerMP player : world.getEntitiesWithinAABB(EntityPlayerMP.class, aabb))
		{
			if (!player.capabilities.isCreativeMode)
			{
				QuestData data = ServerQuestFile.INSTANCE.getData(player);

				if (data != null && !t.isComplete(data) && t.quest.canStartTasks(data))
				{
					t.forceProgress(data, ChangeProgress.COMPLETE, notifications);
				}
			}
		}
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		markDirty();
		updateContainingBlockInfo();
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		cachedAABB = null;
	}

	public AxisAlignedBB getAABB()
	{
		if (cachedAABB == null)
		{
			double x = pos.getX() + 0.5D;
			double y = pos.getY() + offsetY;
			double z = pos.getZ() + 0.5D;
			cachedAABB = new AxisAlignedBB(x - radius, y, z - radius, x + radius, y + height, z + radius);
		}

		return cachedAABB;
	}
}