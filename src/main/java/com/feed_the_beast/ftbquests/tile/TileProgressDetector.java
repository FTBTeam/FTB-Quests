package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileProgressDetector extends TileBase implements ITickable, IConfigCallback
{
	public final ConfigString team = new ConfigString("");
	public final ConfigQuestObject object = new ConfigQuestObject("*").addType(QuestObjectType.FILE).addType(QuestObjectType.CHAPTER).addType(QuestObjectType.QUEST).addType(QuestObjectType.TASK);
	public final ConfigBoolean level = new ConfigBoolean(false);
	public int redstoneOutput = 0;

	private IProgressData cTeam;
	private ProgressingQuestObject cObject;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!team.isEmpty())
		{
			nbt.setString("Team", team.getString());
		}

		cObject = getObject();

		if (cObject != null)
		{
			object.setString(cObject.getID());
		}

		if (!object.isEmpty())
		{
			nbt.setString("Object", object.getString());
		}

		if (level.getBoolean())
		{
			nbt.setBoolean("Level", true);
		}

		if (redstoneOutput > 0)
		{
			nbt.setByte("RedstoneOutput", (byte) redstoneOutput);
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		team.setString(nbt.getString("Team"));
		object.setString(nbt.getString("Object"));
		level.setBoolean(nbt.getBoolean("Level"));
		redstoneOutput = nbt.getByte("RedstoneOutput");
		updateContainingBlockInfo();
	}

	@Override
	public void writeToItem(ItemStack stack)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeData(nbt, EnumSaveType.ITEM);

		if (!nbt.isEmpty())
		{
			stack.setTagCompound(nbt);
		}
	}

	@Override
	public void readFromItem(ItemStack stack)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		readData(nbt == null ? new NBTTagCompound() : nbt, EnumSaveType.ITEM);
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		cTeam = null;
		cObject = null;
	}

	@Nullable
	public IProgressData getTeam()
	{
		if (team.isEmpty())
		{
			return null;
		}
		else if (cTeam == null)
		{
			cTeam = FTBQuests.PROXY.getQuestFile(world).getData(team.getString());
		}

		return cTeam;
	}

	@Nullable
	public ProgressingQuestObject getObject()
	{
		if (object.isEmpty())
		{
			return null;
		}
		else if (cObject == null || cObject.invalid)
		{
			cObject = FTBQuests.PROXY.getQuestFile(world).getProgressing(object.getString());
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

		cTeam = getTeam();
		cObject = getObject();

		if (cTeam != null && cObject != null)
		{
			double rel = cObject.getRelativeProgress(cTeam);

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
	}

	public void editConfig(EntityPlayerMP player)
	{
		cObject = getObject();

		if (cObject != null)
		{
			object.setString(cObject.getID());
		}

		ConfigGroup group0 = ConfigGroup.newGroup("tile");
		group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.progress_detector.name"));
		ConfigGroup group = group0.getGroup("ftbquests.progress_detector");
		group.add("team", team, new ConfigString("")).setDisplayName(new TextComponentTranslation("ftbquests.team"));
		group.add("object", object, ConfigNull.INSTANCE);
		group.add("level", level, new ConfigBoolean(false));
		FTBLibAPI.editServerConfig(player, group0, this);
	}
}