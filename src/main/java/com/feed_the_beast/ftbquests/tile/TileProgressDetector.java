package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.ConfigTeam;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.QuestObject;
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
	public String team = "";
	public String object = "*";
	public boolean level = false;
	public int redstoneOutput = 0;

	private ITeamData cTeam;
	private QuestObject cObject;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!team.isEmpty())
		{
			nbt.setString("Team", team);
		}

		cObject = getObject();

		if (cObject != null)
		{
			object = cObject.getID();
		}

		if (!object.isEmpty())
		{
			nbt.setString("Object", object);
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
		team = nbt.getString("Team");
		object = nbt.getString("Object");
		level = nbt.getBoolean("Level");

		if (!type.item)
		{
			redstoneOutput = nbt.getByte("RedstoneOutput");
		}

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
	public ITeamData getTeam()
	{
		if (team.isEmpty())
		{
			return null;
		}
		else if (cTeam == null)
		{
			cTeam = FTBQuests.PROXY.getQuestFile(world).getData(team);
		}

		return cTeam;
	}

	@Nullable
	public QuestObject getObject()
	{
		if (object.isEmpty())
		{
			return null;
		}
		else if (cObject == null || cObject.invalid)
		{
			cObject = FTBQuests.PROXY.getQuestFile(world).get(object);
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
			int rel = cObject.getRelativeProgress(cTeam);

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
		if (!player.isSneaking())
		{
			return;
		}

		boolean editor = FTBQuests.canEdit(player);

		if (!editor && !team.equals(Universe.get().getPlayer(player).team.getName()))
		{
			return;
		}

		cObject = getObject();

		if (cObject != null)
		{
			object = cObject.getID();
		}

		ConfigGroup group0 = ConfigGroup.newGroup("tile");
		group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.progress_detector.name"));
		ConfigGroup group = group0.getGroup("ftbquests.progress_detector");

		group.add("team", new ConfigTeam(team)
		{
			@Override
			public void setString(String v)
			{
				team = v;
			}
		}, ConfigNull.INSTANCE).setDisplayName(new TextComponentTranslation("ftbquests.team")).setCanEdit(editor);

		group.add("object", new ConfigQuestObject(object)
		{
			@Override
			public void setString(String v)
			{
				object = v;
			}
		}.addType(QuestObjectType.FILE).addType(QuestObjectType.CHAPTER).addType(QuestObjectType.QUEST).addType(QuestObjectType.TASK), new ConfigQuestObject("*"));

		group.add("level", new ConfigBoolean(level)
		{
			@Override
			public void setBoolean(boolean v)
			{
				level = v;
			}
		}, new ConfigBoolean(false));

		FTBLibAPI.editServerConfig(player, group0, this);
	}
}