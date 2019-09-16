package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
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
public class TileQuestBarrier extends TileBase implements IHasConfig, ITickable
{
	public String object = "";
	private Boolean prevCompleted = null;
	public boolean completed = false;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		nbt.setString("object", object);
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		object = nbt.getString("object");

		if (object.isEmpty())
		{
			object = QuestObjectBase.getCodeString(nbt.getInteger("object"));
		}

		prevCompleted = null;
	}

	@Override
	public void editConfig(EntityPlayerMP player, boolean editor)
	{
		if (!editor)
		{
			return;
		}

		ConfigGroup group0 = ConfigGroup.newGroup("tile");
		group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.barrier.name"));
		ConfigGroup config = group0.getGroup("ftbquests.barrier");

		QuestObject o = getObject(ServerQuestFile.INSTANCE);
		config.add("object", new ConfigQuestObject(ServerQuestFile.INSTANCE, o == null ? 0 : o.id, QuestObjectType.ALL_PROGRESSING)
		{
			@Override
			public void setObject(int v)
			{
				QuestObjectBase o = file.getBase(v);
				object = o == null ? "" : o.getCustomID();
			}
		}, new ConfigQuestObject(ServerQuestFile.INSTANCE, 1, QuestObjectType.ALL_PROGRESSING));

		FTBLibAPI.editServerConfig(player, group0, this);
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		updateContainingBlockInfo();
		markDirty();
	}

	@Override
	public void update()
	{
		if (world.isRemote && world.getTotalWorldTime() % 7L == 0L)
		{
			QuestObject o = getObject(ClientQuestFile.INSTANCE);
			completed = o != null && ClientQuestFile.existsWithTeam() && o.isComplete(ClientQuestFile.INSTANCE.self);

			if (prevCompleted == null || prevCompleted != completed)
			{
				prevCompleted = completed;
				markDirty();
			}
		}

		checkIfDirty();
	}

	@Override
	protected void sendDirtyUpdate()
	{
		if (world != null)
		{
			world.markChunkDirty(pos, this);
			BlockUtils.notifyBlockUpdate(world, pos, getBlockState());
		}
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		prevCompleted = null;
	}

	@Nullable
	public QuestObject getObject(QuestFile file)
	{
		return file.get(file.getID(object));
	}
}