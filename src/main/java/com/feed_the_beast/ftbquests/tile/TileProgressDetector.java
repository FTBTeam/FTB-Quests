package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileProgressDetector extends TileWithTeam implements ITickable, IConfigCallback
{
	public int object = 0;
	public boolean level = false;
	public int redstoneOutput = 0;

	private QuestObject cObject;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		super.writeData(nbt, type);
		cObject = getObject();

		if (cObject != null)
		{
			object = cObject.id;
		}

		if (object != 0)
		{
			nbt.setInteger("Object", object);
		}

		if (level)
		{
			nbt.setBoolean("Level", true);
		}

		if (redstoneOutput > 0 && !type.item)
		{
			nbt.setByte("RedstoneOutput", (byte) redstoneOutput);
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		super.readData(nbt, type);
		object = nbt.getInteger("Object");
		level = nbt.getBoolean("Level");

		if (!type.item)
		{
			redstoneOutput = nbt.getByte("RedstoneOutput");
		}

		updateContainingBlockInfo();
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		cObject = null;
	}

	@Nullable
	public QuestObject getObject()
	{
		if (object == 0)
		{
			return null;
		}
		else if (cObject == null || cObject.invalid)
		{
			QuestFile file = FTBQuests.PROXY.getQuestFile(world);

			if (file == null)
			{
				return null;
			}

			cObject = file.get(object);
		}

		return cObject;
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		updateContainingBlockInfo();
		markDirty();
		updateRedstoneOutput();
		BlockUtils.notifyBlockUpdate(world, pos, getBlockState());
	}

	@Override
	public void update()
	{
		if (world.getTotalWorldTime() % 7L == 0L)
		{
			updateRedstoneOutput();
		}

		checkIfDirty();
	}

	public void updateRedstoneOutput()
	{
		int rout = redstoneOutput;
		redstoneOutput = 0;

		QuestFile file = FTBQuests.PROXY.getQuestFile(world);

		if (file == null)
		{
			return;
		}

		QuestData data = file.getData(team);
		QuestObject o = file.get(object);

		if (data != null && o != null)
		{
			int rel = o.getRelativeProgress(data);

			if (rel >= 100)
			{
				redstoneOutput = 15;
			}
			else if (rel > 0 && level)
			{
				redstoneOutput = 1 + (int) (rel * 14D / 100D);
			}
		}

		if (rout != redstoneOutput)
		{
			world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
			markDirty();
		}
	}

	public void editConfig(EntityPlayerMP player)
	{
		boolean editor = FTBQuests.canEdit(player);

		if (!editor && !isOwner(player))
		{
			return;
		}

		ConfigGroup group0 = ConfigGroup.newGroup("tile");
		group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.progress_detector.name"));
		ConfigGroup config = group0.getGroup("ftbquests.progress_detector");

		if (editor)
		{
			config.add("team", createTeamConfig(), ConfigNull.INSTANCE).setDisplayName(new TextComponentTranslation("ftbquests.team"));
		}

		config.add("object", new ConfigQuestObject(ServerQuestFile.INSTANCE, object, QuestObjectType.ALL_PROGRESSING)
		{
			@Override
			public void setObject(int v)
			{
				object = v;
				cObject = file.get(object);
			}
		}, new ConfigQuestObject(ServerQuestFile.INSTANCE, 1, QuestObjectType.ALL_PROGRESSING));

		config.addBool("level", () -> level, v -> level = v, false);

		FTBLibAPI.editServerConfig(player, group0, this);
	}
}