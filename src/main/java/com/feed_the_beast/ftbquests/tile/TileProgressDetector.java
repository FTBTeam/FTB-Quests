package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.block.BlockFlags;
import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileProgressDetector extends TileBase implements ITickable, IConfigCallback
{
	public final ConfigString owner = new ConfigString("");
	public final ConfigInt object = new ConfigInt(1, 1, QuestObject.MAX_ID);
	public final ConfigBoolean level = new ConfigBoolean(false);
	public int redstoneOutput = 0;

	private IProgressData cOwner;
	private ProgressingQuestObject cObject;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		nbt.setInteger("Object", object.getInt());
		nbt.setBoolean("Level", level.getBoolean());
		nbt.setByte("RedstoneOutput", (byte) redstoneOutput);
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		object.setInt(nbt.getInteger("Object"));
		level.setBoolean(nbt.getBoolean("Level"));
		redstoneOutput = nbt.getByte("RedstoneOutput");
		updateContainingBlockInfo();
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		cOwner = null;
		cObject = null;
	}

	@Nullable
	public IProgressData getOwner()
	{
		if (owner.isEmpty())
		{
			return null;
		}
		else if (cOwner == null)
		{
			cOwner = FTBQuests.PROXY.getQuestList(world.isRemote).getData(owner.getString());
		}

		return cOwner;
	}

	@Nullable
	public ProgressingQuestObject getObject()
	{
		if (object.getInt() == 0)
		{
			return null;
		}
		else if (cObject == null || cObject.invalid)
		{
			QuestObject o = FTBQuests.PROXY.getQuestList(world.isRemote).get((short) object.getInt());
			cObject = o instanceof ProgressingQuestObject && !o.invalid ? (ProgressingQuestObject) o : null;
		}

		return cObject;
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		updateContainingBlockInfo();
		markDirty();
		world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), BlockFlags.DEFAULT_AND_RERENDER);
	}

	@Override
	public void update()
	{
		if (world.getTotalWorldTime() % 7L != 0L)
		{
			return;
		}

		int rout = redstoneOutput;
		redstoneOutput = 0;

		cOwner = getOwner();
		cObject = getObject();

		if (cOwner != null && cObject != null)
		{
			double rel = cObject.getRelativeProgress(cOwner);

			if (rel >= 1D)
			{
				redstoneOutput = 15;
			}
			else if (rel > 0D && level.getBoolean())
			{
				redstoneOutput = 1 + (int) (rel * 14);
			}
		}

		if (rout != redstoneOutput)
		{
			world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
			markDirty();
		}

		checkIfDirty();
	}

	public void editConfig(EntityPlayerMP player)
	{
		ConfigGroup group0 = ConfigGroup.newGroup("tile");
		group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.progress_detector.name"));
		ConfigGroup group = group0.getGroup("ftbquests.progress_detector");
		group.add("owner", owner, new ConfigString("")).setDisplayName(new TextComponentTranslation("ftbquests.owner"));
		group.add("object", object, ConfigNull.INSTANCE);
		group.add("level", level, new ConfigBoolean(false));
		FTBLibAPI.editServerConfig(player, group0, this);
	}
}